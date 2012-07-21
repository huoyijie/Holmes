/**
* Copyright (c) 2012 Cedric Cheneau
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/
package net.holmes.core.configuration.xml;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.holmes.core.configuration.ConfigurationNode;
import net.holmes.core.configuration.Parameter;

public final class XmlRootNode implements Serializable {
    private static final long serialVersionUID = 1607439493422835211L;

    private String upnpServerName;
    private Integer httpServerPort;
    private LinkedList<ConfigurationNode> videoFolders;
    private LinkedList<ConfigurationNode> pictureFolders;
    private LinkedList<ConfigurationNode> audioFolders;
    private LinkedList<ConfigurationNode> podcasts;
    private Properties parameters;

    public void check() {
        if (this.videoFolders == null) this.videoFolders = new LinkedList<ConfigurationNode>();
        if (this.audioFolders == null) this.audioFolders = new LinkedList<ConfigurationNode>();
        if (this.pictureFolders == null) this.pictureFolders = new LinkedList<ConfigurationNode>();
        if (this.podcasts == null) this.podcasts = new LinkedList<ConfigurationNode>();
        if (this.parameters == null) this.parameters = new Properties();
        for (Parameter param : Parameter.values()) {
            if (this.parameters.getProperty(param.getName()) == null) this.parameters.put(param.getName(), param.getDefaultValue());
        }
    }

    public String getUpnpServerName() {
        return this.upnpServerName;
    }

    public void setUpnpServerName(String upnpServerName) {
        this.upnpServerName = upnpServerName;
    }

    public Integer getHttpServerPort() {
        return this.httpServerPort;
    }

    public void setHttpServerPort(Integer httpServerPort) {
        this.httpServerPort = httpServerPort;
    }

    public List<ConfigurationNode> getVideoFolders() {
        return this.videoFolders;
    }

    public void setVideoFolders(LinkedList<ConfigurationNode> videoFolders) {
        this.videoFolders = videoFolders;
    }

    public List<ConfigurationNode> getPodcasts() {
        return this.podcasts;
    }

    public void setPodcasts(LinkedList<ConfigurationNode> podcasts) {
        this.podcasts = podcasts;
    }

    public List<ConfigurationNode> getAudioFolders() {
        return this.audioFolders;
    }

    public void setAudioFolders(LinkedList<ConfigurationNode> audioFolders) {
        this.audioFolders = audioFolders;
    }

    public List<ConfigurationNode> getPictureFolders() {
        return this.pictureFolders;
    }

    public void setPictureFolders(LinkedList<ConfigurationNode> pictureFolders) {
        this.pictureFolders = pictureFolders;
    }

    public Boolean getParameter(Parameter param) {
        String value = (String) this.parameters.get(param.getName());
        if (value == null) value = param.getDefaultValue();
        return Boolean.parseBoolean(value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("XmlRootNode [upnpServerName=");
        builder.append(upnpServerName);
        builder.append(", httpServerPort=");
        builder.append(httpServerPort);
        builder.append(", videoFolders=");
        builder.append(videoFolders);
        builder.append(", pictureFolders=");
        builder.append(pictureFolders);
        builder.append(", audioFolders=");
        builder.append(audioFolders);
        builder.append(", podcasts=");
        builder.append(podcasts);
        builder.append(", parameters=");
        builder.append(parameters);
        builder.append("]");
        return builder.toString();
    }

}
