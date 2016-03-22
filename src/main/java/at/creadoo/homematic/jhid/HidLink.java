package at.creadoo.homematic.jhid;

import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.impl.LinkBaseImpl;
import at.creadoo.homematic.packets.HomeMaticPacket;

import org.apache.log4j.Logger;
import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.event.HidServicesEvent;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * HidLink manages the connection to the homematic usb
 * gateway HM-CFG-USB-(2). It is responsible to start the real hardware (USB
 * gateway).
 */
public class HidLink extends LinkBaseImpl implements HidServicesListener {

	private static final Logger log = Logger.getLogger(HidLink.class);

	private String usbVendorId;

	private String usbProductId;
	
	private HashMap<String, HidConnection> connectionsBySerial = new HashMap<String, HidConnection>();

    public HidLink() {
    	super();
	}
    
    public HidLink(final ILinkListener listener) {
    	super(listener);
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
	protected boolean startLink(final Boolean reconnecting) {
		Boolean result = true;
		
		log.debug("Initializing... ");

		final int vid = getUsbVendorId();
		final int pid = getUsbProductId();

		HidServices hidServices = HidManager.getHidServices();
		hidServices.addHidServicesListener(this);

		if (hidServices.getAttachedHidDevices() == null) {
			log.error("No devices found!");
			return false;
		}

		log.debug(hidServices.getAttachedHidDevices().size() + " HID devices found!");

		// Provide a list of attached devices
		for (HidDevice device : hidServices.getAttachedHidDevices()) {
			int tempVendorId = ((int) device.getVendorId()) & 0xFFFF;
			int tempProductId = ((int) device.getProductId()) & 0xFFFF;

			if (tempVendorId != vid || tempProductId != pid) {
				log.debug("Ignoring device with vendor id 0x" + Integer.toHexString(tempVendorId) + ", product id 0x" + Integer.toHexString(tempProductId));
				continue;
			}

			// Look for the gateway id
			final String serial = device.getSerialNumber();
			if (serial == null) {
				log.debug("Found gateway with invalid serial ('" + serial + "'): " + device.toString());
				continue;
			} else {
				log.debug("Found gateway with serial '" + serial + "': " + device.toString());
			}

			// Create the connection
			final HidConnection connection = new HidConnection(device, this);

            // Store the connection
            connectionsBySerial.put(serial, connection);

			// Open the connection
			if (!connection.open()) {
				result = false;
			}
		}
		
		return result;
	}

	@Override
	protected boolean closeLink() {
		Boolean result = true;
		for (Map.Entry<String, HidConnection> entry : this.connectionsBySerial.entrySet()) {
			if (entry.getValue().isOpened()) {
				if (!entry.getValue().close()) {
					result = false;
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
    	
    	return result;
	}

	public HidConnection getHidConnectionByDeviceSerial(final String serial) {
		for (String key: this.connectionsBySerial.keySet()) {
			if (key.equals(serial))  {
				return this.connectionsBySerial.get(key);
			}
		}
		return null;
	}

	private int getUsbVendorId() {
		// Read configuration
		int vid = 0x1b1f;
		try {
			// vids in the config file can start with '0x' -> interpret those as
			// hexadecimal numbers
			vid = usbVendorId.startsWith("0x") ? Integer.parseInt(usbVendorId.substring(2), 16) : Integer.parseInt(usbVendorId);
		} catch (Throwable e) {
			log.warn("Unable to parse vendor id '" + usbVendorId + "', using default: " + Integer.toHexString(vid));
		}
		return vid;
	}

	/**
	 * Setter for the UsbVendorId.
	 * 
	 * @param usbVendorId
	 */
	public void setUsbVendorId(String usbVendorId) {
		this.usbVendorId = usbVendorId;
	}

	private int getUsbProductId() {
		int pid = 0xc00f;
		try {
			// pids in the config file can start with '0x' -> interpret those as
			// hexadecimal numbers
			pid = usbProductId.startsWith("0x") ? Integer.parseInt(
					usbProductId.substring(2), 16) : Integer
					.parseInt(usbProductId);
		} catch (Throwable e) {
			log.warn("Unable to parse product id '" + usbProductId
					+ "', using default: " + Integer.toHexString(pid));
		}
		return pid;
	}

	/**
	 * Setter for the UsbProductId.
	 * 
	 * @param usbProductId
	 */
	public void setUsbProductId(final String usbProductId) {
		this.usbProductId = usbProductId;
	}

	private String getDeviceIdString(final HidDevice device) {
		int tempProdid = ((int) device.getProductId()) & 0xFFFF;
		int tempVendid = ((int) device.getVendorId()) & 0xFFFF;
		return Integer.toHexString(tempProdid) + ":" + Integer.toHexString(tempVendid);
	}

	public boolean isGatewayConnected(final Integer serial) {
		for (String key : connectionsBySerial.keySet()) {
			if (Integer.valueOf(key).equals(serial)) {
				final HidConnection connection = connectionsBySerial.get(key);
				if (connection.isOpened()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void hidDeviceAttached(final HidServicesEvent event) {
		log.debug("Device attached: " + this.getDeviceIdString(event.getHidDevice()));

	}

	@Override
	public void hidDeviceDetached(final HidServicesEvent event) {
		log.debug("Device detached: " + this.getDeviceIdString(event.getHidDevice()));
	}

	@Override
	public void hidFailure(final HidServicesEvent event) {
		// TODO Auto-generated method stub

	}

}
