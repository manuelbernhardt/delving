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

package eu.delving.services.core;

import eu.delving.sip.DataSetState;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class IndexJobRunner {
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private Harvindexer harvindexer;

    @Autowired
    private MetaRepo metaRepo;

    public void runParallelHarvindexing() {
        MetaRepo.DataSet dataSet = metaRepo.getFirstDataSet(DataSetState.QUEUED);
        if (dataSet == null) {
            log.debug("no collection found for indexing");
        }
        else {
            log.info("found collection to index: " + dataSet.getSpec());
            dataSet.setState(DataSetState.INDEXING);
            dataSet.setRecordsIndexed(0);
            dataSet.save();
            harvindexer.commenceImport(dataSet);
        }
    }
}