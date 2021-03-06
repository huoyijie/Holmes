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

package net.holmes.core.service.upnp.directory;

import net.holmes.core.business.media.model.ContentNode;
import net.holmes.core.business.media.model.MediaNode;
import net.holmes.core.business.mimetype.model.MimeType;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.DIDLObject.Property.DC;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

import static net.holmes.core.business.mimetype.model.MimeType.*;
import static net.holmes.core.business.streaming.upnp.UpnpUtils.getUpnpMimeType;
import static org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode.CANNOT_PROCESS;

/**
 * UPnP directory browse result.
 */
final class DirectoryBrowseResult {
    private static final String UPNP_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final DIDLObject.Class CONTAINER_CLASS = new DIDLObject.Class("object.container");

    private final DIDLContent didl;
    private final long firstResult;
    private final long maxResults;

    private long itemCount;
    private long totalCount;

    /**
     * Instantiates a new directory browse result.
     *
     * @param firstResult first result
     * @param maxResults  max results
     */
    public DirectoryBrowseResult(final long firstResult, final long maxResults) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.didl = new DIDLContent();
        this.itemCount = 0L;
        this.totalCount = 0L;
    }

    /**
     * Get item count.
     *
     * @return item count
     */
    public long getItemCount() {
        return itemCount;
    }

    /**
     * Get total count.
     *
     * @return total count
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Get first result.
     *
     * @return first result
     */
    public long getFirstResult() {
        return firstResult;
    }

    /**
     * Get DIDL.
     *
     * @return DIDL
     */
    public DIDLContent getDidl() {
        return didl;
    }

    /**
     * Get total result count.
     *
     * @return total result count
     */
    public long getResultCount() {
        return itemCount + firstResult;
    }

    /**
     * Build browse result.
     *
     * @param didlParser DIDL parser
     * @return browse result
     * @throws ContentDirectoryException
     */
    public BrowseResult buildBrowseResult(final DIDLParser didlParser) throws ContentDirectoryException {
        try {
            return new BrowseResult(didlParser.generate(didl), itemCount, totalCount);
        } catch (Exception e) {
            throw new ContentDirectoryException(CANNOT_PROCESS.getCode(), e.getMessage(), e);
        }
    }

    /**
     * Add item to result.
     *
     * @param parentNodeId parent node id
     * @param contentNode  content node
     * @param url          content url
     * @throws ContentDirectoryException
     */
    public void addItem(final String parentNodeId, final ContentNode contentNode, final String url) throws ContentDirectoryException {
        Res res = new Res(getUpnpMimeType(contentNode.getMimeType()), contentNode.getSize(), url);
        addDidlItem(parentNodeId, contentNode, contentNode.getName(), contentNode.getMimeType(), res);
    }

    /**
     * Add item to didl.
     *
     * @param parentNodeId parent node id
     * @param node         node to add
     * @param name         node name
     * @param mimeType     node mimeType
     * @param res          didl resource
     * @throws ContentDirectoryException
     */
    private void addDidlItem(final String parentNodeId, final MediaNode node, final String name, final MimeType mimeType, final Res res) throws ContentDirectoryException {
        Item item;
        switch (mimeType.getType()) {
            case TYPE_VIDEO:
                // Add video item
                item = new Movie(node.getId(), parentNodeId, name, null, res);
                break;
            case TYPE_AUDIO:
                // Add audio track item
                item = new MusicTrack(node.getId(), parentNodeId, name, null, null, (String) null, res);
                break;
            case TYPE_IMAGE:
                // Add image item
                item = new Photo(node.getId(), parentNodeId, name, null, null, res);
                break;
            default:
                item = getUnknownTypeItem(parentNodeId, node, name, mimeType, res);
                break;
        }
        if (item != null) {
            setDidlMetadata(item, node);
            didl.addItem(item);
            itemCount++;
        }
    }

    /**
     * Get DIDL item with unknown mime type.
     *
     * @param parentNodeId parent node id
     * @param node         node to add
     * @param name         node name
     * @param mimeType     node mimeType
     * @param res          didl resource
     * @return DIDL item or null
     */
    private Item getUnknownTypeItem(final String parentNodeId, final MediaNode node, final String name, final MimeType mimeType, final Res res) {
        Item item = null;
        if (mimeType.equals(MIME_TYPE_SUBTITLE)) {
            // Add subtitle item
            item = new TextItem(node.getId(), parentNodeId, name, null, res);
        } else if (mimeType.equals(MIME_TYPE_OGG)) {
            // Add OGG item
            item = new MusicTrack(node.getId(), parentNodeId, name, null, null, (String) null, res);
        }
        return item;
    }

    /**
     * Add container to result.
     *
     * @param parentNodeId parent node id
     * @param node         container node
     * @param childCount   child count
     * @throws ContentDirectoryException
     */
    public void addContainer(final String parentNodeId, final MediaNode node, final int childCount) throws ContentDirectoryException {
        Container container = new Container(node.getId(), parentNodeId, node.getName(), null, CONTAINER_CLASS, childCount);
        container.setSearchable(true);
        setDidlMetadata(container, node);

        didl.addContainer(container);
        itemCount++;
    }

    /**
     * Check if node can be added to result according to pagination parameters.
     *
     * @return true, if successful
     */
    public boolean acceptNode() {
        totalCount++;
        return maxResults == 0 || itemCount < maxResults && totalCount >= firstResult + 1;
    }

    /**
     * Set the didl metadata.
     *
     * @param didlObject didl object
     * @param node       node
     * @throws ContentDirectoryException
     */
    private void setDidlMetadata(final DIDLObject didlObject, final MediaNode node) throws ContentDirectoryException {
        if (node.getModifiedDate() != null) {
            didlObject.replaceFirstProperty(new DC.DATE(new SimpleDateFormat(UPNP_DATE_FORMAT).format(node.getModifiedDate())));
        }

        if (node.getIconUrl() != null) {
            try {
                didlObject.replaceFirstProperty(new UPNP.ICON(new URI(node.getIconUrl())));
            } catch (URISyntaxException e) {
                throw new ContentDirectoryException(CANNOT_PROCESS.getCode(), e.getMessage(), e);
            }
        }
    }
}
