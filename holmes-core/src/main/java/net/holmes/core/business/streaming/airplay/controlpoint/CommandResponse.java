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

package net.holmes.core.business.streaming.airplay.controlpoint;

/**
 * Control point command response
 */

import com.google.common.base.Splitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Iterables.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * Command response.
 */
public final class CommandResponse {
    private static final String EOL = "\n";
    private static final char PARAMETER_SEPARATOR = ':';
    private static final Pattern HTTP_RESPONSE_PATTERN = Pattern.compile("^(.*)\\s(\\d+)\\s(.*)$");

    private int code;
    private String message;

    private final Map<String, String> httpHeaders = new HashMap<>();
    private final Map<String, String> contentParameters = new HashMap<>();

    /**
     * Gt response code.
     *
     * @return response code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get response message.
     *
     * @return response message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get content parameters.
     *
     * @return content parameters
     */
    public Map<String, String> getContentParameters() {
        return contentParameters;
    }

    /**
     * Get content length.
     *
     * @return content length
     */
    public int getContentLength() {
        return httpHeaders.get(CONTENT_LENGTH) != null ? Integer.valueOf(httpHeaders.get(CONTENT_LENGTH)) : 0;
    }

    /**
     * Get content type.
     *
     * @return content type
     */
    public String getContentType() {
        return httpHeaders.get(CONTENT_TYPE);
    }

    /**
     * Decode Http response.
     *
     * @param responseLines response lines
     */
    public void decodeHttpResponse(final List<String> responseLines) {
        // Decode http response on first line
        Matcher matcher = HTTP_RESPONSE_PATTERN.matcher(responseLines.get(0));
        if (matcher.find()) {
            code = Integer.valueOf(matcher.group(2));
            message = matcher.group(3);
        }

        // Decode http headers on next lines
        for (int i = 1; i < responseLines.size(); i++) {
            Iterable<String> it = Splitter.on(PARAMETER_SEPARATOR).trimResults().split(responseLines.get(i));
            httpHeaders.put(get(it, 0), getLast(it));
        }
    }

    /**
     * Decode content parameters.
     *
     * @param content content
     */
    public void decodeContentParameters(final String content) {
        for (String line : Splitter.on(EOL).split(content)) {
            Iterable<String> it = Splitter.on(PARAMETER_SEPARATOR).trimResults().split(line);
            if (size(it) > 1) {
                contentParameters.put(get(it, 0), getLast(it));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this)
                .add("code", code)
                .add("message", message)
                .add("httpHeaders", httpHeaders)
                .add("contentParameters", contentParameters)
                .toString();
    }
}
