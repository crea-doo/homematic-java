package at.creadoo.homematic.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.MessageCallback;
import at.creadoo.homematic.impl.LinkBaseImpl;
import at.creadoo.homematic.packets.HomeMaticPacket;
import at.creadoo.homematic.util.CryptoUtil;
import at.creadoo.homematic.util.PacketUtil;
import at.creadoo.homematic.util.Util;

public class SocketLink extends LinkBaseImpl implements MessageCallback {
	
	private static final Logger log = Logger.getLogger(SocketLink.class);

	/**
	 * Default timeout in milliseconds
	 */
	private static final Integer DEFAULT_TIMEOUT = 5000;

	private static final Integer DEFAULT_KEEP_ALIVE_INTERVAL = 25 * 1000;
	
	private static final Integer RESET_GATEWAY_TIME_INTERVAL = 10 * 60 * 60 * 1000;

	/**
	 * Line end marker
	 */
	public static final byte[] EOL = new byte[] {0x0D , 0x0A};
	
	/**
	 * Socket
	 */
	private Socket socket;

	/**
	 * IP address the socket tries to connect to
	 */
	private final InetSocketAddress remoteAddress;

	/**
	 * Timeout when connecting to the socket and read timeout
	 */
	private final Integer connectionTimeout;
	
	private SocketListener listener;

	private int firmwareVersion;

	private String serial;

	private int addressDefault;

	private int address;

	/**
	 * The keep alive interval defines the period after that a keep alive packet
	 * is sent
	 */
	private int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL;

	/**
	 * Holds the startup time value read from the gateway
	 */
	private long startUpTime = 0L;

	/**
	 * Holds the timestamp of the last received response to a keep alive packet
	 */
	private long lastKeepAliveResponse = 0L;

	private final Timer timer = new Timer();

    /**
     * Hexadecimal representation of the cenztral address to be used
     */
	protected String centralAddress = null;

    /**
     * Hexadecimal representation of the key used for en-/decrypting messages
     */
	protected String aesLanKey = null;

    /**
     * Hexadecimal representation of the key used for en-/decrypting messages
     */
	protected byte[] aesLanKeyByte = null;

    /**
     * Hexadecimal representation of the initialization vector sent from the gateway
     */
    private String aesRemoteIV = null;

    /**
     * Byte representation of the initialization vector sent from the gateway
     */
    private byte[] aesRemoteIVByte = null;

    /**
     * Hexadecimal representation of the initialization vector sent to the gateway
     */
    protected String aesLocalIV = null;

    /**
     * Byte representation of the initialization vector sent to the gateway
     */
    protected byte[] aesLocalIVByte = null;
    
    protected Cipher aesCipherEncrypt = null;
    
    protected Cipher aesCipherDecrypt = null;
    
    public SocketLink(final InetSocketAddress remoteAddress) {
    	this(remoteAddress, null);
    }
    
    public SocketLink(final InetSocketAddress remoteAddress, final ILinkListener listener) {
    	super(listener);
    	
    	this.remoteAddress = remoteAddress;
		this.connectionTimeout = DEFAULT_TIMEOUT;
	}

	@Override
	public String getName() {
		String result = this.getClass().getSimpleName();
		if (remoteAddress != null) {
			result = result + "(" + remoteAddress.getHostName() + ":" + remoteAddress.getPort() + ")";
		} else {
			result = result + "(invalid)";
		}
		return result;
	}

	@Override
	public boolean isReconnectSupported() {
		return true;
	}

	@Override
	protected boolean startLink(final Boolean reconnecting) {
		log.info("*** Address *** set to " + remoteAddress.getHostName() + ":" + remoteAddress.getPort());
		log.info("*** Timeout *** set to " + connectionTimeout);
		
		try {
			this.prepareSocket(remoteAddress, connectionTimeout);
		} catch (IOException ex) {
			log.debug("Error connecting to socket", ex);
			return false;
		}
		log.info("*** Connected *** to " + remoteAddress);
		startReceiver();
		
		startUpTime = 0L;
		
		// TimerTask to keep connection opened
		final TimerTask keepAlive = new TimerTask() {

			@Override
			public void run() {
				sendKeepAlive();
			}
		};
		
		// TimerTask to set time in the gateway
		final TimerTask gatewayTime = new TimerTask() {

			@Override
			public void run() {
				try {
					setupGatewayTime();
				} catch (Throwable ex) {
					log.error("Error while setting gateway time", ex);
				}
			}
		};
		
		// scheduling the task at interval
		timer.schedule(keepAlive, keepAliveInterval, keepAliveInterval);
		timer.schedule(gatewayTime, RESET_GATEWAY_TIME_INTERVAL, RESET_GATEWAY_TIME_INTERVAL);

		if (!this.getAESEnabled()) {
			try {
				setupGateway();
			} catch (Throwable ex) {
				log.error("Error initializing gateway", ex);
				return false;
			}
		} else {
			// Wait for AES initialized
			try {
				while (!this.aesInitialized) {
					Thread.sleep(100);
				}
			} catch (InterruptedException ex) {
				log.error("Error while waiting AES to be initialized", ex);
			}
		}
		
		return isConnected();
	}

