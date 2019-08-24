/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.handler;

import static org.openhab.binding.tuya.TuyaBindingConstants.CHANNEL_POWER;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.tuya.internal.exceptions.ParseException;
import org.openhab.binding.tuya.internal.json.CommandByte;
import org.openhab.binding.tuya.internal.json.JsonPowerPlug;
import org.openhab.binding.tuya.internal.json.JsonStatusQuery;
import org.openhab.binding.tuya.internal.net.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler for a Tuya Switch device.
 *
 * @author Wim Vissers
 *
 */
public class PowerPlugHandler extends AbstractTuyaHandler {

    private Logger logger = LoggerFactory.getLogger(PowerPlugHandler.class);

    public PowerPlugHandler(Thing thing) {
        super(thing);
    }

    /**
     * This method is called when a DeviceEventEmitter.Event.MESSAGE_RECEIVED is received from the device. This could
     * result in a possible state change of this things channels.
     */
    @Override
    protected void handleStatusMessage(Message message) {
        JsonPowerPlug dev = message.toPowerPlugDevice();
        updateState(new ChannelUID(thing.getUID(), CHANNEL_POWER), dev.getDps().isDp1() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * This method is called when the device is connected, for an initial status request if the device supports it.
     */
    @Override
    protected void sendStatusQuery() {
        try {
            JsonStatusQuery query = new JsonStatusQuery(deviceDescriptor);
            deviceEventEmitter.send(query, CommandByte.DP_QUERY);
        } catch (IOException | ParseException e) {
            logger.error("Error on status request", e);
        }
    }

    /**
     * Add the commands to the dispatcher.
     */
    @Override
    protected void initCommandDispatcher() {
        // Channel power command with OnOffType.
        commandDispatcher.on(CHANNEL_POWER, OnOffType.class, command -> {
            JsonPowerPlug dev = new JsonPowerPlug(deviceDescriptor);
            dev.getDps().setDp1(command == OnOffType.ON);
            return dev;
        });
    }

}
