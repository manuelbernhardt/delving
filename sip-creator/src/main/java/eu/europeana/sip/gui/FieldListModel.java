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

package eu.europeana.sip.gui;

import eu.europeana.core.querymodel.annotation.AnnotationProcessor;
import eu.europeana.core.querymodel.annotation.EuropeanaField;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Given an annotation processor, provide food for the JList to show fields
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldListModel extends AbstractListModel {
    private static final long serialVersionUID = 939393939;
    private List<EuropeanaField> list;

    private FieldListModel(AnnotationProcessor annotationProcessor) {
        this.list = new ArrayList<EuropeanaField>(annotationProcessor.getMappableFields());
        Collections.sort(list, new Comparator<EuropeanaField>() {
            @Override
            public int compare(EuropeanaField field0, EuropeanaField field1) {
                return field0.getFieldNameString().compareTo(field1.getFieldNameString());
            }
        });
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public Object getElementAt(int index) {
        return list.get(index);
    }

    public static class EuropeanaFieldCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            EuropeanaField europeanaField = (EuropeanaField) value;
            return super.getListCellRendererComponent(list, europeanaField.getFieldNameString(), index, isSelected, cellHasFocus);
        }
    }

}