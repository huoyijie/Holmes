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

package net.holmes.core.util.inject;

import javax.inject.Inject;
import javax.inject.Provider;

import net.holmes.core.configuration.Configuration;
import net.holmes.core.upnp.ContentDirectoryService;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import com.google.inject.Injector;

public class UpnpServiceServiceProvider implements Provider<UpnpService> {

    private final Injector injector;
    private final Configuration configuration;

    @Inject
    public UpnpServiceServiceProvider(Injector injector, Configuration configuration) {
        this.injector = injector;
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UpnpService get() {

        // Create Upnp service
        UpnpService upnpService = new UpnpServiceImpl();

        // Device identity
        DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("Holmes UPnP Server"));

        // Device type
        DeviceType type = new UDADeviceType("MediaServer", 1);

        // Device name
        DeviceDetails details = new DeviceDetails(configuration.getUpnpServerName());

        // Content directory service
        LocalService<ContentDirectoryService> contentDirectoryService = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
        DefaultServiceManager<ContentDirectoryService> serviceManager = new DefaultServiceManager<ContentDirectoryService>(contentDirectoryService,
                ContentDirectoryService.class);
        contentDirectoryService.setManager(serviceManager);

        injector.injectMembers(serviceManager.getImplementation());

        // Create local device
        try {
            upnpService.getRegistry().addDevice(new LocalDevice(identity, type, details, new LocalService[] { contentDirectoryService }));
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return upnpService;
    }
}