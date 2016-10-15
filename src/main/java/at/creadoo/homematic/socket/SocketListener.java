package at.creadoo.homematic.socket;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import at.creadoo.homematic.MessageCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

class SocketListener implements Runnable {
	private static final Logger log = Logger.getLogger(SocketListener.class);

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final MessageCallback callback;

	private Socket socket;

	public SocketListener(final MessageCallback callback, final Socket socket) {
		this.callback = callback;
		this.socket = socket;
	}

	@Override
	public void run() {
		log.debug(Thread.currentThread().getName() + " : Starting to observe");

		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				log.debug(this.getClass().getSimpleName() +  " received: " + line);
				callback.received(line.getBytes());
			}
		} catch (final IOException ex) {
			log.error("IP thread exception", ex);
			if (callback != null) {
				callback.connectionTerminated();
			}
		} finally {
			// Close the stream reader
			IOUtils.closeQuietly(bufferedReader);
		}
		
		log.debug(Thread.currentThread().getName() + " : Stopping to observe");
	}

	/**
	 * Opens the device in non-blocking mode, and starts observing it.
	 */
	public void start() {
		if (!running.getAndSet(true)) {
			new Thread(this, this.getClass().getSimpleName() + " [" + socket.getInetAddress() + "]").start();
		} else {
			System.out.println("Already running");
		}
	}

	/**
	 * Stops observing the device, and closes the connection
	 */
	public void stop() {
		if (running.getAndSet(false)) {
			try {
				socket.close();
			} catch (Throwable e) {
				log.error("Unable to close", e);
			}
		} else {
			log.error("Not running");
		}
	}

	/**
	 * Writes the bytes to the device.
	 *
	 * @param buf
	 *            buffer to send
	 * @return {@code true} if writ succeeded.
	 */
	public Boolean write(byte[] buf) {
		if (socket == null) {
			log.error("Error writing data to socket: Socket null");
			return false;
		}
		try {
			// open the socket for writing
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			// write telegram string to the socket
			out.write(new String(buf));
			// force sending it
			out.flush();
			log.debug("Sending packet over IP");
		} catch (IOException ex) {
			log.error("Error writing data to socket: IO exception", ex);
			return false;
		}
		return true;
	}

}
