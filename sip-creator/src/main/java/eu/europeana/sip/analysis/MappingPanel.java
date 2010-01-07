package eu.europeana.sip.analysis;

import eu.europeana.query.RecordField;
import eu.europeana.sip.transform.Transform;
import eu.europeana.sip.transform.TransformException;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * The panel which shows statistics and allows for setting up the mapping
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class MappingPanel extends JPanel {
    private static final String NO_TABS = "NO_TABS";
    private static final String TABS = "TABS";
    private static final String SELECT_INSTRUCTION = "Select an entry above";
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(15,15,15,15);
    private JList eseList = new JList(RecordField.values());
    private DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel();
    private JTable statsTable;
    private JLabel mappingTitle = new JLabel("Mapping", JLabel.CENTER);
    private JLabel statsTitle = new JLabel("Statistics", JLabel.CENTER);
    private JPanel mapToPanel = new JPanel(new CardLayout());
    private JTabbedPane mapToTabbedPane = new JTabbedPane();
    private JButton addMappingButton = new JButton("Add Selected Mapping");
    private Transform selectedTransform;
    private String [] selectedFieldValues;
    private JTextField transformedField = new JTextField(SELECT_INSTRUCTION);
    private MappingTree.Node node;

    public MappingPanel() {
        super(new GridLayout(0,1));
        mappingTitle.setFont(new Font("Serif", Font.BOLD, 22));
        tableColumnModel.addColumn(new TableColumn(0, 70));
        tableColumnModel.getColumn(0).setHeaderValue("Percent");
        tableColumnModel.getColumn(0).setResizable(false);
        tableColumnModel.addColumn(new TableColumn(1, 90));
        tableColumnModel.getColumn(1).setHeaderValue("Count");
        tableColumnModel.getColumn(1).setResizable(false);
        tableColumnModel.addColumn(new TableColumn(2));
        tableColumnModel.getColumn(2).setHeaderValue("Value");
        tableColumnModel.getColumn(2).setPreferredWidth(800);
        statsTable = new JTable(new CounterTableModel(null), tableColumnModel);
        add(createTopPanel());
        add(createBottomPanel());
        eseList.setEnabled(false);
        addMappingButton.setEnabled(false);
    }

    public void setNode(MappingTree.Node node) {
        this.node = node;
        if (node != null) {
            mappingTitle.setText("Mapping for \""+node.getStatistics().getPath().getLastNodeString()+"\"");
            statsTitle.setText("Statistics for \""+node.getStatistics().getPath().getLastNodeString()+"\"");
            statsTable.setModel(new CounterTableModel(node.getStatistics().getCounters()));
            statsTable.setColumnModel(tableColumnModel);
            eseList.setEnabled(true);
            addMappingButton.setEnabled(true);
        }
        else {
            mappingTitle.setText("Mapping");
            statsTitle.setText("Statistics");
            statsTable.setModel(new CounterTableModel(null));
            statsTable.setColumnModel(tableColumnModel);
            eseList.setEnabled(false);
            addMappingButton.setEnabled(false);
        }
    }

    private Component createTopPanel() {
        JPanel p = new JPanel(createBorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(15,15,0,15));
        p.add(mappingTitle, BorderLayout.NORTH);
        p.add(createTopGridPanel(), BorderLayout.CENTER);
        return p;
    }

    private Component createTopGridPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        p.add(createESEListPanel(), BorderLayout.WEST);
        p.add(createMapToPanel(), BorderLayout.CENTER);
        return p;
    }

    private Component createBottomPanel() {
        statsTitle.setFont(new Font("Serif", Font.BOLD, 20));
        statsTable.getTableHeader().setReorderingAllowed(false);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        statsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                updateTransformedField();
            }
        });
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(statsTable.getTableHeader(),BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(statsTable), BorderLayout.CENTER);
        JPanel p = new JPanel(createBorderLayout());
        p.setBorder(EMPTY_BORDER);
        p.add(statsTitle, BorderLayout.NORTH);
        p.add(tablePanel, BorderLayout.CENTER);
        p.add(createTransformedPanel(), BorderLayout.SOUTH);
        return p;
    }

    private Component createTransformedPanel() {
        transformedField.setEditable(false);
        JPanel p = new JPanel(createBorderLayout());
        p.add(new JLabel("Transformed Value:",JLabel.RIGHT), BorderLayout.WEST);
        p.add(transformedField, BorderLayout.CENTER);
        return p;
    }

    private Component createESEListPanel() {
        JScrollPane scroll = new JScrollPane(eseList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        addMappingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                RecordField recordField = (RecordField) eseList.getSelectedValue();
                TransformPanel.Listener listener = new TransformPanel.Listener() {
                    public void transformSelected(Transform transform, String[] fieldValues) {
                        selectedTransform = transform;
                        selectedFieldValues = fieldValues;
                        updateTransformedField();
                    }

                    public void fieldChanged() {
                        updateTransformedField();
                    }
                };
                mapToTabbedPane.addTab(recordField.toString(), new TransformPanel(node, recordField, listener));
                switchCards();
            }
        });
        JPanel p = new JPanel(createBorderLayout());
        p.setBorder(EMPTY_BORDER);
        p.add(new JLabel("ESE Model", JLabel.CENTER), BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        p.add(addMappingButton, BorderLayout.SOUTH);
        return p;
    }

    private void switchCards() {
        String card = (mapToTabbedPane.getTabCount() == 0) ? NO_TABS : TABS;
        ((CardLayout)mapToPanel.getLayout()).show(mapToPanel, card);
    }

    private void updateTransformedField() {
        int row = statsTable.getSelectedRow();
        if (row < 0 || selectedTransform == null) {
            transformedField.setText(SELECT_INSTRUCTION);
        }
        else {
            String value = (String) statsTable.getModel().getValueAt(row, 2);
            try {
                String transformed = selectedTransform.transform(value, selectedFieldValues);
                transformedField.setText(transformed);
            }
            catch (TransformException e) {
                transformedField.setText(e.toString()); // todo: better solution
            }
        }
    }

    private Component createMapToPanel() {
        JPanel p = new JPanel(createBorderLayout());
        p.setBorder(EMPTY_BORDER);
        p.add(new JLabel("Map To", JLabel.CENTER), BorderLayout.NORTH);
        mapToPanel.add(new JLabel("Nothing mapped yet", JLabel.CENTER), NO_TABS);
        mapToPanel.add(mapToTabbedPane, TABS);
        p.add(mapToPanel, BorderLayout.CENTER);
        return p;
    }

    private static class CounterTableModel extends AbstractTableModel {
        private List<? extends Statistics.Counter> counterList;

        private CounterTableModel(List<? extends Statistics.Counter> counterList) {
            this.counterList = counterList;
        }

        public int getRowCount() {
            if (counterList == null) {
                return 0;
            }
            return counterList.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public Object getValueAt(int row, int col) {
            Statistics.Counter counter = counterList.get(row);
            switch (col) {
                case 0:
                    return counter.getPercentage();
                case 1:
                    return counter.getCount();
                case 2:
                    return counter.getValue();
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private BorderLayout createBorderLayout() {
        return new BorderLayout(10,10);
    }
}