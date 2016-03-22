package at.creadoo.homematic.jhid;

import org.apache.log4j.Logger;
import org.hid4java.HidDevice;

import at.creadoo.homematic.MessageCallback;

import java.util.concurrent.atomic.AtomicBoolean;

public class HidListener implements Runnable {
	private static final Logger log = Logger.getLogger(HidListener.class);

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final MessageCallback callback;

	private HidDevice device;

	public HidListener(final MessageCallback callback, final HidDevice device) {
		this.callback = callback;
		this.device = device;
	}

	@Override
	public void run() {
		log.debug(Thread.currentThread().getName() + " : Starting to observe");

		final byte[] buf = new byte[256];
		while (running.get()) {
			try {
				Thread.sleep(100); // Non blocking, so pause a bit
				final int len = device.read(buf);
				if (len <= 0) {
					continue;
				}

				// create a copy of the relevant bytes
				final byte[] data = new byte[len];
				System.arraycopy(buf, 0, data, 0, len);
				callback.received(data);
			} catch (Throwable e) {
				log.warn("Error reading from device", e);
			}
		}

		log.debug(Thread.currentThread().getName() + " : Stopping to observe");

	}

	/**
	 * Opens the device in non-blocking mode, and starts observing it.
	 */
	public void start() {
		if (!running.getAndSet(true)) {
			device.open();

			new Thread(this, "Homematic HID Listener '" + device.getSerialNumber() + "'").start();
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
				device.close();
			} catch (Throwable e) {
				log.error("Unable to close", e);
			}
		} else {
			log.error("Not running");
		}
	}

	/**
	 * Writes the bytes to the device.
	 * <p/>
	 * Returns {@code true} if number of written bytes equals length of the
	 * buffer, {@code false} if otherwise.
	 *
	 * @param buf
	 *            buffer to send
	 * @return {@code true} if number of written bytes equals length of the
	 *         buffer, {@code false} if otherwise.
	 */
	public boolean write(final byte[] buf) {
		final int result = device.write(buf, buf.length, (byte) 0);
		if (result != -1) {
			log.debug("Bytes written: " + result);
		} else {
			log.error("Error while writing to device: " + device.getLastErrorMessage());
		}
		
		return (buf.length == result);

	}

}
