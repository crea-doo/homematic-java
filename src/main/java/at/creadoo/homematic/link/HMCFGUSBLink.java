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
 * {@link HMCFGUSBLink} manages the connection to the HomeMatic USB
 * gateway HM-CFG-USB-(2). It is responsible to start the real hardware (USB
 * gateway).
 */
public class HMCFGUSBLink extends LinkBaseImpl implements HidServicesListener {

	private static final Logger log = Logger.getLogger(HMCFGUSBLink.class);

	public static final int DEFAULT_PRODUCT_ID = 0xc00f;

	public static final int DEFAULT_VENDOR_ID = 0x1b1f;

	private final HidServices hidServices = HidManager.getHidServices();
	
	private int vendorId = DEFAULT_VENDOR_ID;

	private int productId = DEFAULT_PRODUCT_ID;
	
	private HashMap<String, HidConnection> connectionsBySerial = new HashMap<String, HidConnection>();

    public HMCFGUSBLink() {
    	this(null);
	}
    
    public HMCFGUSBLink(final IHomeMaticLinkListener listener) {
    	super(listener);
    	
		log.debug("Initializing... ");

		hidServices.addHidServicesListener(this);
		
		updateDevices();
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
		boolean result = true;
		synchronized (connectionsBySerial) {
			for (Map.Entry<String, HidConnection> entry : connectionsBySerial.entrySet()) {
				if (!entry.getValue().isOpened() && !entry.getValue().open()) {
					result = false;
				}
			}
		}
		return result;
	}

	@Override
	protected boolean closeLink() {
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
	}

	public HidConnection getHidConnectionByDeviceSerial(final String serial) {
		synchronized (connectionsBySerial) {
			for (String key: connectionsBySerial.keySet()) {
				if (key.equals(serial))  {
					return connectionsBySerial.get(key);
				}
			}
		}
		return null;
	}

	/**
	 * Getter for the USB Vendor Id.
	 */
	private int getVendorId() {
		return vendorId;
	}

