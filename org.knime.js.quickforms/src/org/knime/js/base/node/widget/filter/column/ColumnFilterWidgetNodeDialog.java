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
 *   28 May 2019 (albrecht): created
 */
package org.knime.js.base.node.widget.filter.column;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.lang.StringUtils;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;
import org.knime.js.base.dialog.selection.multiple.MultipleSelectionsComponentFactory;
import org.knime.js.base.node.base.filter.column.ColumnFilterNodeConfig;
import org.knime.js.base.node.base.filter.column.ColumnFilterNodeValue;
import org.knime.js.base.node.widget.ReExecutableWidgetNodeDialog;
import org.knime.js.base.node.widget.filter.column.ColumnFilterWidgetNodeModel.Version;

/**
 * Node dialog for the column filter widget node
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
public class ColumnFilterWidgetNodeDialog extends ReExecutableWidgetNodeDialog<ColumnFilterNodeValue> {

    private final DataColumnSpecFilterPanel m_defaultField;
    private final JComboBox<String> m_type;
    private String[] m_possibleColumns;
    private final JCheckBox m_limitNumberVisOptionsBox;
    private final JSpinner m_numberVisOptionSpinner;

    private final ColumnFilterWidgetConfig m_config;

    private final Version m_version;

    /**
     * Constructor, inits fields calls layout routines Creates the dialog for Column Filter Widgets prior to KNIME AP
     * 4.1.
     *
     * @deprecated as of KNIME AP 4.1.0 use {@link ColumnFilterWidgetNodeDialog#ColumnFilterWidgetNodeDialog(Version)}
     *             instead
     */
    @Deprecated
    public ColumnFilterWidgetNodeDialog() {
        this(Version.PRE_4_1);
    }

    /**
     * Constructor, inits fields calls layout routines
     * @param version the version of the Column Filter Widget
     */
    public ColumnFilterWidgetNodeDialog(final Version version) {
        m_version = version;
        m_config = new ColumnFilterWidgetConfig(version);
        m_type = new JComboBox<>(MultipleSelectionsComponentFactory.listMultipleSelectionsComponents());
        m_defaultField = new DataColumnSpecFilterPanel(false);
        m_limitNumberVisOptionsBox = new JCheckBox();
        m_numberVisOptionSpinner = new JSpinner(new SpinnerNumberModel(10, 2, Integer.MAX_VALUE, 1));
        createAndAddTab();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getValueString(final NodeSettingsRO settings) throws InvalidSettingsException {
        ColumnFilterNodeValue value = new ColumnFilterNodeValue(m_version == Version.V_4_1);
        value.loadFromNodeSettings(settings);
        return StringUtils.join(value.getColumns(), ", ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillPanel(final JPanel panelWithGBLayout, final GridBagConstraints gbc) {
        /* This option is hidden because we cannot easily implement it in the sub node dialog
        e.g. if the regex filter from the DataColumnSpecFilterPanel is used we have no equivalent with check boxes */
        // addPairToPanel("Selection Type: ", m_type, panelWithGBLayout, gbc);
        addPairToPanel("Default Values: ", m_defaultField, panelWithGBLayout, gbc);
        addPairToPanel("Limit number of visible options: ", m_limitNumberVisOptionsBox, panelWithGBLayout, gbc);
        addPairToPanel("Number of visible options: ", m_numberVisOptionSpinner, panelWithGBLayout, gbc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        m_config.loadSettingsInDialog(settings);
        loadSettingsFrom(m_config);
        DataTableSpec spec = (DataTableSpec)specs[0];
        m_possibleColumns = spec.getColumnNames();
        NodeSettings filterSettings = m_config.getDefaultValue().getSettings();
        if (filterSettings == null) {
            filterSettings = new NodeSettings(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
        }
        DataColumnSpecFilterConfiguration filterConfig =
            new DataColumnSpecFilterConfiguration(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
        filterConfig.loadConfigurationInDialog(filterSettings, (DataTableSpec)specs[0]);
        m_defaultField.loadConfiguration(filterConfig, (DataTableSpec)specs[0]);
        ColumnFilterNodeConfig config = m_config.getColumnFilterConfig();
        m_type.setSelectedItem(config.getType());
        m_limitNumberVisOptionsBox.setSelected(config.isLimitNumberVisOptions());
        m_numberVisOptionSpinner.setValue(config.getNumberVisOptions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        saveSettingsTo(m_config);
        DataColumnSpecFilterConfiguration filterConfig =
            new DataColumnSpecFilterConfiguration(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
        m_defaultField.saveConfiguration(filterConfig);
        NodeSettings filterSettings = new NodeSettings(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
        filterConfig.saveConfiguration(filterSettings);
        m_config.getDefaultValue().setSettings(filterSettings);
        ColumnFilterNodeConfig config = m_config.getColumnFilterConfig();
        config.setType((String)m_type.getSelectedItem());
        config.setPossibleColumns(m_possibleColumns);
        config.setLimitNumberVisOptions(m_limitNumberVisOptionsBox.isSelected());
        config.setNumberVisOptions((Integer)m_numberVisOptionSpinner.getValue());
        m_config.saveSettings(settings);
    }

}
