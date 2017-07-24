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

import org.apache.log4j.Logger;

import at.creadoo.homematic.MessageCallback;
import at.creadoo.homematic.util.CryptoUtil;
import at.creadoo.homematic.util.Util;

import java.net.Socket;

import javax.crypto.Cipher;

class DecryptingSocketListener implements Runnable {
	
	private static final Logger log = Logger.getLogger(DecryptingSocketListener.class);
	
	private Thread runningThread = null;

	private final MessageCallback callback;

	private final Socket socket;

	/**
	 * Line end marker
	 */
	private final byte[] endOfLineMarker;

	private Object cipherLock = new Object();
	private Cipher cipher = null;

	public DecryptingSocketListener(final MessageCallback callback, final Socket socket, final byte[] endOfLineMarker) {
		this.callback = callback;
		this.socket = socket;
		this.endOfLineMarker = endOfLineMarker;
		this.cipher = null;
	}

	public DecryptingSocketListener(final MessageCallback callback, final Socket socket, final byte[] endOfLineMarker, final Cipher cipher) {
		this.callback = callback;
		this.socket = socket;
		this.endOfLineMarker = endOfLineMarker;
		this.cipher = cipher;
	}
	
	public void setCipher(final Cipher cipher) {
		synchronized (cipherLock) {
			this.cipher = cipher;
		}
	}
	
	@Override
	public void run() {
		log.debug(Thread.currentThread().getName() + " : Starting to observe");
		
        final int bufferMax = 2048;
        byte[] buffer = new byte[bufferMax];

		try {
	        int offset = 0;
	        
			while (!Thread.interrupted()) {
				final byte[] buff = new byte[1];
				final int bytesRead = socket.getInputStream().read(buff);
				if (bytesRead <= 0) {
					// No data read simply skip processing for this round
					continue;
				}
				
				synchronized (cipherLock) {
					
					// Decrypt
					if (this.cipher != null) {
						final byte[] decrypted = CryptoUtil.aesCrypt(this.cipher, buff);
						if (decrypted != null) {
							// Copy bytes
				            System.arraycopy(decrypted, 0, buffer, offset, decrypted.length);
							
				            // Calculate next offset
				        	offset = offset + decrypted.length;
						}
					
					// Plain mode
					} else {
						// Copy bytes
			            System.arraycopy(buff, 0, buffer, offset, bytesRead);

			            // Calculate next offset
			        	offset = offset + bytesRead;
					}
				}
	            
	            // Check for line end and send packet
	            int lineEndMarker = Util.indexOf(buffer, endOfLineMarker);
	            if (lineEndMarker > -1) {
	            	do {
		            	final byte[] packet = new byte[lineEndMarker];
						System.arraycopy(buffer, 0, packet, 0, lineEndMarker);
						
						final int restLength = offset - lineEndMarker - endOfLineMarker.length;
						if (restLength > 0) {
							final byte[] rest = new byte[restLength];
							System.arraycopy(buffer, lineEndMarker + endOfLineMarker.length, rest, 0, restLength);
	
							buffer = new byte[bufferMax];
							System.arraycopy(rest, 0, buffer, 0, restLength);
							offset = restLength;
						} else {
							buffer = new byte[bufferMax];
							offset = 0;
						}
						
			            callback.received(this, packet);
			            
			            lineEndMarker = Util.indexOf(buffer, endOfLineMarker);
	            	} while (lineEndMarker > -1);
	            }
			}
		} catch (Throwable ex) {
			log.error(Thread.currentThread().getName() + ": Error while reading data", ex);
			if (callback != null) {
				callback.connectionTerminated();
			}
			stop();
		}
		
		log.debug(Thread.currentThread().getName() + " : Stopping to observe");
	}

	public boolean isRunning() {
		if (runningThread != null) {
			return runningThread.isAlive();
		}
		return false;
	}
	
	/**
	 * Opens the device in non-blocking mode, and starts observing it.
	 */
	public void start() {
		if (!isRunning()) {
			runningThread = new Thread(this, this.getClass().getSimpleName() + " [" + socket.getInetAddress() + "]");
			runningThread.start();
		} else {
			log.error("Already running");
		}
	}

	/**
	 * Stops observing the device, and closes the connection
	 */
	public void stop() {
		if (isRunning()) {
			try {
				runningThread.interrupt();
			} catch (Throwable e) {
				log.error("Unable to close", e);
			}
		} else {
			log.debug("Not running");
		}
	}

}