	/**
	 * Setter for the USB Vendor Id.
	 * 
	 * @param vendorId
	 */
	public void setVendorId(final String vendorId) {
		if (StringUtils.isBlank(vendorId)) {
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
	}

	/**
	 * Getter for the USB Vendor Id.
	 */
	private int getProductId() {
		return productId;
	}

	/**
	 * Setter for the USB Vendor Id.
	 * 
	 * @param productId
	 */
	public void setProductId(final String productId) {
		if (StringUtils.isBlank(productId)) {
			return;
		}
		
		try {
			// pids in the config file can start with '0x' -> interpret those as
			// hexadecimal numbers
			final int pid = productId.trim().startsWith("0x") ? Integer.parseInt(productId.trim().substring(2), 16) : Integer.parseInt(productId.trim());
			
			if (pid != this.productId) {
				this.productId = pid;
				
				updateDevices();
			}
		} catch (Throwable e) {
			log.warn("Unable to parse product id '" + productId + "'");
		}
	}

	@Override
	public void hidDeviceAttached(final HidServicesEvent event) {
		log.debug("Device attached: " + this.getDeviceIdString(event.getHidDevice()));
		
		final HidDevice device = event.getHidDevice();
		if (device == null) {
			return;
		}

		if (ignoreDevice(device)) {
			log.debug("Ignoring device with vendor id 0x" + Integer.toHexString(getVendorId(device)) + ", product id 0x" + Integer.toHexString(getProductId(device)));
			return;
		}
		
		// Create the connection
		final HidConnection connection = addDevice(device);

		// Open the connection
		if (connection != null) {
			connection.open();
		}
	}

	@Override
	public void hidDeviceDetached(final HidServicesEvent event) {
		log.debug("Device detached: " + this.getDeviceIdString(event.getHidDevice()));
		
		final HidDevice device = event.getHidDevice();
		if (device == null) {
			return;
		}
		
		removeDevice(device);
	}

	@Override
	public void hidFailure(final HidServicesEvent event) {
		log.debug("Device failure: " + this.getDeviceIdString(event.getHidDevice()));
		
		final HidDevice device = event.getHidDevice();
		if (device == null) {
			return;
		}
		
		removeDevice(device);
	}
	
	private int getVendorId(final HidDevice device) {
		return ((int) device.getVendorId()) & 0xFFFF;
	}
	
	private int getProductId(final HidDevice device) {
		return ((int) device.getProductId()) & 0xFFFF;
	}

	private String getDeviceIdString(final HidDevice device) {
		int tempProdid = ((int) device.getProductId()) & 0xFFFF;
		int tempVendid = ((int) device.getVendorId()) & 0xFFFF;
		return Integer.toHexString(tempProdid) + ":" + Integer.toHexString(tempVendid);
	}
	
	private boolean ignoreDevice(final HidDevice device) {
		final int vid = getVendorId();
		final int pid = getProductId();
		final int tempVendorId = getVendorId(device);
		final int tempProductId = getProductId(device);

		if (tempVendorId != vid || tempProductId != pid) {
			log.debug("Ignoring device with vendor id 0x" + Integer.toHexString(tempVendorId) + ", product id 0x" + Integer.toHexString(tempProductId));
			return true;
		}
		return false;
	}
	
	private HidConnection addDevice(final HidDevice device) {
		if (device == null) {
			return null;
		}
		
		// Look for the gateway id
		final String serial = device.getSerialNumber();
		if (serial == null) {
			log.debug("Found gateway with invalid serial ('" + serial + "'): " + device.toString());
			return null;
		} else {
			log.debug("Found gateway with serial '" + serial + "': " + device.toString());
		}

		synchronized (connectionsBySerial) {
			if (connectionsBySerial.containsKey(serial)) {
				return null;
			}
			
			// Create the connection
			final HidConnection connection = new HidConnection(device, this);
	
	        // Store the connection
	        connectionsBySerial.put(serial, connection);

	        return connection;
		}
	}
	
	private void removeDevice(final HidDevice device) {
		if (device == null) {
			return;
		}
		
		// Look for the gateway id
		final String serial = device.getSerialNumber();
		if (serial == null) {
			log.debug("Found device with invalid serial ('" + serial + "'): " + device.toString());
			return;
		} else {
			log.debug("Found device with serial '" + serial + "': " + device.toString());
		}

		synchronized (connectionsBySerial) {
			if (!connectionsBySerial.containsKey(serial)) {
				return;
			}
			
	        // Remove the connection
			final HidConnection connection = connectionsBySerial.remove(serial);
	
			if (connection != null && connection.isOpened()) {
				connection.close();
			}
		}
	}
    
    private void updateDevices() {
    	// Clean up
		synchronized (connectionsBySerial) {
			final Iterator<Map.Entry<String, HidConnection>> iterator = connectionsBySerial.entrySet().iterator();
			while (iterator.hasNext()) {
				final Map.Entry<String, HidConnection> entry = iterator.next();
				if (entry.getValue().isOpened()) {
					entry.getValue().close();
				}
				iterator.remove();
			}
		}
    	
		// Provide a list of attached devices
		for (HidDevice device : hidServices.getAttachedHidDevices()) {
			if (ignoreDevice(device)) {
				log.debug("Ignoring device with vendor id 0x" + Integer.toHexString(getVendorId(device)) + ", product id 0x" + Integer.toHexString(getProductId(device)));
				continue;
			}

			// Create the connection
			final HidConnection connection = addDevice(device);

			// Open the connection
			if (connection != null && isConnected()) {
				connection.open();
			}
		}
		
		synchronized (connectionsBySerial) {
			if (connectionsBySerial.size() == 0) {
				log.error("No devices found!");
			} else {
				log.debug(connectionsBySerial.size() + " HID devices found!");
			}
		}
    }

}
