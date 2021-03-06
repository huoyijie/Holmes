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

package net.holmes.core.service.airplay;

import net.holmes.core.business.configuration.ConfigurationManager;
import net.holmes.core.business.streaming.StreamingManager;
import net.holmes.core.business.streaming.airplay.device.AirplayDevice;
import net.holmes.core.business.streaming.airplay.device.AirplayDeviceFeatures;
import net.holmes.core.service.Service;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.concurrent.Executors;

import static net.holmes.core.common.ConfigurationParameter.AIRPLAY_STREAMING_ENABLE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Airplay service
 */
@Singleton
public final class AirplayService implements Service {
    private static final Logger LOGGER = getLogger(AirplayService.class);
    private static final String AIRPLAY_TCP = "_airplay._tcp.local.";
    private static final String AIRPLAY_FEATURES = "features";

    private final ConfigurationManager configurationManager;
    private final InetAddress localAddress;
    private final StreamingManager streamingManager;

    private JmDNS jmDNS = null;

    /**
     * Instantiates a new Airplay service.
     *
     * @param configurationManager configuration manager
     * @param localAddress         local address
     * @param streamingManager     streaming manager
     */
    @Inject
    public AirplayService(final ConfigurationManager configurationManager, @Named("localAddress") final InetAddress localAddress, final StreamingManager streamingManager) {
        this.configurationManager = configurationManager;
        this.localAddress = localAddress;
        this.streamingManager = streamingManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (configurationManager.getParameter(AIRPLAY_STREAMING_ENABLE)) {
            LOGGER.info("Starting Airplay service");
            try {
                // Create JmDNS
                jmDNS = JmDNS.create(localAddress);

                // Look up for available devices
                lookupAsync();

                // Add Listener to manage inbound and outbound devices
                jmDNS.addServiceListener(AIRPLAY_TCP, new ServiceListener() {
                    @Override
                    public void serviceAdded(ServiceEvent event) {
                        // Nothing, waiting for service to be resolved with serviceResolved method
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent event) {
                        streamingManager.removeDevice(event.getInfo().getKey());
                    }

                    @Override
                    public void serviceResolved(ServiceEvent event) {
                        streamingManager.addDevice(buildDevice(event.getInfo()));
                    }
                });
                LOGGER.info("Airplay service started");

            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        if (jmDNS != null) {
            LOGGER.info("Stopping Airplay service");
            try {
                jmDNS.close();
                LOGGER.info("Airplay service stopped");
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Asynchronous look up for available Airplay devices
     */
    private void lookupAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (ServiceInfo serviceInfo : jmDNS.list(AIRPLAY_TCP)) {
                streamingManager.addDevice(buildDevice(serviceInfo));
            }
        });
    }

    /**
     * Build Airplay device associated to jmDNS service info.
     * For now, only IPV4 addresses are accepted.
     *
     * @param serviceInfo jmDNS service information
     * @return Airplay device
     */
    private AirplayDevice buildDevice(final ServiceInfo serviceInfo) {
        if (serviceInfo != null && serviceInfo.getInet4Addresses() != null) {
            for (Inet4Address inet4Address : serviceInfo.getInet4Addresses()) {
                if (!inet4Address.isLoopbackAddress()) {
                    AirplayDeviceFeatures features = new AirplayDeviceFeatures(serviceInfo.getPropertyString(AIRPLAY_FEATURES));
                    return new AirplayDevice(serviceInfo.getKey(), serviceInfo.getName(), inet4Address, serviceInfo.getPort(), features);
                }
            }
        }
        return null;
    }
}
