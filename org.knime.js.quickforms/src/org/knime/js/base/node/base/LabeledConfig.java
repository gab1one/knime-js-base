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
 *   2 May 2019 (albrecht): created
 */
package org.knime.js.base.node.base;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
public class LabeledConfig {

    private static final String CFG_LABEL = "label";
    public static final String DEFAULT_LABEL = "Label";
    private String m_label = DEFAULT_LABEL;

    private static final String CFG_DESCRIPTION = "description";
    public static final String DEFAULT_DESCRIPTION = "Enter Description";
    private String m_description = DEFAULT_DESCRIPTION;

    private static final String CFG_REQUIRED = "required";
    public static final boolean DEFAULT_REQUIRED = true;
    private boolean m_required = DEFAULT_REQUIRED;

    /**
     * @return the label
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(final String label) {
        this.m_label = label;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.m_description = description;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return m_required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(final boolean required) {
        m_required = required;
    }

    /**
     * @param settings The settings to save to
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addString(CFG_LABEL, m_label);
        settings.addString(CFG_DESCRIPTION, m_description);
        settings.addBoolean(CFG_REQUIRED, m_required);
    }

    /**
     * @param settings The settings to load from
     * @throws InvalidSettingsException If the settings are not valid
     */
    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_label = settings.getString(CFG_LABEL);
        m_description = settings.getString(CFG_DESCRIPTION);
        m_required = settings.getBoolean(CFG_REQUIRED);
    }

    /**
     * @param settings The settings to load from
     */
    public void loadSettingsInDialog(final NodeSettingsRO settings) {
        m_label = settings.getString(CFG_LABEL, DEFAULT_LABEL);
        m_description = settings.getString(CFG_DESCRIPTION, DEFAULT_DESCRIPTION);
        m_required = settings.getBoolean(CFG_REQUIRED, DEFAULT_REQUIRED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("label=");
        sb.append(m_label);
        sb.append(", ");
        sb.append("description=");
        sb.append(m_description);
        sb.append(", ");
        sb.append("required=");
        sb.append(m_required);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_label)
                .append(m_description)
                .append(m_required)
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
        LabeledConfig other = (LabeledConfig)obj;
        return new EqualsBuilder()
                .append(m_label, other.m_label)
                .append(m_description, other.m_description)
                .append(m_required, other.m_required)
                .isEquals();
    }

}
