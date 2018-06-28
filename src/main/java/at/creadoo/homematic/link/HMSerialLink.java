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

import at.creadoo.homematic.IHomeMaticLinkListener;
import at.creadoo.homematic.impl.LinkBaseImpl;
import at.creadoo.homematic.packet.HomeMaticPacket;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.event.HidServicesEvent;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * {@link HMSerialLink} manages the connection to the HomeMatic HM-MOD_RPI_PCB.
 */
public class HMSerialLink extends LinkBaseImpl {

	private static final Logger log = Logger.getLogger(HMSerialLink.class);
	
	private String device = null;

    public HMSerialLink() {
    	this(null);
	}
    
    public HMSerialLink(final IHomeMaticLinkListener listener) {
    	super(listener);
    	
		log.debug("Initializing... ");
	}
    
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean isReconnectSupported() {
		return true;
	}

	@Override
	protected boolean startLink(final boolean reconnecting) {
		/*
		boolean result = true;
		synchronized (connectionsBySerial) {
			for (Map.Entry<String, HidConnection> entry : connectionsBySerial.entrySet()) {
				if (!entry.getValue().isOpened() && !entry.getValue().open()) {
					result = false;
				}
			}
		}
		return result;
		*/
		return true;
	}

	@Override
	protected boolean closeLink() {
		/*
		boolean result = true;
		synchronized (connectionsBySerial) {
			for (Map.Entry<String, HidConnection> entry : connectionsBySerial.entrySet()) {
				if (entry.getValue().isOpened()) {
					if (!entry.getValue().close()) {
						result = false;
					}
				}
			}
		}
		return result;
		*/
		return true;
	}

	@Override
	protected boolean setupAES() {
		return true;
	}

	@Override
	protected void cleanUpAES() {
		//
	}
    
    @Override
	public boolean send(final HomeMaticPacket packet) throws SocketException, IOException {
    	/*
    	packet.setMessageCounter(getNextMessageCounter(packet.getDestinationAddress()));

		boolean result = false;
		synchronized (connectionsBySerial) {
	    	for (String serial: connectionsBySerial.keySet()) {
	    		final HidConnection connection = connectionsBySerial.get(serial);
	    		if (connection.isOpened()) {
					if (connection.sendPacketToDevice(packet.getData())) {
						result = true;
					} else {
						log.debug("Error while sending packet via connection '" + serial + "'");
					}
	    		} else {
	    			log.debug("Connection '" + serial + "' currently closed");
	    		}
			}
		}
    	
    	return result;
    	*/
    	return true;
	}

	public HidConnection getHidConnectionByDeviceSerial(final String serial) {
		/*
		synchronized (connectionsBySerial) {
			for (String key: connectionsBySerial.keySet()) {
				if (key.equals(serial))  {
					return connectionsBySerial.get(key);
				}
			}
		}
		*/
		return null;
	}

	/**
	 * Getter for the serial device.
	 */
	private String getDevice() {
		return device;
	}

	/**
	 * Setter for the serial device.
	 * 
	 * @param device
	 */
	public void setDevice(final String device) {
		/*
		if (StringUtils.isBlank(device)) {
			return;
		}
		
		try {
			// vids in the config file can start with '0x' -> interpret those as
			// hexadecimal numbers
			final int vid = vendorId.trim().startsWith("0x") ? Integer.parseInt(vendorId.trim().substring(2), 16) : Integer.parseInt(vendorId.trim());
			
			if (vid != this.vendorId) {
				this.vendorId = vid;
				
				updateDevices();
			}
		} catch (Throwable e) {
			log.warn("Unable to parse vendor id '" + vendorId + "'");
		}
		*/
	}

}
