/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.core.database.incoming;

import com.ctc.wstx.stax.WstxInputFactory;
import eu.europeana.core.database.DashboardDao;
import eu.europeana.core.database.domain.CollectionState;
import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.database.domain.EuropeanaId;
import eu.europeana.core.database.domain.ImportFileState;
import eu.europeana.core.database.incoming.cache.ObjectCache;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.definitions.annotations.EuropeanaBean;
import eu.europeana.definitions.annotations.EuropeanaField;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Import xml files filled with normalized metadata into the database
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class ESEImporterImpl implements ESEImporter {
    private static final String RESOLVABLE_URI = "http://www.europeana.eu/resolve/record/";
    private static final DecimalFormat COUNT_FORMAT = new DecimalFormat("000000000");
    private static final String ESE_SCHEMA = "ESE-V3.2.xsd";
    private Logger log = Logger.getLogger(getClass());
    private DashboardDao dashboardDao;
    private ImportRepository importRepository;
    private SolrServer solrServer;
    private AnnotationProcessor annotationProcessor;
    private ObjectCache objectCache;
    private Class<?> beanClass;
    private EuropeanaBean europeanaBean;
    private boolean normalized;
    private int chunkSize = 1000;
    private int indexErrorCount = 0;
    private List<Processor> processors = new CopyOnWriteArrayList<Processor>();

    private interface Processor {
        ImportFile getFile();

        ImportFile stop();

        void start();
    }

    @Autowired
    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    @Autowired
    @Qualifier("solrUpdateServer")
    public void setSolrServer(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    @Autowired
    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }

    @Autowired
    public void setObjectCache(ObjectCache objectCache) {
        this.objectCache = objectCache;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    // npot @Autowired because there are multiple

    public void setImportRepository(ImportRepository importRepository) {
        this.importRepository = importRepository;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    @Override
    public ImportRepository getImportRepository() {
        return importRepository;
    }

    @Override
    public ImportFile commenceValidate(ImportFile importFile, Long collectionId) {
        importFile = importRepository.transition(importFile, ImportFileState.VALIDATING);
        if (importFile != null) {
            for (Processor processor : processors) {
                if (processor.getFile().equals(importFile)) {
                    return processor.getFile();
                }
            }
            Processor processor = new ValidationProcessor(importFile, collectionId);
            processors.add(processor);
            processor.start();
            return importFile;
        }
        return null;
    }

    @Override
    public ImportFile commenceImport(ImportFile importFile, Long collectionId) {
        ImportFile importingFile = importRepository.transition(importFile, ImportFileState.IMPORTING);
        if (importingFile != null) {
            for (Processor processor : processors) {
                if (processor.getFile().equals(importingFile)) {
                    return processor.getFile();
                }
            }
            ImportProcessor importProcessor = new ImportProcessor(importingFile, dashboardDao.prepareForImport(collectionId));
            processors.add(importProcessor);
            importProcessor.start();
            return importingFile;
        }
        return null;
    }

    @Override
    public ImportFile abortImport(ImportFile importingFile) {
        for (Processor processor : processors) {
            if (processor.getFile().equals(importingFile)) {
                return processor.stop();
            }
        }
        return importingFile;
    }

    @Override
    public List<ImportFile> getActiveImports() {
        List<ImportFile> active = new ArrayList<ImportFile>();
        for (Processor processor : processors) {
            active.add(processor.getFile());
        }
        return active;
    }

    @Override
    public void commit() throws IOException, SolrServerException {
        solrServer.commit();
    }

    private class ImportProcessor implements Runnable, Processor {
        private Thread thread;
        private ImportFile importFile;
        private EuropeanaCollection collection;
        private List<SolrInputDocument> recordList = new ArrayList<SolrInputDocument>();

        private ImportProcessor(ImportFile importFile, EuropeanaCollection collection) {
            this.importFile = importFile;
            this.collection = collection;
        }

        @Override
        public void start() {
            if (this.thread == null) {
                this.thread = new Thread(this);
                thread.setName(importFile.getFileName());
                thread.start();
            }
            else {
                log.warn("Import processor already started for " + importFile);
            }
        }

        @Override
        public ImportFile stop() {
            if (thread != null) {
                Thread threadToJoin = thread;
                thread = null;
                try {
                    threadToJoin.join();
                }
                catch (InterruptedException e) {
                    log.error("Interrupted!", e);
                }
            }
            return importFile;
        }

        @Override
        public void run() {
            log.info("Importing " + importFile);
            try {
                InputStream inputStream = createInputStream(importFile);
                importXml(inputStream);
                if (thread != null) {
                    log.info("Finished importing " + importFile);
                    collection = dashboardDao.updateCollectionCounters(collection.getId());
                    importFile = importRepository.transition(importFile, ImportFileState.IMPORTED);
                    collection.setFileState(ImportFileState.IMPORTED);
                    collection.setCollectionState(CollectionState.ENABLED);
                }
                else {
                    log.info("Aborted importing " + importFile);
                    collection.setCollectionState(CollectionState.EMPTY);
                    if (normalized) {
                        importFile = importRepository.transition(importFile, ImportFileState.UPLOADED);
                        collection.setFileState(ImportFileState.UPLOADED);
                    }
                    else {
                        importFile = importRepository.transition(importFile, ImportFileState.VALIDATED);
                        collection.setFileState(ImportFileState.VALIDATED);
                    }
                }
                collection = dashboardDao.updateCollection(collection);
                dashboardDao.removeFromIndexQueue(collection);
            }
            catch (ImportException e) {
                log.warn("Problem importing " + importFile + " to database, moving to error directory", e);
                collection = dashboardDao.setImportError(collection.getId(), exceptionToErrorString(e));
                importFile = importRepository.transition(importFile, ImportFileState.ERROR);
                collection.setFileState(ImportFileState.ERROR);
                collection = dashboardDao.updateCollection(collection);
            }
            finally {
                processors.remove(this);
                thread = null;
            }
        }

        private void importXml(InputStream inputStream) throws ImportException {
            try {
                importXmlInternal(inputStream);
            }
            catch (IOException e) {
                throw new ImportException("Problem reading the XML file", e);
            }
            catch (TransformerException e) {
                throw new ImportException("Problem transforming the XML file", e);
            }
            catch (XMLStreamException e) {
                throw new ImportException("Problem streaming the XML file", e);
            }
            catch (SolrServerException e) {
                throw new ImportException("Problem sending to Solr", e);
            }
            catch (Exception e) {
                throw new ImportException("Unknown problem", e);
            }
        }

        private void importXmlInternal(InputStream inputStream) throws TransformerException, XMLStreamException, IOException, ImportException, SolrServerException {
            XMLInputFactory inFactory = new WstxInputFactory();
            Source source = new StreamSource(inputStream, "UTF-8");
            XMLStreamReader xml = inFactory.createXMLStreamReader(source);
            BufferedWriter fetchScript = new BufferedWriter(new FileWriter(objectCache.getFetchScriptFile(collection)));
            fetchScript.write(objectCache.createFetchScriptBegin(collection.getName()));
            EuropeanaId europeanaId = null;
            int recordCount = 0;
            int objectCount = 0;
            long startTime = System.currentTimeMillis();
            SolrInputDocument solrInputDocument = null;
            while (thread != null) {
                switch (xml.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                        log.info("Document started");
                        break;

                    case XMLStreamConstants.START_ELEMENT:
                        if (isRecordElement(xml)) {
                            europeanaId = new EuropeanaId(collection);
                            solrInputDocument = new SolrInputDocument();
                            solrInputDocument.addField("europeana_collectionName", collection.getName()); // todo: can't just use a string field name here
                        }
                        else if (europeanaId != null) {
                            EuropeanaField field = getEuropeanaField(xml.getPrefix(), xml.getLocalName(), recordCount);
//                            String language = fetchLanguage(xml);
                            String text = xml.getElementText();
                            if (field.europeana().id()) {
                                europeanaId.setEuropeanaUri(text);
                            }
                            else if (field.europeana().object()) {
                                objectCount++;
                            }
                            else if (field.europeana().type()) {
                                DocType.get(text); // checking if it matches one of them
                                SolrInputField objectField = solrInputDocument.getField("europeana_type");
                                if (objectField != null) {
                                    break;
                                }
                            }
                            if (text.length() > 10000) {
                                text = text.substring(0, 9999);
                            }
                            // language being ignored if (language != null) {...}
                            solrInputDocument.addField(field.getFieldNameString(), text);
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (isRecordElement(xml) && europeanaId != null) {
                            if (recordCount > 0 && recordCount % 500 == 0) {
                                log.info(String.format("imported %d records in %s", recordCount, DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime)));
                            }
                            recordCount++;
                            if (normalized) {
                                if (europeanaId.getEuropeanaUri() == null) {
                                    throw new ImportException("Normalized Record must have a field designated as europeana uri", recordCount);
                                }
                            }
                            else {
                                if (europeanaId.getEuropeanaUri() != null) {
                                    throw new ImportException("Sandbox Record must not have a field designated as europeana uri", recordCount);
                                }
                                europeanaId.setEuropeanaUri(String.format("%s%s/%s", RESOLVABLE_URI, collection.getName(), COUNT_FORMAT.format(recordCount)));
                            }
                            Collection<Object> objectUrls = solrInputDocument.getFieldValues("europeana_object");
                            if (objectUrls != null) {
                                for (Object object : objectUrls) {
                                    String url = (String) object;
                                    fetchScript.write(objectCache.createFetchScriptItem(collection.getName(), europeanaId.getEuropeanaUri(), url));
                                    fetchScript.flush();
                                }
                            }
                            else if ("true".equals(solrInputDocument.getFieldValue("europeana_hasObject"))) {
                                log.warn("No object urls for "+europeanaId.getEuropeanaUri());
                            }
                            recordList.add(solrInputDocument);
                            dashboardDao.saveEuropeanaId(europeanaId);
                            europeanaId = null;
                            solrInputDocument = null;
                        }
                        break;

                    case XMLStreamConstants.END_DOCUMENT:
                        log.info(String.format("Document ended, imported %d records", recordCount));
                        break;
                }
                if (recordList.size() >= chunkSize) {
                    indexRecordList();
                }
                if (!xml.hasNext()) {
                    break;
                }
                xml.next();
            }
            if (!recordList.isEmpty()) {
                indexRecordList();
            }
            long elapsedMillis = System.currentTimeMillis() - startTime;
            String scriptEnd = objectCache.createFetchScriptEnd(collection.getName(), recordCount, objectCount, elapsedMillis, indexErrorCount);
            fetchScript.write(scriptEnd);
            log.info(scriptEnd);
            inputStream.close();
            fetchScript.close();
        }

        private void indexRecordList() throws IOException, SolrServerException {
            log.info("sending " + recordList.size() + " records to solr");
            try {
                solrServer.add(recordList);
            } catch (SolrServerException e) {
                log.error("unable to index this batch");
                log.error(recordList.toString());
                indexErrorCount++;
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            solrServer.commit();       // It is better to use the  autocommit from solr
            recordList.clear();
        }

        private EuropeanaField getEuropeanaField(String prefix, String localName, int recordCount) throws ImportException {
            EuropeanaField field = null;
            for (EuropeanaField recordField : getEuropeanaBean().getFields()) {
                if (recordField.getPrefix().equals(prefix) && recordField.getLocalName().equals(localName)) {
                    field = recordField;
                    break;
                }
            }
            if (field == null) {
                throw new ImportException("Field not recognized: " + prefix + ":" + localName, recordCount);
            }
            return field;
        }

        private boolean isRecordElement(XMLStreamReader xml) {
            return "record".equals(xml.getName().getLocalPart());
        }

//        private String fetchLanguage(XMLStreamReader xml) {
//            for (int walk = 0; walk < xml.getAttributeCount(); walk++) {
//                if ("xml".equals(xml.getAttributePrefix(walk)) && "lang".equals(xml.getAttributeLocalName(walk))) {
//                    return xml.getAttributeValue(walk);
//                }
//            }
//            return null;
//        }

        @Override
        public ImportFile getFile() {
            return importFile;
        }
    }

    private class ValidationProcessor implements Runnable, Processor {
        private Long collectionId;
        private EuropeanaCollection collection;
        private Thread thread;
        private ImportFile importFile;

        private ValidationProcessor(ImportFile importFile, Long collectionId) {
            this.importFile = importFile;
            this.collectionId = collectionId;
        }

        @Override
        public void run() {
            log.info("Validating " + importFile);
            try {
                collection = dashboardDao.fetchCollection(collectionId);
                if (collection == null) {
                    throw new ImportException("No collection found with id " + collectionId);
                }
                InputStream inputStream = createInputStream(importFile);
                validateXml(inputStream);
                if (thread != null) {
                    log.info("Finished validating " + importFile);
                    transition(ImportFileState.VALIDATED);
                }
                else {
                    log.info("Aborted validating " + importFile);
                    transition(ImportFileState.UPLOADED);
                }
            }
            catch (ImportException e) {
                log.warn("Problem validating " + importFile + ", moving to error directory", e);
                collection = dashboardDao.setImportError(collection.getId(), exceptionToErrorString(e));
                transition(ImportFileState.ERROR);
            }
            finally {
                processors.remove(this);
                thread = null;
            }
        }

        private void validateXml(InputStream inputStream) throws ImportException {
            Source source = new StreamSource(inputStream, "UTF-8");
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            ErrorHandler errorHandler = new ErrorHandler();
            try {
                Schema schema = schemaFactory.newSchema(getClass().getResource("/" + ESE_SCHEMA));
                Validator validator = schema.newValidator();
                validator.setErrorHandler(errorHandler);
                validator.validate(source);
//                try {
//                    Thread.sleep(1000);
//                }
//                catch (InterruptedException e) {
//                    throw new IOException("Just to keep the catch clause below");
//                }
                if (!errorHandler.exceptions.isEmpty()) {
                    throw new ImportException("File is invalid according to " + ESE_SCHEMA, (Throwable) errorHandler.get());
                }
            }
            catch (SAXException e) {
                log.error("Unable to parse ESE schema!");
                throw new RuntimeException("Unable to parse schema");
            }
            catch (IOException e) {
                throw new ImportException("Problem reading file while validating", e);
            }
        }

        @Override
        public ImportFile getFile() {
            return importFile;
        }

        @Override
        public void start() {
            if (this.thread == null) {
                this.thread = new Thread(this);
                thread.setName(importFile.getFileName());
                thread.start();
            }
            else {
                log.warn("Validation processor already started for " + importFile);
            }
        }

        @Override
        public ImportFile stop() {
            if (thread != null) {
                Thread threadToJoin = thread;
                thread = null;
                try {
                    threadToJoin.join();
                }
                catch (InterruptedException e) {
                    log.error("Interrupted!", e);
                }
            }
            return importFile;
        }

        private class ErrorHandler extends DefaultHandler {
            private List<Throwable> exceptions = new ArrayList<Throwable>();

            @Override
            public void error(SAXParseException parseException) throws SAXException {
                exceptions.add(parseException);
            }

            @Override
            public void fatalError(SAXParseException parseException) throws SAXException {
                exceptions.add(parseException);
            }

            public Object get() {
                if (exceptions.isEmpty()) {
                    return null;
                }
                else {
                    return exceptions.get(0);
                }
            }
        }

        private void transition(ImportFileState state) {
            importFile = importRepository.transition(importFile, state);
            collection.setFileState(state);
            collection = dashboardDao.updateCollection(collection);
        }
    }

    private InputStream createInputStream(ImportFile importFile) throws ImportException {
        try {
            InputStream inputStream;
            if (importFile.isXml()) {
                inputStream = new FileInputStream(importRepository.createFile(importFile));
            }
            else if (importFile.isGzipXml()) {
                inputStream = new GZIPInputStream(new FileInputStream(importRepository.createFile(importFile)));
            }
            else {
                throw new ImportException("File is of the wrong type");
            }
            return inputStream;
        }
        catch (FileNotFoundException e) {
            throw new ImportException("File not found: " + importFile, e);
        }
        catch (IOException e) {
            throw new ImportException("Problem while reading: " + importFile, e);
        }
    }

    private static String exceptionToErrorString(ImportException exception) {
        StringBuilder out = new StringBuilder();
        out.append(exception.getMessage());
        Throwable cause = exception.getCause();
        while (cause != null) {
            out.append('\n');
            out.append(cause.toString());
            cause = cause.getCause();
        }
        return out.toString();
    }

    private EuropeanaBean getEuropeanaBean() {
        if (europeanaBean == null) {
            europeanaBean = annotationProcessor.getEuropeanaBean(beanClass);
            if (europeanaBean == null) {
                throw new RuntimeException("Expected to find bean for class "+beanClass);
            }
        }
        return europeanaBean;
    }
}