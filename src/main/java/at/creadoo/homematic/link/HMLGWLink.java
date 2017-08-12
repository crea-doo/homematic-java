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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import at.creadoo.homematic.IHomeMaticLinkListener;
import at.creadoo.homematic.MessageCallback;
import at.creadoo.homematic.impl.LinkBaseImpl;
import at.creadoo.homematic.packet.HomeMaticPacket;
import at.creadoo.homematic.util.CryptoUtil;
import at.creadoo.homematic.util.PacketUtil;
import at.creadoo.homematic.util.Util;

/**
 * {@link HMLGWLink} manages the connection to the HomeMatic LAN
 * gateway HM-LGW.
 */
public class HMLGWLink extends LinkBaseImpl implements MessageCallback {
	
	private static final Logger log = Logger.getLogger(HMLGWLink.class);

	/**
	 * Default timeout in milliseconds
	 */
	private static final int DEFAULT_TIMEOUT = 5000;

	private static final int DEFAULT_KEEP_ALIVE_INTERVAL = 25 * 1000;
	
	private static final int RESET_GATEWAY_TIME_INTERVAL = 10 * 60 * 60 * 1000;
	
	/**
	 * Time between checks if AES was initialized successfully
	 */
	private static final int DEFAULT_AES_INIT_WAIT_INTERVAL = 100;
	
	/**
	 * Timeout in milliseconds, to abort waiting for successful AES initialization
	 */
	private static final int DEFAULT_AES_INIT_WAIT_TIMEOUT = DEFAULT_AES_INIT_WAIT_INTERVAL * 10 * 5;

	/**
	 * Line end marker
	 */
	public static final byte[] EOL = new byte[] {0x0D , 0x0A};
	
	/**
	 * Socket for default connection
	 */
	private Socket socketDefault;
	
	/**
	 * Socket for KeepAlive connection
	 */
	private Socket socketKeepAlive;

	/**
	 * IP address and port the socket tries to connect to
	 */
	private final InetSocketAddress remoteAddressDefault;

	/**
	 * IP address and port the socket tries to connect to for the KeepAlive connection
	 */
	private final InetSocketAddress remoteAddressKeepAlive;

	/**
	 * Timeout when connecting to the socket and read timeout
	 */
	private final Integer connectionTimeout;
	
	private DecryptingSocketListener listenerDefault;
	
	private DecryptingSocketListener listenerKeepAlive;

	private String firmwareVersion;

	private String serial;

	private enum SocketType {
		DEFAULT("Default"),
		KEEPALIVE("KeepAlive");
		
		private final String value;
		
		SocketType(String value) {
			this.value = value;
		}
		
		public String toString() {
			return value;
		}
	}
	
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
	private long lastKeepAliveResponseDefault = 0L;
	private long lastKeepAliveResponseKeepAlive = 0L;

	/**
	 * Holds the packet index of the last received message
	 */
	private int lastPacketIndexDefault = 0;
	private int lastPacketIndexKeepAlive = 0;
	private Object lastPacketIndexLockDefault = new Object();
	private Object lastPacketIndexLockKeepAlive = new Object();

	private final Timer timer = new Timer();

    /**
     * Hexadecimal representation of the central address to be used
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
    private String aesRemoteIVDefault = null;
    private String aesRemoteIVKeepAlive = null;

    /**
     * Byte representation of the initialization vector sent from the gateway
     */
    private byte[] aesRemoteIVByteDefault = null;
    private byte[] aesRemoteIVByteKeepAlive = null;

    /**
     * Hexadecimal representation of the initialization vector sent to the gateway
     */
    protected String aesLocalIVDefault = null;
    protected String aesLocalIVKeepAlive = null;

    /**
     * Byte representation of the initialization vector sent to the gateway
     */
    protected byte[] aesLocalIVByteDefault = null;
    protected byte[] aesLocalIVByteKeepAlive = null;
    
