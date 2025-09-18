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
 *   Nov 15, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.js.base.node.base.validation.min.column;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.base.node.base.validation.AbstractValidatorConfig;
import org.knime.js.base.node.base.validation.ValidatorConfig;

/**
 * The {@link ValidatorConfig} for the {@link MinNumColumnsValidatorFactory}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class MinNumColumnsValidatorConfig extends AbstractValidatorConfig {

    public static final String CFG_MIN_NUM_COLUMNS = "min_num_columns";

    public static final int DEFAULT_MIN_NUM_COLUMNS = 0;

    private int m_minNumColumns = 0;

    /**
     * @return the minNumColumns
     */
    int getMinNumColumns() {
        return m_minNumColumns;
    }

    /**
     * @param minNumColumns the minNumColumns to set
     */
    void setMinNumColumns(final int minNumColumns) {
        m_minNumColumns = minNumColumns;
    }

    @Override
    public void saveTo(final NodeSettingsWO settings) {
        super.saveTo(settings);
        settings.addInt(CFG_MIN_NUM_COLUMNS, m_minNumColumns);
    }

    @Override
    public void loadInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadInModel(settings);
        m_minNumColumns = settings.getInt(CFG_MIN_NUM_COLUMNS, DEFAULT_MIN_NUM_COLUMNS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadInDialog(final NodeSettingsRO settings) {
        super.loadInDialog(settings);
        m_minNumColumns = settings.getInt(CFG_MIN_NUM_COLUMNS, DEFAULT_MIN_NUM_COLUMNS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean delegateEquals(final AbstractValidatorConfig config) {
        if (config instanceof MinNumColumnsValidatorConfig) {
            return m_minNumColumns == ((MinNumColumnsValidatorConfig)config).m_minNumColumns;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int delegateHashCode() {
        return 17 + m_minNumColumns;
    }

}