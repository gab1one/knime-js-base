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
 *   May 31, 2017 (simon): created
 */
package org.knime.js.base.node.base.input.date;

import org.knime.node.parameters.widget.choices.Label;

/**
 * An enumeration that contains different granularity of time to show in the view.
 *
 * @author Simon Schmid, KNIME.com, Konstanz, Germany
 */
public enum GranularityTime {
        /**
         * Show hours and minutes.
         */
        @Label(value = "Minutes", description = "Show hours and minutes.")
        SHOW_MINUTES("Show minutes"),
        /**
         * Show hours, minutes and seconds.
         */
        @Label(value = "Seconds", description = "Show hours, minutes and seconds.")
        SHOW_SECONDS("Show seconds"),
        /**
         * Show hours, minutes, seconds and milliseconds.
         */
        @Label(value = "Milliseconds", description = "Show hours, minutes, seconds and milliseconds.")
        SHOW_MILLIS("Show milliseconds");

    private final String m_name;

    private GranularityTime(final String name) {
        m_name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return m_name;
    }

}
