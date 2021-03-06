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
package at.creadoo.homematic.impl;

import org.apache.log4j.Logger;

import at.creadoo.homematic.IHomeMaticLink;
import at.creadoo.homematic.IHomeMaticLinkListener;
import at.creadoo.homematic.packet.HomeMaticPacket;
import at.creadoo.homematic.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Socket connector base implementation class for connecting to gateway
 */
public abstract class LinkBaseImpl implements IHomeMaticLink {
	
	private static final Logger log = Logger.getLogger(LinkBaseImpl.class);
	
    private final List<IHomeMaticLinkListener> listeners = new ArrayList<IHomeMaticLinkListener>();
    
    protected final AtomicBoolean listen = new AtomicBoolean(false);
    
    private boolean aesEnabled = false;
    
    protected AtomicBoolean aesInitialized = new AtomicBoolean(false);
    
    protected byte[] aesRFKey = null;
    protected int aesRFKeyIndex = 1;

    protected byte[] aesRFKeyOld = null;

	/**
	 * Holds the values of the message counters for the destination devices
	 */
	private final Map<Integer, Integer> messageCounters = new HashMap<Integer, Integer>();

	public LinkBaseImpl() {
		this(null);
	}
	
	public LinkBaseImpl(final IHomeMaticLinkListener listener) {
		if (listener != null) {
			addLinkListener(listener);
		}
	}
	
	protected List<IHomeMaticLinkListener> getListeners() {
		synchronized (listeners) {
			return listeners;
		}
	}

	@Override
	public boolean start() {
    	return start(false);
    }
    
    public boolean start(final boolean reconnecting) {
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
	
	protected abstract boolean startLink(final boolean reconnecting);
	
	public void reconnect() {
		start(true);
	}

	protected void receivedPacket(final HomeMaticPacket packet) {
		for (IHomeMaticLinkListener listener : getListeners()) {
			try {
				listener.received(this, packet);
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
			
			for (IHomeMaticLinkListener listener : getLinkListeners()) {
				try {
		            listener.linkClosed(this);
				} catch (Throwable ex) {
					//
				}
	        }
		} else {
			log.warn("Link '" + getName() + "' not running");
		}
	}
	
	protected void terminate() {
		log.debug("Terminating link '" + getName() + "' ...");
		if (listen.getAndSet(false)) {
			cleanUpAES();
			
			//Actually stop the link
			closeLink();
			
			for (IHomeMaticLinkListener listener : getLinkListeners()) {
				try {
					listener.linkTerminated(this);
				} catch (Throwable ex) {
					//
				}
	        }
		} else {
			log.warn("Link '" + getName() + "' not running");
		}
	}
	
	protected abstract boolean closeLink();

	@Override
	public List<IHomeMaticLinkListener> getLinkListeners() {
		synchronized (listeners) {
			return listeners;
		}
	}

	@Override
	public void addLinkListener(final IHomeMaticLinkListener linkListener) {
		synchronized (listeners) {
			if (!listeners.contains(linkListener)) {
				listeners.add(linkListener);
			}
		}
	}

	@Override
	public void removeLinkListener(final IHomeMaticLinkListener linkListener) {
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

	protected String getAESRFKey() {
		if (this.aesRFKey != null) {
			return Util.toHex(this.aesRFKey);
		}
		return null;
	}
	
	public boolean setAESRFKey(final String aesRfKey) {
		if (aesRfKey == null || aesRfKey.length() != 32 || !Util.isHex(aesRfKey)) {
			return false;
		}
		
		this.aesRFKey = Util.toByteFromHex(aesRfKey);
		return true;
	}

	protected int getAESRFKeyIndex() {
		return this.aesRFKeyIndex;
	}

	public void setAESRFKeyIndex(final int aesRfKeyIndex) {
		if (aesRfKeyIndex > 0) {
			this.aesRFKeyIndex = aesRfKeyIndex;
		}
	}

	protected String getAESRFKeyOld() {
		if (this.aesRFKeyOld != null) {
			return Util.toHex(this.aesRFKeyOld);
		}
		return null;
	}

	public void setAESRFKeyOld(final byte[] aesRfKeyOld) {
		this.aesRFKeyOld = aesRfKeyOld;
	}

	protected abstract boolean setupAES();

	protected abstract void cleanUpAES();

    protected int getNextMessageCounter(final int destinationAddress) {
    	int value = 0;
    	if (messageCounters.containsKey(destinationAddress)) {
    		value = messageCounters.get(destinationAddress) + 1;
    		if (value > 255) {
    			value = 0;
        	}
    	}
    	messageCounters.put(destinationAddress, value);

    	return value;
    }

    protected void decreaseMessageCounter(final int destinationAddress) {
    	if (messageCounters.containsKey(destinationAddress)) {
    		int value = messageCounters.get(destinationAddress) - 1;
    		if (value < 0) {
    			value = 255;
        	}
        	messageCounters.put(destinationAddress, value);
    	}
    }

}
