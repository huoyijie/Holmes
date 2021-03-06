/*
 * Copyright (C) 2012-2015  Cedric Cheneau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.holmes.core.business.configuration.dao;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import net.holmes.core.business.configuration.exception.UnknownNodeException;
import net.holmes.core.business.configuration.model.ConfigurationNode;
import net.holmes.core.business.media.model.RootNode;
import net.holmes.core.common.ConfigurationParameter;
import net.holmes.core.common.exception.HolmesRuntimeException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static net.holmes.core.common.ConfigurationParameter.PARAMETERS;

/**
 * XML configuration dao implementation.
 */
@Singleton
public final class XmlConfigurationDaoImpl implements ConfigurationDao {

    private static final String CONF_FILE_NAME = "config.xml";
    private static final String CONF_DIR = "conf";

    private final String localHolmesDataDir;
    private final XStream xstream;

    private XmlRootNode rootNode = null;

    /**
     * Instantiates a new xml configuration.
     *
     * @param localHolmesDataDir local Holmes data directory
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Inject
    public XmlConfigurationDaoImpl(@Named("localHolmesDataDir") final String localHolmesDataDir) throws IOException {
        super();
        this.localHolmesDataDir = localHolmesDataDir;

        // Instantiates a new XStream
        this.xstream = new XStream(new DomDriver(UTF_8.name()));

        // Define XStream aliases
        this.xstream.alias("config", XmlRootNode.class);
        this.xstream.alias("node", ConfigurationNode.class);
        this.xstream.ignoreUnknownElements();

        // Load configuration
        loadConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigurationNode> getNodes(final RootNode rootNode) {
        return this.rootNode.getConfigurationNodes(rootNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationNode getNode(final RootNode rootNode, final String nodeId) throws UnknownNodeException {
        return this.rootNode.getConfigurationNodes(rootNode).stream()
                .filter(node -> node.getId().equals(nodeId))
                .findFirst()
                .orElseThrow(() -> new UnknownNodeException(nodeId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ConfigurationNode> findNode(final RootNode rootNode, final String excludedNodeId, final String label, final String path) {
        return this.rootNode.getConfigurationNodes(rootNode).stream().filter(node -> {
            if (excludedNodeId != null && excludedNodeId.equals(node.getId())) {
                return false;
            } else if (node.getLabel().equals(label) || node.getPath().equals(path)) {
                return true;
            }
            return false;
        }).findFirst();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void save() throws IOException {
        try (OutputStream out = new FileOutputStream(getConfigFile().toFile())) {
            // Save configuration to XML
            xstream.toXML(rootNode, out);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getParameter(final ConfigurationParameter<T> parameter) {
        String value = this.rootNode.getParameter(parameter.getName());
        return value != null ? parameter.parse(value) : parameter.getDefaultValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void setParameter(final ConfigurationParameter<T> parameter, final T value) {
        this.rootNode.setParameter(parameter.getName(), parameter.format(value));
    }

    /**
     * Get Holmes configuration file path.
     *
     * @return configuration file path
     */
    private Path getConfigFile() {
        Path confPath = Paths.get(localHolmesDataDir, CONF_DIR);
        if (Files.isDirectory(confPath) || confPath.toFile().mkdirs()) {
            return Paths.get(confPath.toString(), CONF_FILE_NAME);
        }

        throw new HolmesRuntimeException("Failed to create " + confPath);
    }

    /**
     * Load configuration from Xml file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void loadConfig() throws IOException {
        boolean configLoaded = false;

        Path confFile = getConfigFile();
        if (Files.isReadable(confFile)) {
            try (InputStream in = new FileInputStream(confFile.toFile())) {
                // Load configuration from XML
                rootNode = (XmlRootNode) xstream.fromXML(in);
                configLoaded = true;
            }
        }

        if (rootNode == null) {
            rootNode = new XmlRootNode();
        }
        rootNode.checkDefaultValues();
        rootNode.checkParameters();

        // Save default config if nothing is loaded
        if (!configLoaded) {
            save();
        }
    }

    /**
     * Xml root node: result of Xml configuration deserialization
     */
    private static final class XmlRootNode {
        private Properties parameters;
        private List<ConfigurationNode> videoFolders;
        private List<ConfigurationNode> pictureFolders;
        private List<ConfigurationNode> audioFolders;

        /**
         * Check config default values.
         */
        public void checkDefaultValues() {
            if (this.videoFolders == null) {
                this.videoFolders = new ArrayList<>(0);
            }
            if (this.audioFolders == null) {
                this.audioFolders = new ArrayList<>(0);
            }
            if (this.pictureFolders == null) {
                this.pictureFolders = new ArrayList<>(0);
            }
            if (this.parameters == null) {
                this.parameters = new Properties();
            }
        }

        /**
         * Check configuration parameters.
         */
        @SuppressWarnings("unchecked")
        public void checkParameters() {
            // If a parameter is not present in configuration, add parameter with default value
            PARAMETERS.stream()
                    .filter(param -> this.parameters.getProperty(param.getName()) == null)
                    .forEach(param -> this.parameters.put(param.getName(), param.format(param.getDefaultValue())));

            // Get available parameters
            List<String> availableParams = PARAMETERS.stream()
                    .map(ConfigurationParameter::getName)
                    .collect(toList());

            // Get obsolete parameters
            List<String> obsoleteParams = this.parameters.keySet().stream()
                    .filter(paramKey -> !availableParams.contains(paramKey.toString()))
                    .map(Object::toString)
                    .collect(toList());

            // Remove obsolete parameters
            obsoleteParams.forEach(this.parameters::remove);
        }

        /**
         * Gets configuration nodes.
         *
         * @param rootNode root node
         * @return configuration nodes corresponding to root node
         */
        public List<ConfigurationNode> getConfigurationNodes(final RootNode rootNode) {
            List<ConfigurationNode> configurationNodes;
            switch (rootNode) {
                case AUDIO:
                    configurationNodes = this.audioFolders;
                    break;
                case PICTURE:
                    configurationNodes = this.pictureFolders;
                    break;
                case VIDEO:
                    configurationNodes = this.videoFolders;
                    break;
                default:
                    configurationNodes = new ArrayList<>(0);
                    break;
            }
            return configurationNodes;
        }

        /**
         * Get parameter value.
         *
         * @param parameter parameter name
         * @return parameter value
         */
        public String getParameter(final String parameter) {
            return (String) this.parameters.get(parameter);
        }

        /**
         * Sets the parameter.
         *
         * @param parameter parameter
         * @param value     parameter value
         */
        public void setParameter(final String parameter, final String value) {
            this.parameters.setProperty(parameter, value);
        }
    }
}
