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
 *   1 Jun 2019 (albrecht): created
 */
package org.knime.js.base.node.base.selection.singleMultiple;

import java.util.Arrays;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The base value for the single and multiple selection configuration and widget node
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class SingleMultipleSelectionNodeValue extends JSONViewContent {

    /**
     * Config setting for the variable value
     */
    public static final String CFG_VARIABLE_VALUE = "variable_value";

    /**
     * Default variable value
     */
    protected static final String[] DEFAULT_VARIABLE_VALUE = new String[0];
    private String[] m_variableValue = DEFAULT_VARIABLE_VALUE;

    /**
     * @return the variableValue
     */
    @JsonProperty("value")
    public String[] getVariableValue() {
        return m_variableValue;
    }

    /**
     * @param variableValue the variableValue to set
     */
    @JsonProperty("value")
    public void setVariableValue(final String[] variableValue) {
        m_variableValue = variableValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void saveToNodeSettings(final NodeSettingsWO settings) {
        settings.addStringArray(CFG_VARIABLE_VALUE, m_variableValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        setVariableValue(settings.getStringArray(CFG_VARIABLE_VALUE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("variableValue=");
        sb.append(Arrays.toString(m_variableValue));
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_variableValue)
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
        SingleMultipleSelectionNodeValue other = (SingleMultipleSelectionNodeValue)obj;
        return new EqualsBuilder()
                .append(m_variableValue, other.m_variableValue)
                .isEquals();
    }

}
