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

package eu.delving.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * An XStream approach for replacing the annotated beans.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("field")
public class FieldDefinition implements Comparable<FieldDefinition> {

    @XStreamAsAttribute
    public String prefix;

    @XStreamAsAttribute
    public String localName;

    @XStreamAsAttribute
    public boolean briefDoc;

    @XStreamAsAttribute
    public boolean fullDoc;

    @XStreamAsAttribute
    public boolean systemField;

    @XStreamAsAttribute
    public String fieldType;

    @XStreamAsAttribute
    public String facetPrefix;

    @XStreamAsAttribute
    public String searchField;

    public Validation validation;

    @XStreamOmitField
    public Path path;

    public String description;

    @XStreamOmitField
    private Tag tag;

    public Tag getTag() {
        if (tag == null) {
            tag = Tag.create(prefix, localName);
        }
        return tag;
    }

    public void setPath(Path path) {
        path.push(getTag());
        this.path = new Path(path);
        path.pop();
    }

    public String getFieldNameString() {
        if (getPrefix() == null) {
            return tag.getLocalName();
        }
        else {
            return getPrefix() + '_' + tag.getLocalName();
        }
    }

    public String getPrefix() {
        return tag.getPrefix();
    }

    public String getLocalName() {
        return tag.getLocalName();
    }

    public String getFacetName() {
        return tag.getLocalName().toUpperCase();
    }

    @Override
    public String toString() {
        return String.format("FieldDefinition(%s)", path);
    }

    @Override
    public int compareTo(FieldDefinition fieldDefinition) {
        return path.compareTo(fieldDefinition.path);
    }

    private Object readResolve() {
        fullDoc = true;
        return this;
    }

    public String addOptionalConverter(String variable) {
        if (validation != null && validation.converter != null) {
            return variable + validation.converter.call;
        }
        else {
            return variable;
        }
    }

    @XStreamAlias("validation")
    public static class Validation {

        @XStreamAsAttribute
        public String factName;

        @XStreamAsAttribute
        public String requiredGroup;

        @XStreamAsAttribute
        public boolean url;

        @XStreamAsAttribute
        public boolean object;

        @XStreamAsAttribute
        public boolean unique;

        @XStreamAsAttribute
        public boolean id;

        @XStreamAsAttribute
        public boolean type;

        @XStreamAsAttribute
        public boolean multivalued;

        @XStreamAsAttribute
        public boolean required;

        @XStreamOmitField
        public FactDefinition factDefinition;

        public Converter converter;

        private Object readResolve() {
            multivalued = true;
            required = true;
            return this;
        }
    }

    public static class Converter {

        @XStreamAsAttribute
        public boolean multipleOutput;

        @XStreamAsAttribute
        public String call;
    }
}
