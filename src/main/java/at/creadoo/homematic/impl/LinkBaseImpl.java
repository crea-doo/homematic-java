package at.creadoo.homematic.impl;

import org.apache.log4j.Logger;

import at.creadoo.homematic.ILink;
import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.packets.HomeMaticPacket;

import java.io.IOException;
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

	@Override
	public boolean start() {
    	return start(false);
    }
    
    public Boolean start(final Boolean reconnecting) {
        log.debug("Starting link " + getName());

        if (!listen.getAndSet(true)) {
        	//Actually setup the link and start it
    		
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
	
	protected abstract boolean startLink(final Boolean reconnecting);
	
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
	public boolean isConnected() {
		return listen.get();
	}

	@Override
	public void close() {
		log.debug("Stopping link '" + getName() + "' ...");
		if (listen.getAndSet(false)) {
			cleanUpAES();
			
			//Actually stop the link
			closeLink();
		} else {
			log.warn("Link '" + getName() + "' not running");
		}
	}
	
	protected abstract boolean closeLink();

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
	public abstract boolean send(final HomeMaticPacket packet) throws IOException;

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
