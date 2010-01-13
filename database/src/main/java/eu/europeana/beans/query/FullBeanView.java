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

package eu.europeana.beans.query;

import eu.europeana.beans.annotation.EuropeanaView;
import eu.europeana.query.BriefDoc;
import eu.europeana.query.DocIdWindowPager;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.FullDoc;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * todo: javadoc
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
@EuropeanaView(facets = false, rows = 3) // todo: this might be the right place for this annotation
public interface FullBeanView {
    DocIdWindowPager getDocIdWindowPager() throws Exception, UnsupportedEncodingException;
    List<? extends BriefDoc> getRelatedItems();
    FullDoc getFullDoc() throws EuropeanaQueryException;
}
