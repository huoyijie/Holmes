/*
 * PhotocastModuleImpl.java
 *
 * Created on March 30, 2006, 6:23 PM
 *
  *
 * This library is provided under dual licenses.
 * You may choose the terms of the Lesser General Public License or the Apache
 * License at your discretion.
 *
 *  Copyright (C) 2006  Robert Cooper, Temple of the Screaming Penguin
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.syndication.feed.module.photocast;

import java.net.URL;
import java.util.Date;

import com.sun.syndication.feed.impl.EqualsBean;
import com.sun.syndication.feed.impl.ToStringBean;
import com.sun.syndication.feed.module.photocast.types.Metadata;

/**
 *
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 */
public class PhotocastModuleImpl implements PhotocastModule {
    private static final long serialVersionUID = 8035085246742367220L;

    private Date photoDate;
    private Date cropDate;
    private URL imageUrl;
    private URL thumbnailUrl;
    private Metadata metadata;

    /** Creates a new instance of PhotocastModuleImpl */
    public PhotocastModuleImpl() {
    }

    @Override
    public Date getPhotoDate() {
        return photoDate;
    }

    @Override
    public void setPhotoDate(Date photoDate) {
        this.photoDate = photoDate;
    }

    @Override
    public Date getCropDate() {
        return cropDate;
    }

    @Override
    public void setCropDate(Date cropDate) {
        this.cropDate = cropDate;
    }

    @Override
    public URL getImageUrl() {
        return imageUrl;
    }

    @Override
    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public URL getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public void setThumbnailUrl(URL thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void copyFrom(Object obj) {
        PhotocastModule pm = (PhotocastModule) obj;
        this.setPhotoDate((pm.getPhotoDate() == null) ? null : (Date) pm.getPhotoDate().clone());
        this.setCropDate((pm.getCropDate() == null) ? null : (Date) pm.getCropDate().clone());
        this.setImageUrl(pm.getImageUrl());
        this.setThumbnailUrl(pm.getThumbnailUrl());
        this.setMetadata(pm.getMetadata());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        PhotocastModuleImpl pm = new PhotocastModuleImpl();
        pm.setPhotoDate((this.getPhotoDate() == null) ? null : (Date) this.getPhotoDate().clone());
        pm.setCropDate((this.getCropDate() == null) ? null : (Date) this.getCropDate().clone());
        pm.setImageUrl(this.getThumbnailUrl());
        pm.setThumbnailUrl(this.getThumbnailUrl());
        pm.setMetadata(this.getMetadata());

        return pm;
    }

    @Override
    public String getUri() {
        return PhotocastModule.URI;
    }

    @Override
    public Class<?> getInterface() {
        return PhotocastModule.class;
    }

    @Override
    public String toString() {
        ToStringBean tsBean = new ToStringBean(PhotocastModuleImpl.class, this);

        return tsBean.toString();
    }

    @Override
    public boolean equals(Object obj) {
        EqualsBean eBean = new EqualsBean(PhotocastModuleImpl.class, this);

        return eBean.beanEquals(obj);
    }

    @Override
    public int hashCode() {
        EqualsBean equals = new EqualsBean(PhotocastModuleImpl.class, this);

        return equals.beanHashCode();
    }
}
