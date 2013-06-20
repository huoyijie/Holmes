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

package com.sun.syndication.feed.module.itunes.types;

import java.io.Serializable;

/**
 * This class represents a Subcategory of a Category.
 *
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 * @version $Revision: 1.2 $
 */
public final class Subcategory implements Cloneable, Serializable {
    private static final long serialVersionUID = -8563595355552684061L;

    private String name;

    /**
     * Creates a new instance of SubCategory
     */
    public Subcategory() {
    }

    /**
     * Creates a new instance of Category with a given name.
     *
     * @param name Name of the category.
     */
    public Subcategory(final String name) {
        this.setName(name);
    }

    /**
     * Returns the name of the subcategory.
     *
     * @return Returns the name of the subcategory.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the subcategory.
     *
     * @param name Set the name of the subcategory.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Clones the object.
     *
     * @return Clone of the object.
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        return new Subcategory(this.getName());
    }
}
