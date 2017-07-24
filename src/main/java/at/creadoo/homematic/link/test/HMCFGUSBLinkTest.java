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
package at.creadoo.homematic.link.test;

import java.io.IOException;
import java.net.SocketException;

import org.apache.log4j.Logger;

import at.creadoo.homematic.HomeMaticStatus;
import at.creadoo.homematic.link.HMCFGUSBLink;
import at.creadoo.homematic.packet.HomeMaticPacketSet;

public class HMCFGUSBLinkTest {

	private static final Logger log = Logger.getLogger(HMCFGUSBLinkTest.class);

	public static void main(final String[] args) {
		/*
		PacketUtil.logPacket(new HomeMaticPacket(0x26, 0x0, HomeMaticMessageType.EVENT, 0x31e00f, 0x0).getData());
		PacketUtil.logPacket(new HomeMaticPacketEvent(0x26, 0x31e00f, 0x0, HomeMaticStatus.OFF).getData());
		PacketUtil.logPacket(new HomeMaticPacketSet(0x26, 0x31e00f, 0x35366f, HomeMaticStatus.ON).getData());
		*/
		
		final HMCFGUSBLink hardware = new HMCFGUSBLink();
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
