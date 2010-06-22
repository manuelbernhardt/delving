package eu.delving.metarepo.core;

import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface MetaRepo {

    DataSet createDataSet(String spec, String name, String providerName, String description);

    Map<String, ? extends DataSet> getDataSets();

    List<MetadataFormat> getMetadataFormats();

    List<MetadataFormat> getMetadataFormats(String id);

    HarvestStep getHarvestStep(String resumptionToken);

    HarvestStep getHarvestStep(PmhRequest request);

    Record getRecord(String identifier, String metadataFormat);

    public interface DataSet {
        String setSpec();
        String setName();
        String providerName();
        String description();
        Record fetch(ObjectId id);
        void parseRecords(InputStream inputStream, QName recordRoot, QName uniqueElement) throws XMLStreamException, IOException;
        void setMapping(String mappingName, String mapping);
        List<? extends Record> records(int start, int count);
        
        String DATASET_SPEC = "dataset_spec";
        String DATASET_NAME = "dataset_name";
        String DATASET_PROVIDER_NAME = "dataset_provider_name";
        String DATASET_DESCRIPTION = "dataset_desciption";
        String DATASET_NAMESPACES = "dataset_namespaces";
        String DATASET_MAPPINGS = "mappings";
        String DATASET_MAPPING_TO_ESE = "to-ese";
        
    }

    public interface HarvestStep {
        ObjectId resumptionToken(); // is resumptionToken
        Date expiration();
        int listSize();
        int cursor();
        List<? extends Record> records();
        PmhRequest pmhRequest();
        boolean hasNext();
        HarvestStep next();
    }

    public interface Record {
        ObjectId identifier();
        DBObject rootObject();
        PmhSet set();
        Date modified();
        boolean deleted();
        // todo how to deal with formats
        String format();
        String xml();

        String MODIFIED = "mod";
        String UNIQUE = "uniq";
        String ORIGINAL = "orig";
    }

    public interface PmhRequest {
        PmhVerb getVerb();
        String getSet();
        String getFrom();
        String getUntil();
        String getMetadataPrefix();
        String getIdentifier(); // Only used GetRecord
    }

    public interface PmhSet {
        String getSetSpec();
        String getSetName();
    }

    public interface MetadataFormat {
        String getMetadataPrefix();
        String getSchema();
        String getMetadataNameSpace();
    }

    public enum PmhVerb {
        LIST_SETS("ListSets"),
        List_METADATA_FORMATS("ListMetadataFormats"),
        LIST_IDENTIFIERS("ListIdentifiers"),
        LIST_RECORDS("ListRecords"),
        GET_RECORD("GetRecord"),
        IDENTIFY("Identify");

        private String pmhCommand;

        PmhVerb(String pmhCommand) {
            this.pmhCommand = pmhCommand;
        }

        public String getPmhCommand() {
            return pmhCommand;
        }
    }
    
    String DATABASE_NAME = "MetaRepo";
    String RECORD_COLLECTION_PREFIX = "Records_";
    String DATASETS_COLLECTION = "Datasets";
    String HARVEST_STEPS_COLLECTION = "HarvestSteps";
    String MONGO_ID = "_id";
}


