/*
 * Copyright (C) 2012-2013  Cedric Cheneau
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

package net.holmes.core;

import com.google.common.collect.Lists;
import net.holmes.core.common.configuration.Configuration;
import net.holmes.core.common.configuration.ConfigurationNode;
import net.holmes.core.common.configuration.Parameter;
import net.holmes.core.media.model.RootNode;

import java.io.File;
import java.net.URL;
import java.util.List;

public class TestConfiguration implements Configuration {

    private final List<ConfigurationNode> videoFolders;
    private final List<ConfigurationNode> pictureFolders;
    private final List<ConfigurationNode> audioFolders;
    private final List<ConfigurationNode> podcasts;

    public TestConfiguration() {
        videoFolders = Lists.newArrayList();
        videoFolders.add(getTestContentFolder("videosTest", "/videosTest/"));
        audioFolders = Lists.newArrayList();
        audioFolders.add(getTestContentFolder("audiosTest", "/audiosTest/"));
        pictureFolders = Lists.newArrayList();
        pictureFolders.add(getTestContentFolder("imagesTest", "/imagesTest/"));
        podcasts = Lists.newArrayList();
        podcasts.add(new ConfigurationNode("castcodersTest", "castcodersTest", "http://lescastcodeurs.libsyn.com/rss"));
    }

    @Override
    public void saveConfig() {
    }

    private ConfigurationNode getTestContentFolder(String label, String path) {
        ConfigurationNode contentFolder = null;

        URL rs = this.getClass().getResource(path);
        if (rs != null) {
            String filePath = new File(rs.getFile()).getAbsolutePath();
            contentFolder = new ConfigurationNode(label, label, filePath);
        }

        return contentFolder;
    }

    @Override
    public String getUpnpServerName() {
        return DEFAULT_UPNP_SERVER_NAME;
    }

    @Override
    public void setUpnpServerName(String upnpServerName) {

    }

    @Override
    public Integer getHttpServerPort() {
        return DEFAULT_HTTP_SERVER_PORT;
    }

    @Override
    public void setHttpServerPort(Integer httpServerPort) {

    }

    @Override
    public List<ConfigurationNode> getFolders(RootNode rootNode) {
        List<ConfigurationNode> folders = null;
        switch (rootNode) {
            case AUDIO:
                folders = this.audioFolders;
                break;
            case PICTURE:
                folders = this.pictureFolders;
                break;
            case PODCAST:
                folders = this.podcasts;
                break;
            case VIDEO:
                folders = this.videoFolders;
                break;
            default:
                break;
        }
        return folders;
    }

    @Override
    public Boolean getParameter(Parameter param) {
        return Boolean.FALSE;
    }

    @Override
    public Integer getIntParameter(Parameter prop) {
        return 0;
    }

    @Override
    public void setParameter(Parameter param, Boolean value) {
    }
}
