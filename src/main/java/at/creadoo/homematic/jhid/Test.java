package at.creadoo.homematic.jhid;

import java.io.IOException;
import java.net.SocketException;

import org.apache.log4j.Logger;

import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.HomeMaticStatus;
import at.creadoo.homematic.packets.HomeMaticPacket;
import at.creadoo.homematic.packets.HomeMaticPacketEvent;
import at.creadoo.homematic.packets.HomeMaticPacketSet;
import at.creadoo.homematic.util.PacketUtil;

public class Test {

	private static final Logger log = Logger.getLogger(Test.class);

	public static void main(final String[] args) {
		/*
		PacketUtil.logPacket(new HomeMaticPacket(0x26, 0x0, HomeMaticMessageType.EVENT, 0x31e00f, 0x0).getData());
		PacketUtil.logPacket(new HomeMaticPacketEvent(0x26, 0x31e00f, 0x0, HomeMaticStatus.OFF).getData());
		PacketUtil.logPacket(new HomeMaticPacketSet(0x26, 0x31e00f, 0x35366f, HomeMaticStatus.ON).getData());
		*/
		
		final HidLink hardware = new HidLink();
		hardware.setUsbProductId("0xC00F");
		hardware.setUsbVendorId("0x1B1F");
		try {
			hardware.start();
		} catch (Exception ex) {
			log.error("Error", ex);
		}
		
		try {
			Thread.sleep(1000);
			if (!hardware.send(new HomeMaticPacketSet(0x26, 0x31e00f, 0x35366f, HomeMaticStatus.ON))) {
				log.warn("Error sending packet");
			}
		} catch (SocketException ex) {
			log.error("Error", ex);
		} catch (IOException ex) {
			log.error("Error", ex);
		} catch (InterruptedException ex) {
			log.error("Error", ex);
		}
	}

}