	@Override
	protected boolean closeLink() {
		stopReceiver();
		
		timer.cancel();
		
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ex) {
				log.error("Error while closing socket", ex);
			}
		}
		
		return true;
	}

	/**
	 * Prepare the socket
	 */
	private final Socket prepareSocket(final InetSocketAddress remoteAddress, final Integer timeout) throws IOException {
		if (this.socket != null) {
			this.socket.close();
		}
		this.socket = new Socket();
		socket.connect(remoteAddress, connectionTimeout);
		socket.setSoTimeout(0);
		return socket;
	}

	/**
	 * Starting the receiver thread
	 */
	private final void startReceiver() {
		if (listener == null) {
			// start thread
			(listener = new SocketListener(this, socket)).start();
		}
	}

	/**
	 * Stopping the receiver thread
	 */
	private final void stopReceiver() {
		if (listener != null) {
			// stop thread
			listener.stop();
			listener = null;
		}
	}

	/**
	 * Setup the gateway with AES keys and time
	 * 
	 * @throws IOException 
	 * @throws SocketException 
	 */
	private final void setupGateway() throws SocketException, IOException {
		// Clear settings
		send("C");
		
		// Set central address
		if (this.centralAddress != null) {
			send("A" + this.centralAddress);
		}
		
		// Set current AES RF key
		final String strAESKey;
		if (getAESRFKey() != null && getAESRFKey().length() > 0) {
			strAESKey = "Y01," + Util.padLeft(Util.toHex(getAESRFKeyIndex()), 2, "0") + "," + getAESRFKey();
		} else {
			strAESKey = "Y01,00,";
		}
		send(strAESKey);

		// Set previous AES RF key
		final String strAESKeyOld;
		if (getAESRFKeyOld() != null && getAESRFKeyOld().length() > 0) {
			strAESKeyOld = "Y02," + Util.padLeft(Util.toHex(getAESRFKeyIndex() - 1), 2, "0") + "," + getAESRFKey();
		} else {
			strAESKeyOld = "Y02,00,";
		}
		send(strAESKeyOld);

		// Setting a third AES RF key is not supported
		send("Y03,00,");
		
		// Set current date/time
		setupGatewayTime();
	}

	/**
	 * Setup current time in the gateway. The time information is stored as the
	 * seconds since year 2000 UTC plus the GMT offset in hours.
	 * 
	 * @throws IOException
	 * @throws SocketException
	 */
	private final void setupGatewayTime() throws SocketException, IOException {
		final TimeZone tz = TimeZone.getDefault();
		final long now = new Date().getTime();
		//final int gmtOffset = tz.getOffset(now) / 1000 / 60 / 60;
		final int gmtOffset = tz.getOffset(now) / 1000 / 1800;
		// Add one second for processing time
		final long secondsSince2000 = (now / 1000) - 946684800 + 1;
		
		send("T" + Util.padLeft(Util.toHex(secondsSince2000), 8, "0") + "," + Util.padLeft(Util.toHex(gmtOffset), 2, "0") + ",00,00000000");
	}

    @Override
    public void received(final byte[] data) {
    	final List<byte[]> packets;
    	
        if (getAESEnabled() && this.aesInitialized) {
        	log.debug("Treating packet as encrypted");

        	try {
            	log.debug("Encrypted packet >>");
                PacketUtil.logPacket(this, data);
            	log.debug("<<");
                if (this.aesCipherDecrypt == null) {
                	log.error("Decryption not working due to missing cipher");
                	return;
                } else {
                	// Decrypt data
                	final byte[] decrypted = CryptoUtil.aesCrypt(this.aesCipherDecrypt, data);
                	packets = Util.split(decrypted, EOL);
                }
			} catch (Throwable ex) {
				log.error("Error while decrypting", ex);
				return;
			}
        } else {
        	// Add plain packet
        	packets = Util.split(data, EOL);
        }

    	for (byte[] packet : packets) {
        	log.debug("Packet >>");
        	PacketUtil.logPacket(this, packet);
        	log.debug("<<");
        	
        	try {
        		processData(data);
        	} catch (Throwable ex) {
        		log.error("Error while processing packets", ex);
        	}
    	}
    }

    public void processData(final byte[] data) {
    	final byte[] packet = Util.strip(data, EOL);
    	final String[] parts = new String(packet).split(",");
        
        char c = (char) packet[0];
        if (c == 'H') {
            /*
    		Index	Meaning
    		0		"HM-CFG-LAN"
    		1		Firmware version
    		2		Serial number
    		3		Default address?
    		4		Address
    		5		Time since boot in milliseconds
    		6		Number of registered peers
    		*/
        	
        	if (parts.length >= 7) {
        		firmwareVersion = Integer.parseInt(parts[1], 16);
        		serial = parts[2];
        		addressDefault = Integer.parseInt(parts[3], 16);
        		address = Integer.parseInt(parts[4], 16);
        		
        		startUpTime = Long.parseLong(parts[5], 16);
        		lastKeepAliveResponse = System.currentTimeMillis();
        		
        		log.debug("Received info from 'HM-CFG-LAN': [serial=" + serial + ", firmwareVersion=" + firmwareVersion + ", addressDefault=" + addressDefault + ", address=" + address + "]");
        		return;
        	}
		} else if (c == 'V' && getAESEnabled()) {
			final String remoteIV = Util.toString(Util.subset(packet, 1));
	        
	        this.aesRemoteIV = remoteIV;
	        this.aesRemoteIVByte = Util.toByteFromHex(this.aesRemoteIV);
	        
			log.debug("Received RemoteIV from 'HM-CFG-LAN': [" + this.aesRemoteIV + "]");
	        
	        if (!Util.isHex(this.aesRemoteIV) || this.aesRemoteIVByte.length != 16) {
				log.warn("RemoteIV received from HM-CFG-LAN not in hexadecimal format: " + this.aesRemoteIV);
				return;
	        }
	        
    		try {
	    		this.aesCipherEncrypt = CryptoUtil.getAESCipherEncrypt(this.aesLanKeyByte, this.aesRemoteIVByte);
			} catch (Throwable ex) {
				log.error("Error while setting up AES", ex);
				close();
				return;
			}
    		
    		try {
    			log.debug("Send LocalIV to 'HM-CFG-LAN': [" + this.aesLocalIV + "]");
    			final String response = "V" + this.aesLocalIV.toUpperCase();
    			send(response.getBytes());
    		} catch(IOException ex) {
    			log.error("Error while setting up AES", ex);
    			close();
				return;
    		}
    		
    		this.aesInitialized = true;
    		
    		try {
				setupGateway();
			} catch (Throwable ex) {
				log.error("Error initializing gateway", ex);
				close();
			}
		} else if (c == 'V' && !getAESEnabled()) {
			log.error("Device 'HM-CFG-LAN' requires AES, but AES was not configured!");
			return;
		} else if (c == 'E' || c == 'R') {
			final HomeMaticPacket homeMaticPacket = PacketUtil.createPacketByMessageType(PacketUtil.convertLANPacketToBidCos(packet));
			if (homeMaticPacket == null) {
	        	return;
	        }
	        
	        log.debug("Packet: " + homeMaticPacket);
	        
	        for (ILinkListener listener : getLinkListeners()) {
	            listener.received(homeMaticPacket);
	        }
		}
    }
    
    @Override
	public boolean send(final HomeMaticPacket packet) throws SocketException, IOException {
    	packet.setSenderAddress(address);
    	packet.setMessageCounter(getNextMessageCounter(packet.getDestinationAddress()));
    	
		final String data = "S" + formatHexTime(System.currentTimeMillis()) + ",00,00000000,01," + formatHexTime(System.currentTimeMillis() - startUpTime) + "," + Util.toHex(packet.getData()).substring(2);

		final boolean result = send(data);
		if (!result) {
			decreaseMessageCounter(packet.getDestinationAddress());
		}
		return result;
	}
	
	protected boolean send(final String data) throws SocketException, IOException {
		return send(data.getBytes());
	}

	protected boolean send(final byte[] data) throws SocketException, IOException {
		if (data == null) {
			log.debug("Sending not possible. Data is null.");
			return false;
		}
		if (socket == null) {
			log.error("Sending not possible. Socket is null.");
			return false;
		}
		
		final byte[] packet;
        if (getAESEnabled()) {
        	if (this.aesInitialized) {
            	// Encryption enabled and ready
	        	log.debug("Encrypt packet");
	        	try {
	            	log.debug("Plain packet >>");
	            	PacketUtil.logPacket(this, data);
	            	log.debug("<<");
	                if (this.aesCipherEncrypt == null) {
	                	log.error("Encryption not working due to missing cipher");
	                	packet = Util.appendItem(data, EOL);
	                } else {
	                	packet = CryptoUtil.aesCrypt(this.aesCipherEncrypt, Util.appendItem(data, EOL));
	                }
	
	            	log.debug("Encrypted packet >>");
	            	PacketUtil.logPacket(this, packet);
	            	log.debug("<<");
				} catch (Throwable ex) {
					log.error("Error while encrypting", ex);
					return false;
				}
            } else {
            	// Encryption enabled but not yet ready
	        	//log.debug("Trying to send packet but AES not initialized yet. Sending plain.");
	        	packet = Util.appendItem(data, EOL);
            }
        } else {
        	// Encryption disabled
        	packet = Util.appendItem(data, EOL);
        }
		
		try {
			final OutputStream out = socket.getOutputStream();
			out.write(packet);
			out.flush();
			log.debug("Sending packet: '" + Util.toString(data) + "'");
		} catch (IOException ex) {
			log.error("Sending: IO exception", ex);
			return false;
		}
		return true;
	}
    
    /**
     * Send a keep alive packet to keep the connection opened
     * @return True, if packet sent successfully, false otherwise
     */
    private boolean sendKeepAlive() {
    	try {
			return send("K");
		} catch (SocketException ex) {
			log.debug("Error while sending keep alive packet", ex);
		} catch (IOException ex) {
			log.debug("Error while sending keep alive packet", ex);
		}
    	return false;
    }

	@Override
	public void connectionTerminated() {
		close();
	}
    
    private String formatHexTime(final Long time) {
    	return Util.padLeft(Util.toHex(time), 8, "0");
    }

	@Override
	protected boolean setupAES() {
    	if (this.getAESEnabled()) {
    		if (this.aesLocalIV == null) {
    			this.aesLocalIV = Util.randomHex(32).toUpperCase();
    			this.aesLocalIVByte = Util.toByteFromHex(this.aesLocalIV);
    		}

			log.debug("LanKey for 'HM-CFG-LAN': [" + this.aesLanKey + "]");
			log.debug("LocalIV for 'HM-CFG-LAN': [" + this.aesLocalIV + "]");
			
    		try {
	    		this.aesCipherDecrypt = CryptoUtil.getAESCipherDecrypt(this.aesLanKeyByte, this.aesLocalIVByte);
			} catch (Throwable ex) {
				log.error("Error while setting up AES", ex);
				return false;
			}
    	}
    	return true;
    }
	
	@Override
    protected void cleanUpAES() {
    	this.aesInitialized = false;
    	this.aesRemoteIV = null;
    	this.aesRemoteIVByte = null;
    	this.aesLocalIV = null;
    	this.aesLocalIVByte = null;
    	this.aesCipherDecrypt = null;
    	this.aesCipherEncrypt = null;
    }
	
	public boolean setCentralAddress(final String centralAddress) {
		if (centralAddress == null || centralAddress.length() != 6 || !Util.isHex(centralAddress)) {
			return false;
		}
		
		this.centralAddress = centralAddress;
		return true;
	}
	
	public boolean setAESLANKey(final String aesLanKey) {
		if (aesLanKey == null || aesLanKey.length() != 32 || !Util.isHex(aesLanKey)) {
			return false;
		}
		
		this.aesLanKey = aesLanKey;
		this.aesLanKeyByte = Util.toByteFromHex(aesLanKey);
		return true;
	}

	public int getFirmwareVersion() {
		return firmwareVersion;
	}

	public String getSerial() {
		return serial;
	}

	public int getAddressDefault() {
		return addressDefault;
	}

	public int getAddress() {
		return address;
	}
	
	public int getKeepAliveInterval() {
		return keepAliveInterval;
	}

	public void setKeepAliveInterval(final int keepAliveInterval) {
		if (keepAliveInterval > 0 && keepAliveInterval < 30) {
			this.keepAliveInterval = keepAliveInterval;
		}
	}

	public void testAES(final String data) {
		log.debug("testAES: Input = '" + data + "'");
		
		setupAES();
		
		received(data.getBytes());
	}

}
