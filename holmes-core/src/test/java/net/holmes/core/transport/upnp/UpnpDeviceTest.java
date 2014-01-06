/*
 * Copyright (C) 2012-2014  Cedric Cheneau
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

package net.holmes.core.transport.upnp;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UpnpDeviceTest {

    @Test
    public void testEmptyDevice() {
        UpnpDevice device = new UpnpDevice("id", "name", null, null, null);
        device.close();
        assertEquals("id", device.getId());
        assertEquals("name", device.getName());
        assertNull(device.getAddress());
        assertNull(device.getSupportedMimeTypes());
        assertNull(device.getAvTransportService());
        assertFalse(device.isVideoSupported());
        assertFalse(device.isAudioSupported());
        assertFalse(device.isImageSupported());
        assertFalse(device.isSlideShowSupported());
    }

    @Test
    public void testFullDevice() {
        List<String> supportedMimeTypes = Lists.newArrayList("video/avi", "audio/mp3", "image/jpeg", "application/x-subrip");
        UpnpDevice device = new UpnpDevice("id", "name", null, supportedMimeTypes, null);
        assertNotNull(device.getSupportedMimeTypes());
        assertTrue(device.isVideoSupported());
        assertTrue(device.isAudioSupported());
        assertTrue(device.isImageSupported());
        assertTrue(device.isSlideShowSupported());
    }
}
