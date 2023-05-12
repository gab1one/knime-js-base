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
 *   29.09.2014 (Christian Albrecht, KNIME AG, Zurich, Switzerland): created
 */
package org.knime.js.base.node.quickform.input.fileupload;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.dialog.DialogNodeValue;
import org.knime.core.util.JsonUtil;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 *
 * @author Christian Albrecht, KNIME AG, Zurich, Switzerland
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class FileUploadQuickFormValue extends JSONViewContent implements DialogNodeValue {

    private static final String CFG_PATH = "path";
    private static final String DEFAULT_PATH = "";
    private String m_path = DEFAULT_PATH;

    private static final String CFG_PATH_VALID = "pathValid";
    private static final boolean DEFAULT_PATH_VALID = true;
    private boolean m_pathValid = DEFAULT_PATH_VALID;

    private static final String CFG_FILE_NAME = "fileName";
    private static final String DEFAULT_FILE_NAME = "";
    private String m_fileName = DEFAULT_FILE_NAME;

    /**
     * @return the path
     */
    @JsonProperty("path")
    public String getPath() {
        return m_path;
    }

    /**
     * @param path the path to set
     */
    @JsonProperty("path")
    public void setPath(final String path) {
        m_path = path;
    }

    /**
     * @return the pathValid
     */
    @JsonProperty("pathValid")
    public boolean isPathValid() {
        return m_pathValid;
    }

    /**
     * @param pathValid the pathValid to set
     */
    @JsonProperty("pathValid")
    public void setPathValid(final boolean pathValid) {
        m_pathValid = pathValid;
    }

    /**
     * @return the fileName
     */
    @JsonProperty("fileName")
    public String getFileName() {
        return m_fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    @JsonProperty("fileName")
    public void setFileName(final String fileName) {
        m_fileName = fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void saveToNodeSettings(final NodeSettingsWO settings) {
        settings.addString(CFG_PATH, getPath());
        settings.addBoolean(CFG_PATH_VALID, m_pathValid);
        settings.addString(CFG_FILE_NAME, m_fileName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        setPath(settings.getString(CFG_PATH));

        //added with 3.2
        setPathValid(settings.getBoolean(CFG_PATH_VALID, DEFAULT_PATH_VALID));

        // added with 4.2.2
        setFileName(settings.getString(CFG_FILE_NAME, DEFAULT_FILE_NAME));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public void loadFromNodeSettingsInDialog(final NodeSettingsRO settings) {
        setPath(settings.getString(CFG_PATH, DEFAULT_PATH));
        setPathValid(settings.getBoolean(CFG_PATH, DEFAULT_PATH_VALID));
        setFileName(settings.getString(CFG_FILE_NAME, DEFAULT_FILE_NAME));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=");
        sb.append(m_fileName);
        sb.append(", path=");
        sb.append(m_path);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_path)
                .append(m_pathValid)
                .append(m_fileName)
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
        FileUploadQuickFormValue other = (FileUploadQuickFormValue)obj;
        return new EqualsBuilder()
                .append(m_path, other.m_path)
                .append(m_pathValid, other.m_pathValid)
                .append(m_fileName, other.m_fileName)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFromString(final String fromCmdLine) throws UnsupportedOperationException {
        setPath(fromCmdLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFromJson(final JsonValue json) throws JsonException {
        if (json instanceof JsonString) {
            m_path = ((JsonString) json).getString();
        } else if (json instanceof JsonObject) {
            try {
                JsonValue jsonPath = ((JsonObject)json).get(CFG_PATH);
                if (JsonValue.NULL.equals(jsonPath)) {
                    m_path = null;
                } else {
                    m_path = ((JsonObject) json).getString(CFG_PATH);
                }
                JsonValue jsonFileName = ((JsonObject)json).get(CFG_FILE_NAME);
                if (JsonValue.NULL.equals(jsonFileName)) {
                    m_fileName = DEFAULT_FILE_NAME;
                } else {
                    m_fileName = ((JsonObject)json).getString(CFG_FILE_NAME);
                }
            } catch (Exception e) {
                throw new JsonException("Expected path value for key '" + CFG_PATH + "'.", e);
            }
        } else {
            throw new JsonException("Expected JSON object or JSON string, but got " + json.getValueType());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        JsonObjectBuilder builder = JsonUtil.getProvider().createObjectBuilder();
        if (m_path == null) {
            builder.addNull(CFG_PATH);
        } else {
            builder.add(CFG_PATH, m_path);
        }
        if (m_fileName == null) {
            builder.addNull(CFG_FILE_NAME);
        } else {
            builder.add(CFG_FILE_NAME, m_fileName);
        }
        return builder.build();
    }

}
