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
 *   27 May 2019 (albrecht): created
 */
package org.knime.js.base.node.base.filter.column;

import java.util.Arrays;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.filter.NameFilterConfiguration.EnforceOption;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The base value for the column filter configuration and widget node
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ColumnFilterNodeValue extends JSONViewContent {

    /**
     * Config setting for the columns value
     */
    public static final String CFG_COLUMNS = "columns";

    /**
     * Default column value
     */
    protected static final String[] DEFAULT_COLUMNS = new String[0];

    /**
     * The columns included by the column filter.
     */
    protected String[] m_columns = DEFAULT_COLUMNS;

    private NodeSettings m_settings = null;

    private static NodeSettings createDefaultSettings() {
        final NodeSettings settings = new NodeSettings(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
        final DataColumnSpecFilterConfiguration filterConfig =
            new DataColumnSpecFilterConfiguration(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
        filterConfig.saveConfiguration(settings);
        return settings;
    }

    /**
     * Creates a new column filter value that does not initialize its filter settings.
     * @deprecated as of KNIME AP 4.1.0 use the {@link ColumnFilterNodeValue#ColumnFilterNodeValue(boolean)} instead
     */
    @Deprecated
    public ColumnFilterNodeValue() {
        this(false);
    }

    /**
     * Creates a new column filter value that optionally initializes its column filter settings.
     * Prior to 4.1.0 these were not initialized in the constructor which caused the Column Filter Configuration
     * and Widget nodes to filter out all columns if the user did not open and save the dialog.
     * @param initializeFilterSettings whether the filter settings should be initialized
     */
    public ColumnFilterNodeValue(final boolean initializeFilterSettings) {
        if (initializeFilterSettings) {
            m_settings = createDefaultSettings();
        }
    }

    /**
     * @return the columns
     */
    @JsonProperty("columns")
    public String[] getColumns() {
        return m_columns;
    }

    /**
     * Updates which columns are included.<br/>
     * NOTE: Also updates the column filter settings used to display the column filter in the node dialog.
     * This means that the column filter switches to standard mode and its include list matches the columns
     * provided as argument to this method.
     *
     * @param columns the columns to include
     */
    @JsonProperty("columns")
    public void setColumns(final String[] columns) {
        m_columns = columns;
        final DataColumnSpecFilterConfiguration config =
                new DataColumnSpecFilterConfiguration(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
        config.loadDefaults(columns, null, EnforceOption.EnforceInclusion);
        if (m_settings == null) {
            m_settings = createDefaultSettings();
        }
        config.saveConfiguration(m_settings);
    }

    /**
     * @return the settings
     */
    @JsonIgnore
    public NodeSettings getSettings() {
        return m_settings;
    }

    /**
     * @param settings the settings to set
     */
    @JsonIgnore
    public void setSettings(final NodeSettings settings) {
        m_settings = settings;
    }

    /**
     * Updates the selection based on the settings and the given spec.
     *
     * @param spec The current table spec
     */
    @JsonIgnore
    public void updateFromSpec(final DataTableSpec spec) {
        if (m_settings != null) {
            DataColumnSpecFilterConfiguration config =
                new DataColumnSpecFilterConfiguration(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
            config.loadConfigurationInDialog(m_settings, spec);
            m_columns = config.applyTo(spec).getIncludes();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void saveToNodeSettings(final NodeSettingsWO settings) {
        settings.addStringArray(CFG_COLUMNS, getColumns());
        if (m_settings != null) {
            settings.addNodeSettings(m_settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_columns = settings.getStringArray(CFG_COLUMNS);
        try {
            m_settings = (NodeSettings) settings.getNodeSettings(ColumnFilterNodeConfig.CFG_COLUMN_FILTER);
        } catch (InvalidSettingsException e) {
            m_settings = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("columns=");
        sb.append(Arrays.toString(m_columns));
        sb.append(", ");
        sb.append("settings=");
        sb.append("{");
        sb.append(m_settings);
        sb.append("}");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_columns)
                .append(m_settings)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ColumnFilterNodeValue other = (ColumnFilterNodeValue)obj;
        return new EqualsBuilder()
                .append(m_columns, other.m_columns)
                .append(m_settings, other.m_settings)
                .isEquals();
    }

}
