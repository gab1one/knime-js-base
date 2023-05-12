/*
 * ------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------
 *
 * History
 *   14.10.2013 (Christian Albrecht, KNIME AG, Zurich, Switzerland): created
 */
package org.knime.js.base.node.quickform.filter.column;

import java.util.Arrays;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.dialog.DialogNodeValue;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.util.JsonUtil;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

/**
 * Value for the column filter quick form node.
 *
 * @author Patrick Winter, KNIME AG, Zurich, Switzerland
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ColumnFilterQuickFormValue extends JSONViewContent implements DialogNodeValue {

    private static final String CFG_COLUMNS = "columns";

    private static final String[] DEFAULT_COLUMNS = new String[0];

    private String[] m_columns = DEFAULT_COLUMNS;

    private NodeSettings m_settings = null;

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
    public void loadFromNodeSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        setColumns(settings.getStringArray(CFG_COLUMNS));
        try {
            m_settings = (NodeSettings) settings.getNodeSettings("columnFilter");
        } catch (InvalidSettingsException e) {
            m_settings = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void loadFromNodeSettingsInDialog(final NodeSettingsRO settings) {
        setColumns(settings.getStringArray(CFG_COLUMNS, DEFAULT_COLUMNS));
        try {
            m_settings = (NodeSettings) settings.getNodeSettings("columnFilter");
        } catch (InvalidSettingsException e) {
            m_settings = null;
        }
    }

    /**
     * @return the settings
     */
    @JsonProperty("settings")
    @JsonDeserialize(using = NodeSettingsDeserializer.class)
    public NodeSettings getSettings() {
        return m_settings;
    }

    /**
     * @param settings the settings to set
     */
    @JsonProperty("settings")
    @JsonSerialize(using = NodeSettingsSerializer.class)
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
            DataColumnSpecFilterConfiguration config = new DataColumnSpecFilterConfiguration("columnFilter");
            config.loadConfigurationInDialog(m_settings, spec);
            setColumns(config.applyTo(spec).getIncludes());
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
     * @param columns the columns to set
     */
    @JsonProperty("columns")
    public void setColumns(final String[] columns) {
        m_columns = columns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        ColumnFilterQuickFormValue other = (ColumnFilterQuickFormValue)obj;
        return new EqualsBuilder()
                .append(m_columns, other.m_columns)
                .append(m_settings, other.m_settings)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFromString(final String fromCmdLine) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Parameterization of ColumnFilter not supported!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFromJson(final JsonValue json) throws JsonException {
        if (json instanceof JsonArray) {
            JsonArray array = (JsonArray) json;
            m_columns = new String[array.size()];
            for (int i = 0; i < array.size(); i++) {
                m_columns [i] = array.getString(i);
            }
        } else if (json instanceof JsonObject) {
            try {
                JsonValue val = ((JsonObject) json).get(CFG_COLUMNS);
                if (JsonValue.NULL.equals(val)) {
                    m_columns = null;
                } else {
                    JsonArray array = ((JsonObject) json).getJsonArray(CFG_COLUMNS);
                    m_columns = new String[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        m_columns [i] = array.getString(i);
                    }
                }
            } catch (Exception e) {
                throw new JsonException("Expected valid string array for key '" + CFG_COLUMNS + ".", e);
            }
        } else {
            throw new JsonException("Expected JSON object or JSON array, but got " + json.getValueType());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        JsonObjectBuilder builder = JsonUtil.getProvider().createObjectBuilder();
        if (m_columns == null) {
            builder.addNull(CFG_COLUMNS);
        } else {
            JsonArrayBuilder arrayBuilder = JsonUtil.getProvider().createArrayBuilder();
            for (String col : m_columns) {
                arrayBuilder.add(col);
            }
            builder.add(CFG_COLUMNS, arrayBuilder);
        }
        return builder.build();
    }
}
