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

package net.holmes.core.business.streaming.airplay.command;

import io.netty.handler.codec.http.HttpMethod;
import net.holmes.core.business.streaming.device.CommandFailureHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.util.Map.Entry;

/**
 * Airplay command.
 */
public abstract class AirplayCommand {
    private static final String AIRPLAY_USER_AGENT = "MediaControl/1.0";
    private static final String PARAMETER_SEPARATOR = ": ";
    private static final String EOL = "\n";
    private static final String SPACE = " ";

    private final CommandType type;
    private final CommandFailureHandler failureHandler;
    private final Map<UrlParameter, String> urlParameters;
    private final Map<PostParameter, String> postParameters;

    /**
     * Instantiates a new Airplay command.
     *
     * @param type           command type
     * @param failureHandler failure handler
     */
    public AirplayCommand(final CommandType type, final CommandFailureHandler failureHandler) {
        this.type = type;
        this.failureHandler = failureHandler;
        this.urlParameters = new HashMap<>();
        this.postParameters = new LinkedHashMap<>();
    }

    /**
     * Get Http request.
     *
     * @return Http request
     */
    public String getRequest() {
        StringBuilder sbCommand = new StringBuilder();
        String requestContent = getRequestContent();

        // Http command
        sbCommand.append(type.getMethod()).append(SPACE).append(buildRequestUrl()).append(SPACE).append(HTTP_1_1.text()).append(EOL);

        // Http headers
        sbCommand.append(CONTENT_LENGTH).append(PARAMETER_SEPARATOR).append(requestContent == null ? 0 : requestContent.length()).append(EOL);
        sbCommand.append(USER_AGENT).append(PARAMETER_SEPARATOR).append(AIRPLAY_USER_AGENT).append(EOL);

        // Http content
        if (requestContent != null) {
            sbCommand.append(EOL).append(requestContent);
        }

        sbCommand.append(EOL);
        return sbCommand.toString();
    }

    /**
     * Command failure.
     *
     * @param message error message
     */
    public final void failure(final String message) {
        if (failureHandler != null) {
            failureHandler.handle(message);
        }
    }

    /**
     * Success callback.
     *
     * @param contentParameters content parameters map
     */
    public abstract void success(Map<String, String> contentParameters);

    /**
     * Add Url parameter.
     *
     * @param parameter Url parameter
     * @param value     Url parameter value
     */
    void addUrlParameter(final UrlParameter parameter, final String value) {
        urlParameters.put(parameter, value);
    }

    /**
     * Add post parameter.
     *
     * @param parameter post parameter
     * @param value     post parameter value
     */
    void addPostParameter(final PostParameter parameter, final String value) {
        this.postParameters.put(parameter, value);
    }

    /**
     * Build request Url.
     *
     * @return request Url
     */
    private String buildRequestUrl() {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append("/").append(type.getValue());
        if (!urlParameters.isEmpty()) {
            sbUrl.append("?");
            for (Entry<UrlParameter, String> paramEntry : urlParameters.entrySet()) {
                sbUrl.append(paramEntry.getKey().getValue()).append("=").append(paramEntry.getValue()).append("&");
            }
            sbUrl.deleteCharAt(sbUrl.length() - 1);
        }
        return sbUrl.toString();
    }

    /**
     * Get request content.
     *
     * @return request content
     */
    private String getRequestContent() {
        // Build Http content
        if (!postParameters.isEmpty()) {
            StringBuilder sbContent = new StringBuilder();
            for (PostParameter param : postParameters.keySet()) {
                sbContent.append(param.getValue()).append(PARAMETER_SEPARATOR).append(postParameters.get(param)).append(EOL);
            }
            return sbContent.toString();
        } else {
            return null;
        }
    }

    /**
     * Airplay command type
     */
    public enum CommandType {
        PLAY("play", POST),
        PLAY_STATUS("scrub", GET),
        RATE("rate", POST),
        STOP("stop", POST);

        private final String value;
        private final HttpMethod method;

        /**
         * Instantiates a new command type.
         *
         * @param value value
         */
        CommandType(final String value, final HttpMethod method) {
            this.value = value;
            this.method = method;
        }

        /**
         * Get command type value.
         *
         * @return command type value
         */
        public String getValue() {
            return value;
        }

        /**
         * Get HTTP method.
         *
         * @return HTTP method
         */
        public HttpMethod getMethod() {
            return method;
        }
    }

    /**
     * Url parameter
     */
    public enum UrlParameter {
        VALUE("value"),
        POSITION("position");
        private final String value;

        /**
         * Instantiates a new Url parameter
         *
         * @param value value
         */
        UrlParameter(final String value) {
            this.value = value;
        }

        /**
         * Get Url parameter value.
         *
         * @return Url parameter value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Post parameter
     */
    public enum PostParameter {
        CONTENT_LOCATION("Content-Location"),
        START_POSITION("Start-Position");
        private final String value;

        /**
         * Instantiates a new post parameter
         *
         * @param value value
         */
        PostParameter(final String value) {
            this.value = value;
        }

        /**
         * Get post parameter value.
         *
         * @return post parameter value
         */
        public String getValue() {
            return value;
        }
    }
}
