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
 * ---------------------------------------------------------------------
 *
 * Created on 05.05.2015 by Christian Albrecht
 */
package org.knime.dynamic.js.v30;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.knime.core.internal.NodeDescriptionUtil;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.core.node.NodeLogger;
import org.knime.dynamicjsnode.v30.KnimeNodeDocument;
import org.knime.dynamicnode.v30.DynamicInPort;
import org.knime.dynamicnode.v30.DynamicOption;
import org.knime.dynamicnode.v30.DynamicOutPort;
import org.knime.dynamicnode.v30.DynamicTab;
import org.knime.node.v212.View;
import org.knime.node.v212.Views;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link NodeDescription} for dynamic JavaScript node descriptions introduced with 2.12.
 * It uses XMLBeans to extract the information from the XML file.<br />
 * If assertions are enabled (see {@link KNIMEConstants#ASSERTIONS_ENABLED} it also checks the contents of the XML for
 * against the XML schema and reports errors via the logger.
 *
 * @author Christian Albrecht, KNIME AG, Zurich, Switzerland
 * @since 3.0
 */
public final class DynamicJSNodeDescription30Proxy extends NodeDescription {
    private static final XmlOptions OPTIONS = new XmlOptions();

    private static final NodeLogger logger = NodeLogger.getLogger(DynamicJSNodeDescription30Proxy.class);

    static {
        Map<String, String> namespaceMap = new HashMap<String, String>(1);
        namespaceMap.put("", KnimeNodeDocument.type.getContentModel().getName().getNamespaceURI());
        OPTIONS.setLoadSubstituteNamespaces(namespaceMap);
    }

    private final KnimeNodeDocument m_document;
    private File m_nodeDir;

    /**
     * Creates a new proxy object using the given XML document. If assertions are enabled (see
     * {@link KNIMEConstants#ASSERTIONS_ENABLED} it also checks the contents of the XML for against the XML schema and
     * reports errors via the logger.
     *
     * @param doc the XML document of the node description XML file
     * @throws XmlException if something goes wrong while analyzing the XML structure
     */
    public DynamicJSNodeDescription30Proxy(final Document doc) throws XmlException {
        m_document = KnimeNodeDocument.Factory.parse(doc.getDocumentElement(), OPTIONS);
        validate();
    }

    /**
     * Creates a new proxy object using the given knime node document. If assertions are enabled (see
     * {@link KNIMEConstants#ASSERTIONS_ENABLED} it also checks the contents of the XML for against the XML schema and
     * reports errors via the logger.
     *
     * @param doc a knime node document
     * @param nodeDir the directory in which the node is configured
     */
    public DynamicJSNodeDescription30Proxy(final KnimeNodeDocument doc, final File nodeDir) {
        m_document = doc;
        m_nodeDir = nodeDir;
        if (KNIMEConstants.ASSERTIONS_ENABLED) {
            validate();
        }
    }

    /**
     * Validate against the XML Schema. If violations are found they are reported via the logger as coding problems.
     *
     * @return <code>true</code> if the document is valid, <code>false</code> otherwise
     */
    protected final boolean validate() {
        // this method has default visibility so that we can use it in testcases
        XmlOptions options = new XmlOptions(OPTIONS);
        List<XmlError> errors = new ArrayList<XmlError>();
        options.setErrorListener(errors);
        boolean valid = m_document.validate(options);
        if (!valid) {
            logger.coding("Node description of '" + m_document.getKnimeNode().getName()
                + "' does not conform to the Schema. Violations follow.");
            for (XmlError err : errors) {
                logger.coding(err.toString());
            }
        }
        return valid;
    }

    private static final Pattern ICON_PATH_PATTERN = Pattern.compile("[^\\./]+/\\.\\./");

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconPath() {
        String iconPath = m_document.getKnimeNode().getIcon();
        //construct absolute file path
        iconPath = iconPath.replaceAll("//", "/");
        if (iconPath.startsWith("./")) {
            iconPath = iconPath.substring("./".length());
        }
        if (!iconPath.startsWith("/")) {
            try {
                iconPath = m_nodeDir.getCanonicalPath() + "/" + iconPath;
            } catch (IOException e) {
                iconPath = m_nodeDir.getAbsolutePath() + "/" + iconPath;
            }
            iconPath = iconPath.replaceAll("\\\\", "/");
            Matcher m = ICON_PATH_PATTERN.matcher(iconPath);
            while (m.find()) {
                //TODO this fails if there are folders that start with '.' in the path
                iconPath = iconPath.replaceAll("[^./]+/\\.\\./", "");
                m = ICON_PATH_PATTERN.matcher(iconPath);
            }
        }
        return iconPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInportDescription(final int index) {
        if (m_document.getKnimeNode().getPorts() == null) {
            return null;
        }

        for (DynamicInPort inPort : m_document.getKnimeNode().getPorts().getInPortArray()) {
            if (inPort.getIndex().intValue() == index) {
                return NodeDescriptionUtil.getPrettyXmlText(inPort);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInportName(final int index) {
        if (m_document.getKnimeNode().getPorts() == null) {
            return null;
        }

        for (DynamicInPort inPort : m_document.getKnimeNode().getPorts().getInPortArray()) {
            if (inPort.getIndex().intValue() == index) {
                return inPort.getName();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInteractiveViewName() {
        if (m_document.getKnimeNode().getInteractiveView() != null) {
            return m_document.getKnimeNode().getInteractiveView().getName();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeName() {
        String nodeName = m_document.getKnimeNode().getName();
        if (m_document.getKnimeNode().getDeprecated() && !nodeName.matches("^.+\\s+\\(?[dD]eprecated\\)?$")) {
            return nodeName + " (deprecated)";
        } else {
            return nodeName;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutportDescription(final int index) {
        if (m_document.getKnimeNode().getPorts() == null) {
            return null;
        }

        for (DynamicOutPort outPort : m_document.getKnimeNode().getPorts().getOutPortArray()) {
            if (outPort.getIndex().intValue() == index) {
                return NodeDescriptionUtil.getPrettyXmlText(outPort);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutportName(final int index) {
        if (m_document.getKnimeNode().getPorts() == null) {
            return null;
        }

        for (DynamicOutPort outPort : m_document.getKnimeNode().getPorts().getOutPortArray()) {
            if (outPort.getIndex().intValue() == index) {
                return outPort.getName();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeType getType() {
        try {
            return NodeType.valueOf(m_document.getKnimeNode().getType().toString());
        } catch (IllegalArgumentException ex) {
            logger.error("Unknown node type for " + m_document.getKnimeNode().getName() + ": "
                + m_document.getKnimeNode().getDomNode().getAttributes().getNamedItem("type").getNodeValue(), ex);
            return NodeType.Unknown;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewCount() {
        Views views = m_document.getKnimeNode().getViews();
        return (views == null) ? 0 : views.sizeOfViewArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewDescription(final int index) {
        if (m_document.getKnimeNode().getViews() == null) {
            return null;
        }

        for (View view : m_document.getKnimeNode().getViews().getViewArray()) {
            if (view.getIndex().intValue() == index) {
                return NodeDescriptionUtil.getPrettyXmlText(view);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewName(final int index) {
        if (m_document.getKnimeNode().getViews() == null) {
            return null;
        }

        for (View view : m_document.getKnimeNode().getViews().getViewArray()) {
            if (view.getIndex().intValue() == index) {
                return view.getName();
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setIsDeprecated(final boolean b) {
        super.setIsDeprecated(b);
        m_document.getKnimeNode().setDeprecated(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element getXMLDescription() {
        return (Element)m_document.getKnimeNode().getDomNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getIntro() {
        return Optional.ofNullable(NodeDescriptionUtil.getPrettyXmlText(m_document.getKnimeNode().getFullDescription().getIntro()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DialogOptionGroup> getDialogOptionGroups() {
        var ungroupedOptions = NodeDescriptionUtil.getDirectChildrenOfType(
                m_document.getKnimeNode().getFullDescription().getOptions(),
                DynamicOption.class
        );
        return NodeDescriptionUtil.extractDialogOptionGroups(
                ungroupedOptions,
                m_document.getKnimeNode().getFullDescription().getTabList(),
                DynamicTab::getName,
                DynamicTab::getDescription,
                t -> NodeDescriptionUtil.getDirectChildrenOfType(t.getOptions(), DynamicOption.class),
                DynamicOption::getName,
                DynamicOption::getOptional
        );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getShortDescription() {
        return Optional.ofNullable(NodeDescriptionUtil.normalizeWhitespace(m_document.getKnimeNode().getShortDescription()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getInteractiveViewDescription() {
        return Optional.ofNullable(NodeDescriptionUtil.getPrettyXmlText(m_document.getKnimeNode().getInteractiveView()));
    }
}
