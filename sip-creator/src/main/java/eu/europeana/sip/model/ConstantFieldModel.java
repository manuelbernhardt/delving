/*
 * Copyright 2007 EDL FOUNDATION
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

package eu.europeana.sip.model;

import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.definitions.annotations.EuropeanaField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Hold a collection of global fields that can be used here and there.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ConstantFieldModel {
    private static final String PREFIX = "// ConstantField ";
    private List<String> fields = new ArrayList<String>();
    private Map<String, String> map = new TreeMap<String, String>();
    private Listener listener;

    public interface Listener {
        void updatedConstant();
    }

    public static ConstantFieldModel fromMapping(String mapping, AnnotationProcessor annotationProcessor) {
        ConstantFieldModel model = new ConstantFieldModel(annotationProcessor, null);
        for (String line : mapping.split("\n")) {
            model.fromLine(line);
        }
        return model;
    }

    public ConstantFieldModel(AnnotationProcessor annotationProcessor, Listener listener) {
        fields.add("collectionId");
        for (EuropeanaField field : annotationProcessor.getAllFields()) {
            if (field.europeana().constant()) {
                fields.add(field.getFieldNameString());
            }
        }
        this.listener = listener;
    }

    public boolean fromLine(String line) {
        if (line.startsWith(PREFIX)) {
            line = line.substring(PREFIX.length());
            int space = line.indexOf(" ");
            if (space > 0) {
                String fieldName = line.substring(0, space);
                String value = line.substring(space).trim();
                set(fieldName, value);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public List<String> getFields() {
        return fields;
    }

    public void clear() {
        map.clear();
        fireUpdate();
    }

    public void set(String field, String value) {
        String oldValue = map.get(field);
        if (oldValue == null || !oldValue.equals(value)) {
            map.put(field, value);
            fireUpdate();
        }
    }

    private void fireUpdate() {
        if (listener != null) {
            listener.updatedConstant();
        }
    }

    public String get(String fieldName) {
        String value = map.get(fieldName);
        if (value != null) {
            return value;
        }
        else {
            return "";
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (String field : fields) {
            String value = map.get(field);
            if (value == null) {
                value = "";
            }
            out.append(PREFIX).append(field).append(' ').append(value).append('\n');
        }
        return out.toString();
    }
}