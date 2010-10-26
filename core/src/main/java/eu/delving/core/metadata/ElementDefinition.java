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

package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Defines the root of a hierarchical model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("element")
public class ElementDefinition {

    @XStreamAsAttribute
    public String tag;

    @XStreamImplicit
    public List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

    @XStreamImplicit
    public List<ElementDefinition> elements = new ArrayList<ElementDefinition>();

    public FieldDefinition getField(String path) {
        int slash = path.indexOf("/");
        if (slash < 0) {
            if (fields != null) {
                for (FieldDefinition fieldDefinition : fields) {
                    if (path.equals(fieldDefinition.getTag())) {
                        return fieldDefinition;
                    }
                }
            }
        }
        else {
            if (elements != null) {
                String tag = path.substring(0, slash);
                for (ElementDefinition node : elements) {
                    if (tag.equals(node.tag)) {
                        return node.getField(path.substring(slash + 1));
                    }
                }
            }
        }
        return null;
    }

    public FieldDefinition getFieldDefinition(String prefix, String localName) {
        if (fields != null) {
            for (FieldDefinition fieldDefinition : fields) {
                if (prefix.equals(fieldDefinition.prefix) && localName.equals(fieldDefinition.localName)) {
                    return fieldDefinition;
                }
            }
        }
        if (elements != null) {
            for (ElementDefinition elementDefinition : elements) {
                FieldDefinition def = elementDefinition.getFieldDefinition(prefix, localName);
                if (def != null) {
                    return def;
                }
            }
        }
        return null;
    }

    public void getConstantFields(String path, Map<String, FieldDefinition> map) {
        if (fields != null) {
            for (FieldDefinition fieldDefinition : fields) {
                if (fieldDefinition.constant) {
                    map.put(String.format("%s/%s", path, fieldDefinition.getTag()), fieldDefinition);
                }
            }
        }
        if (elements != null) {
            for (ElementDefinition elementDefinition : elements) {
                elementDefinition.getConstantFields(String.format("%s/%s", path, elementDefinition.tag), map);
            }
        }
    }


    public void getCategoryFields(String category, List<FieldDefinition> fieldDefinitions) {
        if (fields != null) {
            for (FieldDefinition fieldDefinition : fields) {
                if (category.equals(fieldDefinition.category)) {
                    fieldDefinitions.add(fieldDefinition);
                }
            }
        }
        if (elements != null) {
            for (ElementDefinition elementDefinition : elements) {
                elementDefinition.getCategoryFields(category, fieldDefinitions);
            }
        }
    }

    public void getFieldNames(List<String> fieldNames) {
        if (fields != null) {
            for (FieldDefinition fieldDefinition : fields) {
                fieldNames.add(fieldDefinition.getFieldNameString());
            }
        }
        if (elements != null) {
            for (ElementDefinition elementDefinition : elements) {
                elementDefinition.getFieldNames(fieldNames);
            }
        }
    }

    public void getFacetMap(Map<String, String> facetMap) {
        if (fields != null) {
            for (FieldDefinition fieldDefinition : fields) {
                if (fieldDefinition.facetPrefix != null) {
                    facetMap.put(fieldDefinition.getFacetName(), fieldDefinition.facetPrefix);
                }
            }
        }
        if (elements != null) {
            for (ElementDefinition elementDefinition : elements) {
                elementDefinition.getFacetMap(facetMap);
            }
        }
    }

    public void getFacetFieldStrings(List<String> facetFieldStrings) {
        if (fields != null) {
            for (FieldDefinition fieldDefinition : fields) {
                if (fieldDefinition.facetPrefix != null) {
                    facetFieldStrings.add(String.format("{!ex=%s}%s", fieldDefinition.facetPrefix, fieldDefinition.getFacetName()));
                }
            }
        }
        if (elements != null) {
            for (ElementDefinition elementDefinition : elements) {
                elementDefinition.getFacetFieldStrings(facetFieldStrings);
            }
        }
    }

    public void getFieldStrings(List<String> fieldStrings) {
        if (fields != null) {
            for (FieldDefinition fieldDefinition : fields) {
                if (fieldDefinition.facetPrefix != null) {
                    fieldStrings.add(fieldDefinition.getFacetName());
                }
                else {
                    fieldStrings.add(fieldDefinition.getFieldNameString());
                }
            }
        }
        if (elements != null) {
            for (ElementDefinition elementDefinition : elements) {
                elementDefinition.getFieldStrings(fieldStrings);
            }
        }
    }

    public String toString() {
        return String.format("Element(%s)", tag);
    }
}