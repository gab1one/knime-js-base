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
 *   Apr 30, 2025 (Paul Bärnreuther): created
 */
package org.knime.js.base.node.configuration.input.string;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import org.junit.jupiter.api.Test;
import org.knime.js.base.node.configuration.IntegratedComponentDialogTestBase;

import com.fasterxml.jackson.core.JsonProcessingException;

class StringConfigurationComponentDialogTest extends IntegratedComponentDialogTestBase {

    @Test
    void testStringConfigurationComponentDialog() throws JsonProcessingException {
        final var dialogData = getComponentDialog(getTopLevelNodeId(2));
        final var paramName = "string-input-3";
        assertThatJson(dialogData.getDataFor(paramName)).inPath("$.string").isString().isEmpty();
        final var schema = dialogData.getSchemaFor(paramName);
        assertThatJson(schema).inPath("$.properties.string.type").isString().isEqualTo("string");
        assertThatJson(schema).inPath("$.properties.string.title").isString().isEqualTo("Default");
        assertThatJson(schema).inPath("$.properties.string.description").isString().isEqualTo("Default string");
        final var uiSchema = dialogData.getUiSchema();
        assertThatJson(uiSchema).inPath("$.elements[0].type").isString().isEqualTo("Control");
        assertThatJson(uiSchema).inPath("$.elements[0].scope").isString()
            .isEqualTo(String.format("#/properties/model/properties/%s/properties/string", paramName));
        assertThatJson(uiSchema).inPath("$.elements[0]").isObject().doesNotContainKey("validations");
    }

    @Test
    void testWithPattern() throws JsonProcessingException {
        final var dialogData = getComponentDialog(getTopLevelNodeId(2));
        final var paramName = "string-input-with-pattern-4";
        final var uiSchema = dialogData.getUiSchema();
        assertThatJson(uiSchema).inPath("$.elements[1].scope").isString()
            .isEqualTo(String.format("#/properties/model/properties/%s/properties/string", paramName));
        assertThatJson(uiSchema).inPath("$.elements[1].options.validation.pattern.parameters.pattern").isString()
            .isEqualTo(".+");
        assertThatJson(uiSchema).inPath("$.elements[1].options.validation.pattern.errorMessage").isString()
            .isEqualTo("The string must match the pattern: .+");
    }

    @Test
    void testWithPatternAndErrorMessage() throws JsonProcessingException {
        final var dialogData = getComponentDialog(getTopLevelNodeId(2));
        final var paramName = "string-input-with-pattern-message-5";
        final var uiSchema = dialogData.getUiSchema();
        assertThatJson(uiSchema).inPath("$.elements[2].scope").isString()
            .isEqualTo(String.format("#/properties/model/properties/%s/properties/string", paramName));
        assertThatJson(uiSchema).inPath("$.elements[2].options.validation.pattern.parameters.pattern").isString()
            .isEqualTo(".+");
        assertThatJson(uiSchema).inPath("$.elements[2].options.validation.pattern.errorMessage").isString()
            .isEqualTo("This is the message (might contain ?-placeholders)");
    }

    @Test
    void testMultiLine() throws JsonProcessingException {
        final var dialogData = getComponentDialog(getTopLevelNodeId(2));
        final var paramName = "string-input-multi-line-6";
        assertThatJson(dialogData.getSchemaFor(paramName)).inPath("$.properties.string.type").isString()
            .isEqualTo("string");
        final var uiSchema = dialogData.getUiSchema();
        assertThatJson(uiSchema).inPath("$.elements[3].scope").isString()
            .isEqualTo(String.format("#/properties/model/properties/%s/properties/string", paramName));
        assertThatJson(uiSchema).inPath("$.elements[3].options.format").isString().isEqualTo("textArea");
    }

}
