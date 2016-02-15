package at.creadoo.homematic.impl;

import java.io.IOException;
import java.net.SocketException;

import org.apache.log4j.Logger;

import at.creadoo.homematic.ILink;
import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.packets.HomeMaticPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Socket connector base implementation class for connecting to gateway
 */
public abstract class LinkBaseImpl implements ILink {
	
	private static final Logger log = Logger.getLogger(LinkBaseImpl.class);
	
    private final List<ILinkListener> listeners = new ArrayList<ILinkListener>();
    
    protected final AtomicBoolean listen = new AtomicBoolean(false);
    
    private boolean aesEnabled = false;
    
    protected boolean aesInitialized = false;
    
    protected byte[] aesRfKey = null;

	public LinkBaseImpl() {
		this(null);
	}
	
	public LinkBaseImpl(final ILinkListener listener) {
		if (listener != null) {
			addLinkListener(listener);
		}
	}
	
	protected List<ILinkListener> getListeners() {
		synchronized (listeners) {
			return listeners;
		}
	}

	public Boolean start() {
    	return start(false);
    }
    
    public Boolean start(final Boolean reconnecting) {
        log.debug("Starting link " + getName());

        if (!listen.getAndSet(true)) {
        	//TODO: actually setup the link and start it
    		
    		if (!setupAES()) {
    			return false;
    		}
    		
        	startLink(reconnecting);
        } else {
            log.warn("Link '" + getName() + "' already running");
            return false;
        }
        
        return true;
    }
	
	protected abstract Boolean startLink(final Boolean reconnecting);
	
	public void reconnect() {
		start(true);
	}

	protected void receivedPacket(final HomeMaticPacket packet) {
		for (ILinkListener listener : getListeners()) {
			try {
				listener.received(packet);
			} catch (Throwable ex) {
				//
			}
		}
	}
	
	@Override
	public Boolean isConnected() {
		return listen.get();
	}

	@Override
	public void close() {
		log.debug("Stopping link '" + getName() + "' ...");
		if (listen.getAndSet(false)) {
			cleanUpAES();
			
			//TODO: Actually stop the link
			closeLink();
		} else {
			log.warn("Link '" + getName() + "' not running");
		}
	}
	
	protected abstract Boolean closeLink();

	@Override
	public List<ILinkListener> getLinkListeners() {
		synchronized (listeners) {
			return listeners;
		}
	}

	@Override
	public void addLinkListener(final ILinkListener linkListener) {
		synchronized (listeners) {
			if (!listeners.contains(linkListener)) {
				listeners.add(linkListener);
			}
		}
	}

	@Override
	public void removeLinkListener(final ILinkListener linkListener) {
		synchronized (listeners) {
			if (listeners.contains(linkListener)) {
				listeners.remove(linkListener);
			}
		}
	}

	@Override
	public Boolean send(final HomeMaticPacket packet) throws SocketException, IOException {
		//TODO: Implement write functionality
		return false;
	}

	public boolean getAESEnabled() {
		return aesEnabled;
	}

	public void setAESEnabled(final boolean useAES) {
		this.aesEnabled = useAES;
	}

	public void setAESRFKey(final byte[] aesRfKey) {
		this.aesRfKey = aesRfKey;
	}

	protected abstract boolean setupAES();
	
	protected abstract void cleanUpAES();

}
