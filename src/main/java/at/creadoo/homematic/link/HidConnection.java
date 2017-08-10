/*
 * Copyright 2017 crea-doo.at
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package at.creadoo.homematic.link;

import java.util.concurrent.atomic.AtomicBoolean;

import at.creadoo.homematic.IHomeMaticLinkListener;
import at.creadoo.homematic.MessageCallback;
import at.creadoo.homematic.packet.HomeMaticPacket;
import at.creadoo.homematic.packet.HomeMaticPacketRemote;
import at.creadoo.homematic.util.PacketUtil;

import org.apache.log4j.Logger;
import org.hid4java.HidDevice;

/**
 * This class holds the device information of a HID device and information about
 * the connection status
 */
public class HidConnection implements MessageCallback  {

    private static final Logger log = Logger.getLogger(HidConnection.class);
    
    protected final AtomicBoolean opened = new AtomicBoolean(false);

    private final HidDevice device;

    private final HMCFGUSBLink link;

    private HidListener listener; // The packet listener

    public HidConnection(final HidDevice device, final HMCFGUSBLink link) {
        this.device = device;
        this.link = link;
    }
    
    /**
     * Returns connection status
     */
    public boolean isOpened() {
    	return opened.get();
    }
    
    /**
     * Open the device and start the listener
     */
    public boolean open() {
        
    	// Initialize and start the listener
        listener = new HidListener(this, device);
        
        // the listener opens the connection
        listener.start();
        
        opened.getAndSet(true);
        
        return true;
    }

    /**
     * Close the connection and stop all listeners
     */
    public boolean close() {
    	// the listener also closes the connection
    	listener.stop();
        
        opened.getAndSet(false);
    	
    	return true;
    }

    @Override
    public void received(final Object source, final byte[] packet) {
    	PacketUtil.logPacket(link, packet);
        
        final HomeMaticPacket homeMaticPacket = PacketUtil.createPacket(packet);        
        if (homeMaticPacket == null) {
        	return;
        }
        
        log.debug("Packet: " + homeMaticPacket);
        if (homeMaticPacket instanceof HomeMaticPacketRemote) {
        	final HomeMaticPacketRemote homeMaticPacketRemote = (HomeMaticPacketRemote) homeMaticPacket;
        	log.debug("Packet: Channel = " + homeMaticPacketRemote.getChannel());
        }
        
        for (IHomeMaticLinkListener listener : this.link.getLinkListeners()) {
            listener.received(link, homeMaticPacket);
        }
    }

    public boolean sendPacketToDevice(final byte[] packet) {
        return listener.write(packet);
    }

	@Override
	public void connectionTerminated() {
		close();
	}

}
