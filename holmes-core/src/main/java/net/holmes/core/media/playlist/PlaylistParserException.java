/**
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

package net.holmes.core.media.playlist;

/**
 * Playlist parser exception.
 */
public class PlaylistParserException extends Exception {
    private static final long serialVersionUID = -5068408691447981899L;

    /**
     * Constructor.
     */
    public PlaylistParserException() {
        super();
    }

    /**
     * Constructor.
     *
     * @param message 
     *      message
     * @param exception 
     *      exception
     */
    public PlaylistParserException(final String message, final Throwable exception) {
        super(message, exception);
    }

    /**
     * Constructor.
     *
     * @param message 
     *      message
     */
    public PlaylistParserException(final String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param exception 
     *      exception
     */
    public PlaylistParserException(final Throwable exception) {
        super(exception);
    }
}
