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

package net.holmes.core.backend.manager;

import com.google.common.eventbus.EventBus;
import net.holmes.core.backend.exception.BackendException;
import net.holmes.core.backend.response.ConfigurationFolder;
import net.holmes.core.backend.response.Settings;
import net.holmes.core.business.configuration.ConfigurationManager;
import net.holmes.core.business.configuration.exception.UnknownNodeException;
import net.holmes.core.business.configuration.model.ConfigurationNode;
import net.holmes.core.common.event.ConfigurationEvent;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static net.holmes.core.business.media.model.RootNode.AUDIO;
import static net.holmes.core.common.ConfigurationParameter.UPNP_SERVER_NAME;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class BackendManagerImplTest {

    @Test
    public void testGetFolders() {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.getNodes(AUDIO)).andReturn(newArrayList(new ConfigurationNode("id", "name", "path")));

        replay(configurationManager, eventBus);

        BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
        Collection<ConfigurationFolder> folders = backendManager.getFolders(AUDIO);
        assertNotNull(folders);
        assertEquals(1, folders.size());

        verify(configurationManager, eventBus);
    }

    @Test
    public void testGetFolder() throws UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.getNode(eq(AUDIO), eq("id"))).andReturn(new ConfigurationNode("id", "name", "path"));

        replay(configurationManager, eventBus);

        BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
        assertNotNull(backendManager.getFolder("id", AUDIO));

        verify(configurationManager, eventBus);
    }

    @Test
    public void testGetNullFolder() throws UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.getNode(eq(AUDIO), eq("id"))).andReturn(null);

        replay(configurationManager, eventBus);

        BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
        assertNull(backendManager.getFolder("id", AUDIO));

        verify(configurationManager, eventBus);
    }

    @Test(expected = BackendException.class)
    public void testGetBadFolder() throws UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.getNode(eq(AUDIO), eq("bad_id"))).andThrow(new UnknownNodeException("bad_id"));

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.getFolder("bad_id", AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test
    public void testAddFolder() throws IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, null, "newAudioFolder", System.getProperty("java.io.tmpdir"))).andReturn(Optional.<ConfigurationNode>empty());
        expect(configurationManager.addNode(eq(AUDIO), isA(ConfigurationNode.class))).andReturn(true);
        eventBus.post(isA(ConfigurationEvent.class));
        expectLastCall();

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.addFolder(new ConfigurationFolder(null, "newAudioFolder", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test
    public void testAddExistingFolder() throws IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, null, "newAudioFolder", System.getProperty("java.io.tmpdir"))).andReturn(Optional.<ConfigurationNode>empty());
        expect(configurationManager.addNode(eq(AUDIO), isA(ConfigurationNode.class))).andReturn(false);

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.addFolder(new ConfigurationFolder(null, "newAudioFolder", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testAddFolderIOException() throws IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, null, "newAudioFolder", System.getProperty("java.io.tmpdir"))).andReturn(Optional.<ConfigurationNode>empty());
        configurationManager.addNode(eq(AUDIO), isA(ConfigurationNode.class));
        expectLastCall().andThrow(new IOException());

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.addFolder(new ConfigurationFolder(null, "newAudioFolder", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testAddFolderWithSameName() throws IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, null, "name", System.getProperty("java.io.tmpdir"))).andReturn(Optional.of(new ConfigurationNode("id", "name", "path")));

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.addFolder(new ConfigurationFolder(null, "name", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testAddFolderWithSamePath() throws IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, null, "newAudioFolder", System.getProperty("java.io.tmpdir"))).andReturn(Optional.of(new ConfigurationNode("id", "name", System.getProperty("java.io.tmpdir"))));

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.addFolder(new ConfigurationFolder(null, "newAudioFolder", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testAddFolderWithoutName() throws IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.addFolder(new ConfigurationFolder(null, null, System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testAddFolderWithoutPath() {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.addFolder(new ConfigurationFolder(null, "newAudioFolder", null), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testAddFolderWithBadPath() {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.addFolder(new ConfigurationFolder(null, "newAudioFolder", "bad_path"), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testEditFolderIOException() throws IOException, UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, "id", "editedAudiosTest", System.getProperty("java.io.tmpdir"))).andReturn(Optional.<ConfigurationNode>empty());
        expect(configurationManager.editNode(AUDIO, "id", "editedAudiosTest", System.getProperty("java.io.tmpdir"))).andThrow(new IOException());

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.editFolder("id", new ConfigurationFolder("id", "editedAudiosTest", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test
    public void testEditFolder() throws IOException, UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, "id", "name", System.getProperty("java.io.tmpdir"))).andReturn(Optional.<ConfigurationNode>empty());
        expect(configurationManager.editNode(AUDIO, "id", "name", System.getProperty("java.io.tmpdir"))).andReturn(Optional.of(new ConfigurationNode("id", "name", System.getProperty("java.io.tmpdir"))));
        eventBus.post(isA(ConfigurationEvent.class));
        expectLastCall();

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.editFolder("id", new ConfigurationFolder("id", "name", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testEditBadFolder() throws IOException, UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, "bad_id", "editedAudiosTest", System.getProperty("java.io.tmpdir"))).andReturn(Optional.<ConfigurationNode>empty());
        expect(configurationManager.editNode(AUDIO, "bad_id", "editedAudiosTest", System.getProperty("java.io.tmpdir"))).andThrow(new UnknownNodeException("bad_id"));

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.editFolder("bad_id", new ConfigurationFolder("id", "editedAudiosTest", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test
    public void testEditFolderNotSaved() throws IOException, UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.findNode(AUDIO, "id", "name", System.getProperty("java.io.tmpdir"))).andReturn(Optional.<ConfigurationNode>empty());
        expect(configurationManager.editNode(AUDIO, "id", "name", System.getProperty("java.io.tmpdir"))).andReturn(Optional.<ConfigurationNode>empty());

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.editFolder("id", new ConfigurationFolder("id", "name", System.getProperty("java.io.tmpdir")), AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test
    public void testRemoveFolder() throws IOException, UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        ConfigurationNode node = new ConfigurationNode("id", "name", System.getProperty("java.io.tmpdir"));
        expect(configurationManager.removeNode("id", AUDIO)).andReturn(node);
        expectLastCall();
        eventBus.post(isA(ConfigurationEvent.class));
        expectLastCall();

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.removeFolder("id", AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testRemoveFolderIOException() throws IOException, UnknownNodeException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.removeNode(eq("id"), eq(AUDIO))).andThrow(new IOException());

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.removeFolder("id", AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testRemoveBadFolder() throws UnknownNodeException, IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.removeNode("bad_folder", AUDIO)).andThrow(new UnknownNodeException("bad_folder"));

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.removeFolder("bad_folder", AUDIO);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test
    public void testGetSettings() {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        expect(configurationManager.getParameter(UPNP_SERVER_NAME)).andReturn("serverName");

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            Settings settings = backendManager.getSettings();
            assertNotNull(settings);
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test
    public void testSaveSettings() throws IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        configurationManager.setParameter(UPNP_SERVER_NAME, "holmes");
        expectLastCall();
        configurationManager.save();
        expectLastCall();
        eventBus.post(isA(ConfigurationEvent.class));
        expectLastCall();

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.saveSettings(new Settings("holmes"));
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testSaveSettingsWithoutServerName() {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.saveSettings(new Settings(null));
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testSaveSettingsIOException() throws IOException {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        configurationManager.setParameter(UPNP_SERVER_NAME, "holmes");
        expectLastCall();
        configurationManager.save();
        expectLastCall().andThrow(new IOException());

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.saveSettings(new Settings("holmes"));
        } finally {
            verify(configurationManager, eventBus);
        }
    }

    @Test(expected = BackendException.class)
    public void testSaveSettingsWithEmptyServerName() {
        ConfigurationManager configurationManager = createMock(ConfigurationManager.class);
        EventBus eventBus = createMock(EventBus.class);

        replay(configurationManager, eventBus);

        try {
            BackendManagerImpl backendManager = new BackendManagerImpl(configurationManager, eventBus);
            backendManager.saveSettings(new Settings(""));
        } finally {
            verify(configurationManager, eventBus);
        }
    }
}
