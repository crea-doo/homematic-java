package at.creadoo.homematic.jhid;

import java.util.concurrent.atomic.AtomicBoolean;

import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.MessageCallback;
import at.creadoo.homematic.packets.HomeMaticPacket;
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

    private final HidLink link;

    private HidListener listener; // The packet listener

    public HidConnection(final HidDevice device, final HidLink link) {
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
    public void received(final byte[] packet) {
    	PacketUtil.logPacket(link, packet);
        
        final HomeMaticPacket homeMaticPacket = PacketUtil.createPacket(packet);        
        if (homeMaticPacket == null) {
        	return;
        }
        
        log.debug("Packet: " + homeMaticPacket);
        
        for (ILinkListener listener : this.link.getLinkListeners()) {
            listener.received(homeMaticPacket);
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