    protected Cipher aesCipherEncryptDefault = null;
    protected Cipher aesCipherEncryptKeepAlive = null;
    
    protected Cipher aesCipherDecryptDefault = null;
    protected Cipher aesCipherDecryptKeepAlive = null;
    
    protected AtomicBoolean aesInitializedSocketDefault = new AtomicBoolean(false);
    protected AtomicBoolean aesInitializedSocketKeepAlive = new AtomicBoolean(false);
    
    protected HMLGWLink() {
    	this(null, null);
    }
    
    public HMLGWLink(final InetSocketAddress remoteAddress, final InetSocketAddress remoteAddressKeepAlive) {
    	this(remoteAddress.getAddress(), remoteAddress.getPort(), remoteAddressKeepAlive.getPort(), null);
    }
    
    public HMLGWLink(final InetSocketAddress remoteAddress, final InetSocketAddress remoteAddressKeepAlive, final IHomeMaticLinkListener listener) {
    	this(remoteAddress.getAddress(), remoteAddress.getPort(), remoteAddressKeepAlive.getPort(), listener);
    }
    
    public HMLGWLink(final InetSocketAddress remoteAddress, final int keepAlivePort) {
    	this(remoteAddress.getAddress(), remoteAddress.getPort(), keepAlivePort, null);
    }
    
    public HMLGWLink(final InetSocketAddress remoteAddress, final int keepAlivePort, final IHomeMaticLinkListener listener) {
    	this(remoteAddress.getAddress(), remoteAddress.getPort(), keepAlivePort, listener);
    }
    
    public HMLGWLink(final InetAddress remoteAddress, final int defaultPort, final int keepAlivePort) {
    	this(remoteAddress, defaultPort, keepAlivePort, null);
    }
    
    public HMLGWLink(final InetAddress remoteAddress, final int defaultPort, final int keepAlivePort, final IHomeMaticLinkListener listener) {
    	super(listener);
    	
    	this.remoteAddressDefault = new InetSocketAddress(remoteAddress, defaultPort);
    	this.remoteAddressKeepAlive = new InetSocketAddress(remoteAddress, keepAlivePort);
		this.connectionTimeout = DEFAULT_TIMEOUT;
	}

