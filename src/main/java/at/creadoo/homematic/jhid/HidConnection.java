package at.creadoo.homematic.jhid;

import java.util.concurrent.atomic.AtomicBoolean;

import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.MessageCallback;
import at.creadoo.homematic.packets.HomeMaticPacket;
import at.creadoo.homematic.util.Util;

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

    private final HidLink link;

    private HidListener listener; // The packet listener

    public HidConnection(final HidDevice device, final HidLink link) {
        this.device = device;
        this.link = link;
    }
    
    /**
     * Returns connection status
     */
    public Boolean isOpened() {
    	return opened.get();
    }
    
    /**
     * Open the device and start the listener
     */
    public Boolean open() {
        
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
    public Boolean close() {
    	// the listener also closes the connection
    	listener.stop();
        
        opened.getAndSet(false);
    	
    	return true;
    }

    @Override
    public void received(final byte[] packet) {
        Util.logPacket(link, packet);
        
        final HomeMaticPacket homeMaticPacket = Util.createPacket(packet);        
        if (homeMaticPacket == null) {
        	return;
        }
        
        log.debug("Packet: " + homeMaticPacket);
        
        for (ILinkListener listener : this.link.getLinkListeners()) {
            listener.received(homeMaticPacket);
        }
    }

    public boolean sendPacketToDevice(byte[] packet) {
        return listener.write(packet);
    }

	@Override
	public void connectionTerminated() {
		close();
	}

}
