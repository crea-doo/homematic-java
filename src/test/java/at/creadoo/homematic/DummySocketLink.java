package at.creadoo.homematic;

import at.creadoo.homematic.socket.SocketLink;
import at.creadoo.homematic.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import org.apache.log4j.Logger;

public class DummySocketLink extends SocketLink {

	private static final Logger log = Logger.getLogger(DummySocketLink.class);

	private final byte[] remoteIV;

	public DummySocketLink() {
		super(null);
		
		this.remoteIV = null;
		this.aesLocalIV = null;
	}

	public DummySocketLink(final String remoteIV, final String localIV) {
		super(null);
		
		this.remoteIV = Util.toByteFromHex(remoteIV);
		this.aesLocalIV = localIV;
	}

	public DummySocketLink(final InetSocketAddress remoteAddress) {
		super(remoteAddress);
		
		this.remoteIV = null;
		this.aesLocalIV = null;
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
		if (this.getAESEnabled()) {
			/*
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
			*/
					if (remoteIV != null) {
						DummySocketLink.this.received(("V" + Util.toHex(remoteIV)).getBytes());
					} else {
						log.error("Remote IV not set!");
					}
			/*
				}
			}, 500);
			*/
		}
		
		return true;
	}

	@Override
	protected boolean closeLink() {
		return true;
	}

	@Override
	protected boolean send(final byte[] data) throws SocketException, IOException {
        if (getAESEnabled() && !this.aesInitialized) {
        	char c = (char) data[0];
        	if (c == 'V') {
        		final byte[] subset = Util.subset(data, 1);
    			log.debug("Send localIV: [" + Util.toString(subset) + "]");

    			//this.aesInitialized = true;
        	}
        }
		
		return true;
	}

	@Override
	protected void cleanUpAES() {
		//
	}

	@Override
    public void received(final byte[] packet) {
		super.received(packet);
	}

}
