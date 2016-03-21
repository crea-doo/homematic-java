package at.creadoo.homematic;

import at.creadoo.homematic.socket.SocketLink;
import at.creadoo.homematic.util.CryptoUtil;
import at.creadoo.homematic.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class DummySocketLink extends SocketLink {

	private static final Logger log = Logger.getLogger(DummySocketLink.class);

	private final String remoteIV = "V1B0D06036A351A0D0201000000000000";
	
	private final String localIV = "V7DA24AF4F55B92B5B4E1CE35F714EC9B";

	public DummySocketLink() {
		super(null);
	}

	public DummySocketLink(final InetSocketAddress remoteAddress) {
		super(remoteAddress);
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
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					DummySocketLink.this.received(remoteIV.getBytes());
				}
			}, 500);
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
    			log.debug("Received localIV: [" + Util.toString(subset) + "]");

    			this.aesInitialized = true;
    			
    			// Send back encrypted data to test decryption
    			this.received(Util.toByteFromHex("EFBFBD22EFBFBD66EFBFBD4DEFBFBDD4AF22EFBFBDEFBFBDEFBFBD29313536C4ADEFBFBD7DEFBFBDC793EFBFBDEFBFBDEFBFBD6A72664BEFBFBD78"));
    			this.received(Util.toByteFromHex("65EFBFBDEFBFBD70EFBFBDEFBFBD4233EFBFBD2FEFBFBD2F271A101AEFBFBD2760EFBFBDEFBFBD4CEFBFBDEFBFBD7B7967EFBFBD0405EFBFBD4B5750EFBFBDEFBFBD"));
        	}
        }
		
		return true;
	}

	@Override
	protected boolean setupAES() {
    	if (this.getAESEnabled()) {
    		this.aesLocalIV = Util.toByteFromHex(localIV.substring(1));

			log.debug("LanKey for 'HM-CFG-LAN': [" + Util.toHex(this.aesLanKey) + "]");
			log.debug("LocalIV for 'HM-CFG-LAN': [" + Util.toHex(this.aesLocalIV) + "]");
			
    		try {
	    		this.aesCipherEncrypt = CryptoUtil.getAESCipherEncrypt(aesLanKey, this.aesLocalIV);
			} catch (Throwable ex) {
				log.error("Error while setting up AES", ex);
				return false;
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