	@Override
	public String getName() {
		String result = this.getClass().getSimpleName();
		if (remoteAddressDefault != null && remoteAddressKeepAlive != null) {
			result = result + "(" + remoteAddressDefault.getHostName() + ":" + remoteAddressDefault.getPort() + "/" + remoteAddressKeepAlive.getPort() + ")";
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
	protected boolean startLink(final boolean reconnecting) {
		if (remoteAddressDefault != null && remoteAddressKeepAlive != null) {
			if (!remoteAddressDefault.getAddress().equals(remoteAddressKeepAlive.getAddress())) {
				log.error("The host address for Default and KeepAlive connection have be set to identical addresses");
				return false;
			}
			
			if (Integer.compare(remoteAddressDefault.getPort(), remoteAddressKeepAlive.getPort()) == 0) {
				log.error("The ports for Default and KeepAlive connection have be set to different port numbers");
				return false;
			}
			
			log.info("Address for 'HM-LGW' set to '" + remoteAddressDefault.getHostName() + ":" + remoteAddressDefault.getPort() + "/" + remoteAddressKeepAlive.getPort() + "'");
			log.info("Timeout for 'HM-LGW' set to '" + connectionTimeout + "'");
			
			try {
				if (!this.connectSockets(remoteAddressDefault, remoteAddressKeepAlive, connectionTimeout)) {
					log.debug("Error connecting sockets");
					return false;
				}
			} catch (IOException ex) {
				log.debug("Error connecting sockets", ex);
				return false;
			}
			
			log.info("Connected to 'HM-LGW' '" + remoteAddressDefault.getHostName() + ":" + remoteAddressDefault.getPort() + "/" + remoteAddressKeepAlive.getPort() + "'");
			startReceiver();
		}
		
		startUpTime = 0L;
		
		// TimerTask to keep connection opened
		final TimerTask keepAlive = new TimerTask() {

			@Override
			public void run() {
				//TODO: Reactivate
				//sendKeepAlive(SocketType.DEFAULT);
				//sendKeepAlive(SocketType.KEEPALIVE);
			}
		};
		
		// TimerTask to set time in the gateway
		final TimerTask gatewayTime = new TimerTask() {

			@Override
			public void run() {
				try {
					//TODO: Reactivate
					//setupGatewayTime();
				} catch (Throwable ex) {
					log.error("Error while setting gateway time", ex);
				}
			}
		};
		
		// scheduling the task at interval
		if (getAESEnabled()) {
			// Set very short keepAlive interval as otherwise messages are not received or sent in time due to blocked en-/decryption...
			timer.schedule(keepAlive, 1500, 500);
		} else {
			timer.schedule(keepAlive, keepAliveInterval, keepAliveInterval);
		}
		timer.schedule(gatewayTime, RESET_GATEWAY_TIME_INTERVAL, RESET_GATEWAY_TIME_INTERVAL);

		if (this.getAESEnabled()) {
			// Wait for AES initialized
			try {
				int millisWaited = 0;
				while (!this.aesInitialized.get() && (millisWaited < DEFAULT_AES_INIT_WAIT_TIMEOUT)) {
					log.debug("Waiting for AES initialization...");
					Thread.sleep(DEFAULT_AES_INIT_WAIT_INTERVAL);
					millisWaited = millisWaited + DEFAULT_AES_INIT_WAIT_INTERVAL;
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
		
		if (socketDefault != null) {
			try {
				socketDefault.close();
			} catch (IOException ex) {
				log.error("Error while closing socket", ex);
			}
		}
		
		if (socketKeepAlive != null) {
			try {
				socketKeepAlive.close();
			} catch (IOException ex) {
				log.error("Error while closing socket", ex);
			}
		}
		
		return true;
	}

	/**
	 * Prepare the socket
	 */
	private final boolean connectSockets(final InetSocketAddress remoteAddress, final InetSocketAddress remoteAddressKeepAlive, final Integer timeout) throws IOException {
		if (this.socketDefault != null) {
			this.socketDefault.close();
		}
		this.socketDefault = new Socket();
		socketDefault.connect(remoteAddress, connectionTimeout);
		socketDefault.setSoTimeout(0);
		
		if (this.socketKeepAlive != null) {
			this.socketKeepAlive.close();
		}
		this.socketKeepAlive = new Socket();
		socketKeepAlive.connect(remoteAddressKeepAlive, connectionTimeout);
		socketKeepAlive.setSoTimeout(0);
		
		return true;
	}

	/**
	 * Starting the receiver thread
	 */
	private final void startReceiver() {
		startReceiver(null);
	}

	/**
	 * Starting the receiver thread
	 */
	private final void startReceiver(final Cipher cipher) {
		if (listenerDefault == null) {
			// start thread
			(listenerDefault = new DecryptingSocketListener(this, socketDefault, EOL, cipher)).start();
		}
		if (listenerKeepAlive == null) {
			// start thread
			(listenerKeepAlive = new DecryptingSocketListener(this, socketKeepAlive, EOL, cipher)).start();
		}
	}

	/**
	 * Stopping the receiver thread
	 */
	private final void stopReceiver() {
		if (listenerDefault != null) {
			// stop thread
			listenerDefault.stop();
			listenerDefault = null;
		}
		if (listenerKeepAlive != null) {
			// stop thread
			listenerKeepAlive.stop();
			listenerKeepAlive = null;
		}
	}

	/**
	 * Setup the gateway with AES keys and time
	 * 
	 * @throws IOException 
	 * @throws SocketException 
	 * @throws InterruptedException 
	 */
	private final void setupGateway(final SocketType socketType) throws SocketException, IOException, InterruptedException {
		// Gateway needs a moment
		Thread.sleep(2000);
		
		// Clear settings
		send(socketType, ">" + getPacketIndexHex(socketType) + ",0000");
		
		
		//send(socketType, new byte[] {0, 3});
		
		
		
		/*
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
		
		// Request the current config
		sendKeepAlive();
		*/
	}

	/**
	 * Setup current time in the gateway. The time information is stored as the
	 * seconds since year 2000 UTC plus the GMT offset in hours.
	 * 
	 * @throws IOException
	 * @throws SocketException
	 */
	private final boolean setupGatewayTime(final SocketType socketType) throws SocketException, IOException {
		final TimeZone tz = TimeZone.getDefault();
		final long now = new Date().getTime();
		final int gmtOffset = tz.getOffset(now) / 1000 / 1800;
		// Add one second for processing time
		final long secondsSince2000 = (now / 1000) - 946684800 + 1;
		
		return send(socketType, "T" + Util.padLeft(Util.toHex(secondsSince2000), 8, "0") + "," + Util.padLeft(Util.toHex(gmtOffset).toLowerCase(), 2, "0") + ",00,00000000");
	}

    @Override
    public void received(final Object source, final byte[] packet) {
    	final SocketType socketType;
    	if (source.equals(listenerDefault)) {
    		socketType = SocketType.DEFAULT;
    	} else if (source.equals(listenerKeepAlive)) {
    		socketType = SocketType.KEEPALIVE;
    	} else {
    		return;
    	}
    	
    	/* */
    	log.debug("Packet received (" + socketType + ") >>");
    	PacketUtil.logPacket(this, packet);
    	log.debug("<<");
    	/* */
    	
    	try {
    		processData(source, socketType, packet);
    	} catch (Throwable ex) {
    		log.error("Error while processing packet", ex);
    	}
    }

    public void processData(final Object source, final SocketType socketType, final byte[] packet) {
    	// Split the data to parts
    	final String[] parts = new String(packet).split(",");
    	
    	final int packetIndex = Util.toIntFromHex(Util.toString(Util.subset(packet, 1, 2)));
    	
    	// Get current packet index
    	if (socketType.equals(SocketType.DEFAULT)) {
	    	synchronized (lastPacketIndexLockDefault) {
	    		lastPacketIndexDefault = packetIndex;
	    	}
    	} else if (socketType.equals(SocketType.KEEPALIVE)) {
    		synchronized (lastPacketIndexLockKeepAlive) {
	    		lastPacketIndexKeepAlive = packetIndex;
	    	}
    	}
    	
    	// Process the different packet types
        char c = (char) packet[0];
        if (c == 'H') {
            /*
            HXX,01,eQ3-HM-LGW,1.1.X,OEQ0123456
            
    		Index	Meaning
    		0		"Hxx" xx = packet index
    		1		?
    		2		"eQ3-HM-LGW"
    		3		Firmware version
    		4		Serial number
    		*/
        	
        	if (parts.length >= 5) {
        		firmwareVersion = parts[3];
        		serial = parts[4];
        		
        		//startUpTime = Long.parseLong(parts[5], 16);
        		if (socketType.equals(SocketType.DEFAULT)) {
            		lastKeepAliveResponseDefault = System.currentTimeMillis();
            	} else if (socketType.equals(SocketType.KEEPALIVE)) {
            		lastKeepAliveResponseKeepAlive = System.currentTimeMillis();
            	}
        		
        		log.debug("Received info from 'HM-LGW': [serial=" + serial + ", firmwareVersion=" + firmwareVersion + ", packetIndex=" + Util.toHex(packetIndex).toLowerCase() + "]");
        		return;
        	}
		} else if (c == 'V' && getAESEnabled()) {
            /*
            VXX,00112233445566778899001122334455
            
    		Index	Meaning
    		0		"Vxx" xx = packet index
    		1		The AES IV sent from the gateway 
    		*/
			
			final String remoteIV = parts[1];

    		if (socketType.equals(SocketType.DEFAULT)) {
        		
    	        this.aesRemoteIVDefault = remoteIV;
    	        this.aesRemoteIVByteDefault = Util.toByteFromHex(remoteIV);
    			log.debug("Received RemoteIV from 'HM-LGW' (" + socketType + "): [" + this.aesRemoteIVDefault + ", packetIndex=" + Util.toHex(packetIndex).toLowerCase() + "]");
    	        
    	        if (!Util.isHex(this.aesRemoteIVDefault) || this.aesRemoteIVByteDefault.length != 16) {
    				log.warn("RemoteIV received from HM-LGW (" + socketType + ") not in hexadecimal format: " + this.aesRemoteIVDefault);
    				return;
    	        }
    	        
        		try {
    	    		this.aesCipherEncryptDefault = CryptoUtil.getAESCipherEncrypt(this.aesLanKeyByte, this.aesRemoteIVByteDefault);
    			} catch (Throwable ex) {
    				log.error("Error while setting up AES (" + socketType + ")", ex);
    				close();
    				return;
    			}
        		
        		try {
        			final String responsePacketIndex = getNextPacketIndexHex(socketType);
        			
        			log.debug("Send LocalIV to 'HM-LGW' (" + socketType + "): [" + this.aesLocalIVDefault.toLowerCase() + ", packetIndex=" + responsePacketIndex + "]");
        			final String response = "V" + responsePacketIndex + "," +  this.aesLocalIVDefault.toUpperCase();
        			send(socketType, response);
        		} catch(IOException ex) {
        			log.error("Error while setting up AES (" + socketType + ")", ex);
        			close();
    				return;
        		}

        		listenerDefault.setCipher(this.aesCipherDecryptDefault);
        		aesInitializedSocketDefault.getAndSet(true);
        	} else if (socketType.equals(SocketType.KEEPALIVE)) {
    	        this.aesRemoteIVKeepAlive = remoteIV;
    	        this.aesRemoteIVByteKeepAlive = Util.toByteFromHex(remoteIV);
    			log.debug("Received RemoteIV from 'HM-LGW' (" + socketType + "): [" + this.aesRemoteIVKeepAlive + ", packetIndex=" + Util.toHex(packetIndex) + "]");

    	        if (!Util.isHex(this.aesRemoteIVKeepAlive) || this.aesRemoteIVByteKeepAlive.length != 16) {
    				log.warn("RemoteIV received from HM-LGW (" + socketType + ") not in hexadecimal format: " + this.aesRemoteIVKeepAlive);
    				return;
    	        }
    	        
        		try {
    	    		this.aesCipherEncryptKeepAlive = CryptoUtil.getAESCipherEncrypt(this.aesLanKeyByte, this.aesRemoteIVByteKeepAlive);
    			} catch (Throwable ex) {
    				log.error("Error while setting up AES (" + socketType + ")", ex);
    				close();
    				return;
    			}
        		
        		try {
        			final String responsePacketIndex = getNextPacketIndexHex(socketType);
        			
        			log.debug("Send LocalIV to 'HM-LGW' (" + socketType + "): [" + this.aesLocalIVKeepAlive.toLowerCase() + ", packetIndex=" + responsePacketIndex + "]");
        			final String response = "V" + responsePacketIndex + "," +  this.aesLocalIVKeepAlive.toUpperCase();
        			send(socketType, response);
        		} catch(IOException ex) {
        			log.error("Error while setting up AES (" + socketType + ")", ex);
        			close();
    				return;
        		}

        		listenerKeepAlive.setCipher(this.aesCipherDecryptKeepAlive);
        		aesInitializedSocketKeepAlive.getAndSet(true);
        	}
    		
    		/*
    		if (aesInitializedSocketDefault.get() && aesInitializedSocketKeepAlive.get()) {
	    		this.aesInitialized.getAndSet(true);
	    		
	    		//TODO: REMOVE
	    		log.debug("TRY TO INIT GATEWAY...");
	    		
	    		try {
					setupGateway();
				} catch (Throwable ex) {
					log.error("Error initializing gateway", ex);
					close();
				}
    		}
    		*/
		} else if (c == 'V' && !getAESEnabled()) {
			log.error("Device 'HM-LGW' requires AES, but AES was not configured!");
			return;
		} else if (c == 'S') {
			if (socketType.equals(SocketType.DEFAULT)) {
				if (parts.length == 2 && parts[0].length() == 3 && parts[1].length() >= 15 && parts[1].startsWith("BidCoS-over-LAN")) {
					try {
						log.debug("Setup gateway");
						setupGateway(socketType);
					} catch (Throwable ex) {
						log.error("Error while setting up the gateway", ex);
						close();
						return;
					}
				} else {
					log.error("Error initiating: Packet \"S\" has wrong structure. Please check your AES key.");
					close();
					return;
				}
			}
			
			return;
		} else if (c == '>' || c == 'K' || c == 'L') {
			//TODO: REMOVE Log Output
			log.debug("KeepAlive Packet received...");
			return;
		} else if (c == 'E' || c == 'R') {
			final HomeMaticPacket homeMaticPacket = PacketUtil.createPacketByMessageType(PacketUtil.convertLANPacketToBidCos(packet));
			if (homeMaticPacket == null) {
	        	return;
	        }
	        
	        //log.debug("Packet: " + homeMaticPacket);
	        
	        for (IHomeMaticLinkListener listener : getLinkListeners()) {
	        	try {
	        		listener.received(this, homeMaticPacket);
				} catch (Throwable ex) {
					//
				}
	        }
		}
    }
    
    @Override
	public boolean send(final HomeMaticPacket packet) throws SocketException, IOException {
    	//packet.setSenderAddress(address);
    	packet.setMessageCounter(getNextMessageCounter(packet.getDestinationAddress()));
    	
		final String data = "S" + formatHexTime(System.currentTimeMillis()) + ",00,00000000,01," + formatHexTime(System.currentTimeMillis() - startUpTime) + "," + Util.toHex(packet.getData()).substring(2);

		final boolean result = send(SocketType.DEFAULT, data);
		if (!result) {
			decreaseMessageCounter(packet.getDestinationAddress());
		}
		return result;
	}
	
	protected boolean send(final SocketType socketType, final String data) throws SocketException, IOException {
		if (data == null) {
			log.debug("Sending not possible. Data is null.");
			return false;
		}
		return send(socketType, data.getBytes());
	}
	
	protected boolean send(final SocketType socketType, final byte[] data) throws SocketException, IOException {
		if (data == null) {
			log.debug("Sending not possible. Data is null.");
			return false;
		}
		
		final Socket socket = getSocket(socketType);
		
		if (socket == null) {
			log.error("Sending not possible. Socket is null.");
			return false;
		}
		
		final byte[] packet;
        if (getAESEnabled()) {
        	if (this.aesInitialized.get()) {
            	// Encryption enabled and ready
                /* */
	        	log.debug("Encrypt packet");
	            /* */
	        	try {
	                /* */
	            	log.debug("Plain packet >>");
	            	PacketUtil.logPacket(this, data);
	            	log.debug("<<");
	            	/* */
	            	
	            	final Cipher cipherEncrypt = getCipherEncrypt(socketType);
	            	
	                if (cipherEncrypt == null) {
	                	log.error("Encryption not working due to missing cipher");
	                	packet = Util.appendItem(data, EOL);
	                } else {
	                	packet = CryptoUtil.aesCrypt(cipherEncrypt, Util.appendItem(data, EOL));
	                }
	
	                /* */
	            	log.debug("Encrypted packet >>");
	            	PacketUtil.logPacket(this, packet);
	            	log.debug("<<");
	            	/* */
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
		
        if (packet == null) {
        	//log.debug("Packet is null");
        	return false;
        }
        
		try {
			final OutputStream out = socket.getOutputStream();
			out.write(packet);
			out.flush();
            /* */
			log.debug("Sending packet (" + socketType + "): '" + Util.toString(data) + "'");
        	/* */
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
    private boolean sendKeepAlive(final SocketType socketType) {
    	try {
			return send(socketType, "K" + getNextPacketIndexHex(socketType));
		} catch (SocketException ex) {
			log.debug("Error while sending keep alive packet (" + socketType + ")", ex);
		} catch (IOException ex) {
			log.debug("Error while sending keep alive packet (" + socketType + ")", ex);
		}
    	return false;
    }

	@Override
	public void connectionTerminated() {
		close();
	}

	@Override
	protected boolean setupAES() {
    	if (this.getAESEnabled()) {
    		if (this.aesLocalIVDefault == null) {
    			this.aesLocalIVDefault = Util.randomHex(32).toUpperCase();
    			this.aesLocalIVByteDefault = Util.toByteFromHex(this.aesLocalIVDefault);
    		}
    		
    		if (this.aesLocalIVKeepAlive == null) {
    			this.aesLocalIVKeepAlive = Util.randomHex(32).toUpperCase();
    			this.aesLocalIVByteKeepAlive = Util.toByteFromHex(this.aesLocalIVKeepAlive);
    		}

			log.debug("LanKey for 'HM-LGW': [" + this.aesLanKey + "]");
			log.debug("LocalIV for 'HM-LGW' (" + SocketType.DEFAULT + "): [" + this.aesLocalIVDefault + "]");
			log.debug("LocalIV for 'HM-LGW' (" + SocketType.KEEPALIVE + "): [" + this.aesLocalIVKeepAlive + "]");
			
    		try {
	    		this.aesCipherDecryptDefault = CryptoUtil.getAESCipherDecrypt(this.aesLanKeyByte, this.aesLocalIVByteDefault);
			} catch (Throwable ex) {
				log.error("Error while setting up AES (" + SocketType.DEFAULT + ")", ex);
				return false;
			}
    		
    		try {
	    		this.aesCipherDecryptKeepAlive = CryptoUtil.getAESCipherDecrypt(this.aesLanKeyByte, this.aesLocalIVByteKeepAlive);
			} catch (Throwable ex) {
				log.error("Error while setting up AES (" + SocketType.KEEPALIVE + ")", ex);
				return false;
			}
    	}
    	return true;
    }
	
	@Override
    protected void cleanUpAES() {
    	this.aesInitialized.getAndSet(false);
    	this.aesRemoteIVDefault = null;
    	this.aesRemoteIVKeepAlive = null;
    	this.aesRemoteIVByteDefault = null;
    	this.aesRemoteIVByteKeepAlive = null;
    	this.aesLocalIVDefault = null;
    	this.aesLocalIVKeepAlive = null;
    	this.aesLocalIVByteDefault = null;
    	this.aesLocalIVByteKeepAlive = null;
    	this.aesCipherDecryptDefault = null;
    	this.aesCipherDecryptKeepAlive = null;
    	this.aesCipherEncryptDefault = null;
    	this.aesInitializedSocketDefault.getAndSet(false);
    	this.aesInitializedSocketKeepAlive.getAndSet(false);
    }
	
	public boolean setCentralAddress(final String centralAddress) {
		if (centralAddress == null || centralAddress.length() != 6 || !Util.isHex(centralAddress)) {
			return false;
		}
		
		this.centralAddress = centralAddress;
		return true;
	}
	
	public boolean setLANPassword(final String password) {
		if (password == null) {
			return false;
		}
		
		String aesLanKey = null;
		
		try {
			aesLanKey = CryptoUtil.toMD5(password);
		} catch (Throwable ex) {
			return false;
		}
		
		if (aesLanKey == null || aesLanKey.length() != 32 || !Util.isHex(aesLanKey)) {
			return false;
		}
		
		this.aesLanKey = aesLanKey;
		this.aesLanKeyByte = Util.toByteFromHex(aesLanKey);
		return true;
	}
	
	protected boolean setAESLocalIVDefault(final String aesLocalIV) {
		if (aesLocalIV == null || aesLocalIV.length() != 32 || !Util.isHex(aesLocalIV)) {
			return false;
		}
		
		// Only reset everything if key has changed
		if (this.aesLocalIVDefault == null || !this.aesLocalIVDefault.equals(aesLocalIV)) {
			this.aesLocalIVDefault = aesLocalIV;
			this.aesLocalIVByteDefault = Util.toByteFromHex(aesLocalIV);
			setupAES();
		}
		return true;
	}
	
	protected boolean setAESLocalIVKeepAlive(final String aesLocalIV) {
		if (aesLocalIV == null || aesLocalIV.length() != 32 || !Util.isHex(aesLocalIV)) {
			return false;
		}
		
		// Only reset everything if key has changed
		if (this.aesLocalIVKeepAlive == null || !this.aesLocalIVKeepAlive.equals(aesLocalIV)) {
			this.aesLocalIVKeepAlive = aesLocalIV;
			this.aesLocalIVByteKeepAlive = Util.toByteFromHex(aesLocalIV);
			setupAES();
		}
		return true;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public String getSerial() {
		return serial;
	}
	
	public int getKeepAliveInterval() {
		return keepAliveInterval;
	}

	public void setKeepAliveInterval(final int keepAliveInterval) {
		if (keepAliveInterval > 0 && keepAliveInterval < 30) {
			this.keepAliveInterval = keepAliveInterval;
		}
	}
    
    private String formatHexTime(final long time) {
    	return Util.padLeft(Util.toHex(time), 8, "0");
    }
    
    private synchronized int getPacketIndex(final SocketType socketType) {
    	int result = 0;
    	switch (socketType) {
    	case DEFAULT:
    		synchronized (lastPacketIndexLockDefault) {
    			result = lastPacketIndexDefault;
        	}
    		break;
    	case KEEPALIVE:
    		synchronized (lastPacketIndexLockKeepAlive) {
    			result = lastPacketIndexKeepAlive;
        	}
    		break;
    	}
    	return result;
    }
    
    private synchronized int getNextPacketIndex(final SocketType socketType) {
    	int result = 0;
    	switch (socketType) {
    	case DEFAULT:
    		synchronized (lastPacketIndexLockDefault) {
    			result = (lastPacketIndexDefault + 1) % 255;
        		lastPacketIndexDefault = result;
        	}
    		break;
    	case KEEPALIVE:
    		synchronized (lastPacketIndexLockKeepAlive) {
    			result = (lastPacketIndexKeepAlive + 1) % 255;
        		lastPacketIndexKeepAlive = result;
        	}
    		break;
    	}
    	return result;
    }
    
    private synchronized String getPacketIndexHex(final SocketType socketType) {
    	return Util.toHex(getPacketIndex(socketType)).toLowerCase();
    }
    
    private synchronized String getNextPacketIndexHex(final SocketType socketType) {
    	return Util.toHex(getNextPacketIndex(socketType)).toLowerCase();
    }

	private Socket getSocket(final SocketType socketType) {
		switch(socketType) {
		case DEFAULT:
			return socketDefault;
		case KEEPALIVE:
			return socketKeepAlive;
		}
		return null;
	}

	private Cipher getCipherEncrypt(final SocketType socketType) {
		switch(socketType) {
		case DEFAULT:
			return aesCipherEncryptDefault;
		case KEEPALIVE:
			return aesCipherEncryptKeepAlive;
		}
		return null;
	}

}
