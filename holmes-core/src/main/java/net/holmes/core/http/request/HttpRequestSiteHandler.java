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
package net.holmes.core.http.request;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.holmes.core.configuration.IConfiguration;
import net.holmes.core.http.HttpServer;
import net.holmes.core.util.IMimeTypeFactory;
import net.holmes.core.util.LogUtil;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Handler to serve pages of Holmes administration site
 */
public final class HttpRequestSiteHandler implements IHttpRequestHandler {
    private static Logger logger = LoggerFactory.getLogger(HttpRequestSiteHandler.class);

    @Inject
    private IConfiguration configuration;

    @Inject
    private IMimeTypeFactory mimeTypeFactory;

    public HttpRequestSiteHandler() {
    }

    /* (non-Javadoc)
     * @see net.holmes.core.http.request.IHttpRequestHandler#initHandler()
     */
    @Override
    @Inject
    public void initHandler() {
    }

    /* (non-Javadoc)
     * @see net.holmes.core.http.request.IHttpRequestHandler#canProcess(java.lang.String)
     */
    @Override
    public boolean canProcess(String requestPath) {
        return true;
    }

    /* (non-Javadoc)
     * @see net.holmes.core.http.request.IHttpRequestHandler#processRequest(org.jboss.netty.handler.codec.http.HttpRequest, org.jboss.netty.channel.Channel)
     */
    @Override
    public void processRequest(HttpRequest request, Channel channel) throws HttpRequestException {
        if (logger.isDebugEnabled()) {
            logger.debug("[START] processRequest");
            LogUtil.debugHttpRequest(logger, request);
        }

        // Get file name
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        String fileName = decoder.getPath();
        if (fileName.equals("/")) {
            fileName = "/index.html";
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new HttpRequestException("", HttpResponseStatus.NOT_FOUND);
        }

        String filePath = configuration.getHomeSiteDirectory() + fileName;

        if (logger.isDebugEnabled()) logger.debug("file path:" + filePath);

        try {
            // Get file
            File file = new File(filePath);
            if (file == null || !file.exists() || !file.canRead() || file.isHidden()) {
                if (logger.isWarnEnabled()) logger.warn("resource not found:" + fileName);
                throw new HttpRequestException("", HttpResponseStatus.NOT_FOUND);
            }

            // Read the file
            RandomAccessFile raf = new RandomAccessFile(file, "r");

            // Compute HttpHeader
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaders.setContentLength(response, raf.length());
            response.setHeader(HttpHeaders.Names.SERVER, HttpServer.HTTP_SERVER_NAME);
            String mimeType = mimeTypeFactory.getMimeType(fileName).getMimeType();
            if (mimeType != null) {
                response.setHeader(HttpHeaders.Names.CONTENT_TYPE, mimeType);
            }

            // Write the header.
            channel.write(response);

            // Write the file.
            ChannelFuture writeFuture = channel.write(new ChunkedFile(raf, 0, raf.length(), 8192));

            // Decide whether to close the connection or not.
            if (!HttpHeaders.isKeepAlive(request)) {
                // Close the connection when the whole content is written out.
                writeFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }
        catch (FileNotFoundException fnfe) {
            if (logger.isWarnEnabled()) logger.warn("resource not found:" + fileName);
            throw new HttpRequestException("", HttpResponseStatus.NOT_FOUND);
        }
        catch (IOException e) {
            if (logger.isErrorEnabled()) logger.error(e.getMessage(), e);
            throw new HttpRequestException("", HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        finally {
            if (logger.isDebugEnabled()) logger.debug("[END] processRequest");
        }
    }
}