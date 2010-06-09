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

package eu.europeana.definitions.annotations;

import java.util.HashSet;
import java.util.Set;

/**
 * Reveal the information about one of the annotated beans
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class EuropeanaBean {
    private Set<EuropeanaField> fields = new HashSet<EuropeanaField>();
    private String[] fieldStrings;

    public Set<EuropeanaField> getFields() {
        return fields;
    }

    public String[] getFieldStrings() {
        if (fieldStrings == null) {
            fieldStrings = new String[fields.size()];
            int index = 0;
            for (EuropeanaField europeanaField : fields) {
                if (!europeanaField.europeana().facetPrefix().isEmpty()) {
                    fieldStrings[index] = europeanaField.getFacetName();
                }
                else {
                    fieldStrings[index] = europeanaField.getFieldNameString();
                }
                index++;
            }
        }
        return fieldStrings;
    }

    public void addField(EuropeanaField field) {
        fields.add(field);
    }
}
