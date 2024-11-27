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
 *   Jun 3, 2019 (Daniel Bogenrieder): created
 */
package org.knime.js.base.node.base.input.fileupload;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.js.base.node.base.LabeledConfig;
import org.knime.js.base.node.base.LabeledNodeRepresentation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The base representation for the file upload configuration and widget node
 *
 * @author Daniel Bogenrieder, KNIME GmbH, Konstanz, Germany
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 * @param <VAL> the value implementation of the node
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class MultipleFileUploadNodeRepresentation<VAL extends MultipleFileUploadNodeValue> extends LabeledNodeRepresentation<VAL> {

    private final String[] m_fileTypes;
    private final String m_errorMessage;
    private final boolean m_disableOutput;
    private final boolean m_allowMultipleFiles;

    /**
     * @param label
     * @param description
     * @param required
     * @param defaultValue
     * @param currentValue
     * @param fileTypes
     * @param errorMessage
     * @param disableOutput
     * @param multiple
     */
    @JsonCreator
    protected MultipleFileUploadNodeRepresentation(@JsonProperty("label") final String label,
        @JsonProperty("description") final String description,
        @JsonProperty("required") final boolean required,
        @JsonProperty("defaultValue") final VAL defaultValue,
        @JsonProperty("currentValue") final VAL currentValue,
        @JsonProperty("fileTypes") final String[] fileTypes,
        @JsonProperty("errorMessage") final String errorMessage,
        @JsonProperty("disableOutput") final boolean disableOutput,
        @JsonProperty("multiple") final boolean multiple) {
        super(label, description, required, defaultValue, currentValue);
        m_fileTypes = filterFileTypes(fileTypes);
        m_errorMessage = errorMessage;
        m_disableOutput = disableOutput;
        m_allowMultipleFiles = multiple;
    }

    /**
     * @param currentValue The value currently used by the node
     * @param defaultValue The default value of the node
     * @param fileUploadConfig The config of the node
     * @param labelConfig The label config of the node
     */
    public MultipleFileUploadNodeRepresentation(final VAL currentValue, final VAL defaultValue,
        final MultipleFileUploadNodeConfig fileUploadConfig, final LabeledConfig labelConfig) {
        super(currentValue, defaultValue, labelConfig);
        m_fileTypes = filterFileTypes(fileUploadConfig.getFileTypes());
        m_errorMessage = fileUploadConfig.getErrorMessage();
        m_disableOutput = fileUploadConfig.getDisableOutput();
        m_allowMultipleFiles = fileUploadConfig.isMultipleFiles();
    }

    private static String[] filterFileTypes(final String[] fileTypes) {
        if (fileTypes.length > 1 && fileTypes[0].contains("|")) {
            return fileTypes[0].split("\\|");
        }
        return fileTypes;
    }

    /**
     * @return the fileTypes
     */
    @JsonProperty("fileTypes")
    public String[] getFileTypes() {
        return m_fileTypes;
    }

    /**
     * @return the errorMessage
     */
    @JsonProperty("errorMessage")
    public String getErrorMessage() {
        return m_errorMessage;
    }

    /**
     * @return the disableOutput
     */
    @JsonProperty("disableOutput")
    public boolean getDisableOutput() {
        return m_disableOutput;
    }

    /**
     * @return singleFile
     */
    @JsonProperty("multiple")
    public boolean isMultiple() {
        return m_allowMultipleFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", ");
        sb.append("fileTypes=");
        sb.append(m_fileTypes);
        sb.append(", ");
        sb.append("errorMessage=");
        sb.append(m_errorMessage);
        sb.append(", ");
        sb.append("disableOutput=");
        sb.append(m_disableOutput);
        sb.append(", ");
        sb.append("multiple=");
        sb.append(m_allowMultipleFiles);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode())
                .append(m_fileTypes)
                .append(m_errorMessage)
                .append(m_disableOutput)
                .append(m_allowMultipleFiles)
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
        @SuppressWarnings("unchecked")
        MultipleFileUploadNodeRepresentation<VAL> other = (MultipleFileUploadNodeRepresentation<VAL>)obj;
        return new EqualsBuilder().appendSuper(super.equals(obj))
                .append(m_fileTypes, other.m_fileTypes)
                .append(m_errorMessage, other.m_errorMessage)
                .append(m_disableOutput, other.m_disableOutput)
                .append(m_allowMultipleFiles, other.m_allowMultipleFiles)
                .isEquals();
    }
}
