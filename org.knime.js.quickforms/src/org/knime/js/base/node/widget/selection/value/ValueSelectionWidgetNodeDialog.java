/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   29 May 2019 (albrecht): created
 */
package org.knime.js.base.node.widget.selection.value;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.js.base.dialog.selection.single.SingleSelectionComponentFactory;
import org.knime.js.base.node.base.selection.value.ColumnType;
import org.knime.js.base.node.base.selection.value.ValueSelectionNodeConfig;
import org.knime.js.base.node.base.selection.value.ValueSelectionNodeValue;
import org.knime.js.base.node.configuration.selection.value.ValueSelectionDialogNodeValue;
import org.knime.js.base.node.widget.ReExecutableWidgetNodeDialog;

/**
 * Node dialog for the value selection widget node
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
public class ValueSelectionWidgetNodeDialog extends ReExecutableWidgetNodeDialog<ValueSelectionNodeValue> {

    private final JComboBox<ColumnType> m_columnType;
    private final JCheckBox m_lockColumn;
    private final ColumnSelectionPanel m_defaultColumnField;
    private final JComboBox<String> m_defaultField;
    private final DefaultComboBoxModel<String> m_defaultModel = new DefaultComboBoxModel<String>();
    private final JComboBox<String> m_type;
    private final JCheckBox m_limitNumberVisOptionsBox;
    private final JSpinner m_numberVisOptionSpinner;

    private final ValueSelectionWidgetConfig m_config;

    private DataTableSpec m_tableSpec;

    /**
     * Constructor, inits fields calls layout routines
     */
    @SuppressWarnings("unchecked")
    public ValueSelectionWidgetNodeDialog() {
        m_config = new ValueSelectionWidgetConfig();
        m_type = new JComboBox<String>(SingleSelectionComponentFactory.listSingleSelectionComponents());
        m_lockColumn = new JCheckBox();
        m_columnType = new JComboBox<ColumnType>(ColumnType.values());
        m_columnType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                updateAvailableColumns();
            }
        });
        m_defaultField = new JComboBox<String>(m_defaultModel);
        m_defaultColumnField = new ColumnSelectionPanel((Border) null, new Class[]{DataValue.class});
        m_defaultColumnField.addItemListener(new ItemListener() {
            /** {@inheritDoc} */
            @Override
            public void itemStateChanged(final ItemEvent ie) {
                Object o = ie.getItem();
                if (o != null) {
                    final String column = m_defaultColumnField.getSelectedColumn();
                    if (column != null) {
                        updateValues(column, m_defaultModel);
                    }
                }
            }
        });
        m_lockColumn.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_defaultColumnField.setEnabled(!m_lockColumn.isSelected());
            }
        });
        m_limitNumberVisOptionsBox = new JCheckBox();
        m_numberVisOptionSpinner = new JSpinner(new SpinnerNumberModel(5, 5, Integer.MAX_VALUE, 1));
        createAndAddTab();
    }

    /**
     * Updates the columns that are available for selection.
     */
    private void updateAvailableColumns() {
        List<DataColumnSpec> specs = new ArrayList<DataColumnSpec>();
        switch ((ColumnType)m_columnType.getSelectedItem()) {
        case String:
            for (DataColumnSpec colSpec : m_tableSpec) {
                if (colSpec.getDomain().hasValues() && colSpec.getType().isCompatible(StringValue.class)) {
                    specs.add(colSpec);
                }
            }
            break;
        case Integer:
            for (DataColumnSpec colSpec : m_tableSpec) {
                if (colSpec.getDomain().hasValues() && colSpec.getType().isCompatible(IntValue.class)) {
                    specs.add(colSpec);
                }
            }
            break;
        case Double:
            for (DataColumnSpec colSpec : m_tableSpec) {
                if (colSpec.getDomain().hasValues() && colSpec.getType().isCompatible(DoubleValue.class)) {
                    specs.add(colSpec);
                }
            }
            break;
        default:
            for (DataColumnSpec colSpec : m_tableSpec) {
                if (colSpec.getDomain().hasValues()) {
                    specs.add(colSpec);
                }
            }
        }
        final DataTableSpec newDTS = new DataTableSpec(specs.toArray(new DataColumnSpec[0]));
        try {
            m_defaultColumnField.update(newDTS, null);
            // If no exception has been thrown there is min 1 column available
            m_defaultColumnField.setSelectedIndex(0);
        } catch (NotConfigurableException e) {
            // newDTS is empty
            m_defaultModel.removeAllElements();
        }
    }

    /**
     * Update the values that are available for selection based on the given column.
     *
     * @param column The column holding the values
     * @param model Model of the Combo Box that gets updated
     */
    private void updateValues(final String column, final DefaultComboBoxModel<String> model) {
        final DataTableSpec spec = m_defaultColumnField.getDataTableSpec();
        DataColumnSpec dcs = spec.getColumnSpec(column);
        model.removeAllElements();
        if (dcs != null) {
            final Set<DataCell> vals = dcs.getDomain().getValues();
            for (final DataCell cell : vals) {
                String value = cell.toString();
                model.addElement(value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getValueString(final NodeSettingsRO settings) throws InvalidSettingsException {
        ValueSelectionDialogNodeValue value = new ValueSelectionDialogNodeValue();
        value.loadFromNodeSettings(settings);
        return "Column: " + value.getColumn() + "\nValue: " + value.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillPanel(final JPanel panelWithGBLayout, final GridBagConstraints gbc) {
        addPairToPanel("Selection Type: ", m_type, panelWithGBLayout, gbc);
        //not adding to panel, until this option is actually needed/useful
        //addPairToPanel("Column Type: ", m_columnType, panelWithGBLayout, gbc);
        addPairToPanel("Lock Column: ", m_lockColumn, panelWithGBLayout, gbc);
        addPairToPanel("Default Column: ", m_defaultColumnField, panelWithGBLayout, gbc);
        addPairToPanel("Default Value: ", m_defaultField, panelWithGBLayout, gbc);

        m_type.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                boolean enabled = SingleSelectionComponentFactory.LIST.equals(m_type.getSelectedItem());
                m_limitNumberVisOptionsBox.setEnabled(enabled);
                m_numberVisOptionSpinner.setEnabled(enabled && m_limitNumberVisOptionsBox.isSelected());
            }
        });
        addPairToPanel("Limit number of visible options: ", m_limitNumberVisOptionsBox, panelWithGBLayout, gbc);
        m_limitNumberVisOptionsBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_numberVisOptionSpinner.setEnabled(m_limitNumberVisOptionsBox.isSelected());
            }
        });
        addPairToPanel("Number of visible options: ", m_numberVisOptionSpinner, panelWithGBLayout, gbc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs) throws NotConfigurableException {
        m_config.loadSettingsInDialog(settings);
        super.loadSettingsFrom(m_config);
        m_tableSpec = (DataTableSpec) specs[0];
        boolean hasValues = false;
        for (DataColumnSpec cspec : m_tableSpec) {
            if (cspec.getDomain().hasValues()) {
                hasValues = true;
                break;
            }
        }
        if (!hasValues) {
            throw new NotConfigurableException("Data does not contain any column with domain values.");
        }
        ValueSelectionNodeConfig valueSelectionConfig = m_config.getValueSelectionConfig();
        m_columnType.setSelectedItem(valueSelectionConfig.getColumnType());
        String selectedDefaultColumn = m_config.getDefaultValue().getColumn();
        if (!selectedDefaultColumn.isEmpty()) {
            m_defaultColumnField.setSelectedColumn(selectedDefaultColumn);
        }
        String selectedDefaultValue = m_config.getDefaultValue().getValue();
        if (selectedDefaultValue != null) {
            m_defaultField.setSelectedItem(selectedDefaultValue);
        }
        m_lockColumn.setSelected(valueSelectionConfig.isLockColumn());
        m_type.setSelectedItem(valueSelectionConfig.getType());
        m_limitNumberVisOptionsBox.setSelected(valueSelectionConfig.isLimitNumberVisOptions());
        m_numberVisOptionSpinner.setValue(valueSelectionConfig.getNumberVisOptions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        if (m_defaultColumnField.getSelectedColumn() == null) {
            throw new InvalidSettingsException("No default column selected");
        }
        saveSettingsTo(m_config);
        ValueSelectionNodeConfig valueSelectionConfig = m_config.getValueSelectionConfig();
        valueSelectionConfig.setColumnType((ColumnType)m_columnType.getSelectedItem());
        m_config.getDefaultValue().setValue((String) m_defaultField.getSelectedItem());
        valueSelectionConfig.setLockColumn(m_lockColumn.isSelected());
        valueSelectionConfig.setFromSpec(m_tableSpec);
        m_config.getDefaultValue().setColumn(m_defaultColumnField.getSelectedColumn());
        valueSelectionConfig.setType((String)m_type.getSelectedItem());
        valueSelectionConfig.setLimitNumberVisOptions(m_limitNumberVisOptionsBox.isSelected());
        valueSelectionConfig.setNumberVisOptions((Integer)m_numberVisOptionSpinner.getValue());
        m_config.saveSettings(settings);
    }
}
