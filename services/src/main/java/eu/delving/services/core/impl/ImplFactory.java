/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.delving.services.core.impl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import eu.delving.metadata.MetadataModel;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.DataSetNotFoundException;
import eu.delving.services.exceptions.MappingNotFoundException;
import eu.delving.sip.AccessKey;
import eu.europeana.sip.core.GroovyCodeResource;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Allow for foreign instantiations
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ImplFactory {
    private Executor executor = Executors.newSingleThreadExecutor();
    private MetaRepo metaRepo;
    private DB db;
    private MetadataModel metadataModel;
    private GroovyCodeResource groovyCodeResource;
    private AccessKey accessKey;
    private int responseListSize = 100;
    private int harvestStepSecondsToLive = 3000;

    public ImplFactory(MetaRepo metaRepo, DB db, MetadataModel metadataModel, GroovyCodeResource groovyCodeResource, AccessKey accessKey) {
        this.metaRepo = metaRepo;
        this.db = db;
        this.metadataModel = metadataModel;
        this.groovyCodeResource = groovyCodeResource;
        this.accessKey = accessKey;
    }

    public void setResponseListSize(int responseListSize) {
        this.responseListSize = responseListSize;
    }

    public void setHarvestStepSecondsToLive(int harvestStepSecondsToLive) {
        this.harvestStepSecondsToLive = harvestStepSecondsToLive;
    }

    public int getResponseListSize() {
        return responseListSize;
    }

    public int getHarvestStepSecondsToLive() {
        return harvestStepSecondsToLive;
    }

    public DBCollection records(String spec) {
        return db.getCollection(MetaRepo.RECORD_COLLECTION_PREFIX + spec);
    }

    public DBCollection dataSets() {
        return db.getCollection(MetaRepo.DATASETS_COLLECTION);
    }

    public DBCollection harvestSteps() {
        return db.getCollection(MetaRepo.HARVEST_STEPS_COLLECTION);
    }

    public MetadataModel getMetadataModel() {
        return metadataModel;
    }

    public GroovyCodeResource getGroovyCodeResource() {
        return groovyCodeResource;
    }

    public AccessKey getAccessKey() {
        return accessKey;
    }

    public MetaRepo.DataSet createDataSet(DBObject object) {
        return new DataSetImpl(this, object);
    }

    public MetaRepo.Mapping createMapping(MetaRepo.DataSet dataSet, DBObject object) {
        return new MappingImpl(this, dataSet, object);
    }

    public MetaRepo.HarvestStep createHarvestStep(DBObject stepObject, String key) throws DataSetNotFoundException, MappingNotFoundException, AccessKeyException {
        HarvestStepImpl step = new HarvestStepImpl(this, stepObject);
        if (step.getId() != null) { // it came from storage
            if (step.getRecordCount() == 0) {
                step.createRecordFetcher(getDataSet(step), key).run();
                if (step.getErrorMessage() != null) {
                    throw new RuntimeException(step.getErrorMessage());
                }
                executor.execute(step.createRecordSaver());
            }
        }
        else { // the step has not yet been stored
            harvestSteps().save(step.getObject());
            step.createRecordFetcher(getDataSet(step), key).run();
            step.createRecordSaver().run();
            step.getObject().put(MetaRepo.HarvestStep.FIRST_ID, step.getObject().get(MetaRepo.MONGO_ID));
            step.createRecordSaver().run();
        }
        return step;
    }

    private MetaRepo.DataSet getDataSet(MetaRepo.HarvestStep step) throws DataSetNotFoundException {
        MetaRepo.DataSet dataSet = metaRepo.getDataSet(step.getPmhRequest().getSet());
        if (dataSet == null) {
            throw new DataSetNotFoundException("Cannot find data set " + step.getPmhRequest().getSet());
        }
        return dataSet;
    }


}

