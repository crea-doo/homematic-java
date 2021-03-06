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
package at.creadoo.homematic;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import at.creadoo.homematic.HomeMaticError;
import at.creadoo.homematic.HomeMaticStatus;
import at.creadoo.homematic.packet.HomeMaticPacket;
import at.creadoo.homematic.packet.HomeMaticPacketEvent;
import at.creadoo.homematic.packet.HomeMaticPacketInformation;
import at.creadoo.homematic.packet.HomeMaticPacketRemote;
import at.creadoo.homematic.util.PacketUtil;
import at.creadoo.homematic.util.Util;

public class TestPacket {
	
	private static final Logger log = Logger.getLogger(TestPacket.class);

	@BeforeMethod
	public void setUp() throws Exception {
		log.debug("\n");
	}

	@AfterMethod
	public void tearDown() throws Exception {
		log.debug("\n\n");
	}
	
	private HomeMaticPacket packetFromString(final String bytes) {
		final byte[] b = Util.toByteFromHex(bytes.replace(" ", ""));
		PacketUtil.logPacket(b);
		
		final HomeMaticPacket p = PacketUtil.createPacket(b);
		if (p == null) {
			return null;
		}
		log.debug("Payload: " + Util.toHex(p.getPayload(), true));
		log.debug("Packet: " + p);
		
		return p;
	}
	
	private HomeMaticPacket packetFromStringLAN(final String bytes) {
		final byte[] b = Util.toByteFromHex(bytes.replace(" ", ""));
		
		final HomeMaticPacket p = PacketUtil.createPacketByMessageType(PacketUtil.convertLANPacketToBidCos(b));
		if (p == null) {
			return null;
		}
		log.debug("Payload: " + Util.toHex(p.getPayload(), true));
		log.debug("Packet: " + p);
		
		return p;
	}

	@Test
	public void testPacketEquals() {
		log.debug("testPacketEquals");

		final HomeMaticPacketEvent p1 = (HomeMaticPacketEvent) packetFromString("45 31 e0 0f 00 00 00 44 0f 35 ff ff ba 0c 08 84 41 31 e0 0f 00 00 00 01 aa 00 0e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		final HomeMaticPacketEvent p2 = (HomeMaticPacketEvent) packetFromString("45 31 e0 0f 00 00 00 44 0f 35 ff ff ba 0c 08 84 41 31 e0 0f 00 00 00 01 aa 00 0e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		Assert.assertNotNull(p1);
		Assert.assertNotNull(p2);
		Assert.assertEquals(p1, p2);
		
		final HomeMaticPacketEvent p3 = (HomeMaticPacketEvent) packetFromString("45 31 e0 0f 00 00 00 44 1d db ff ff b8 0c 09 84 41 31 e0 0f 00 00 00 01 ab c8 0e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		Assert.assertNotNull(p3);
		Assert.assertNotEquals(p1, p3);
		Assert.assertNotEquals(p2, p3);
	}

	@Test
	public void testPacketEventContactOff() {
		log.debug("testPacketEventContactOff");

		final HomeMaticPacket p = packetFromString("45 31 e0 0f 00 00 00 44 0f 35 ff ff ba 0c 08 84 41 31 e0 0f 00 00 00 01 aa 00 0e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		if (p instanceof HomeMaticPacketEvent) {
			final HomeMaticPacketEvent e = (HomeMaticPacketEvent) p;
			Assert.assertFalse(e.getLowBat());
			Assert.assertEquals(e.getStatus(), HomeMaticStatus.OFF);
		} else {
			Assert.fail("Wrong packet type");
		}
	}

	@Test
	public void testPacketEventContactOn() {
		log.debug("testPacketEventContactOn");

		final HomeMaticPacket p = packetFromString("45 31 e0 0f 00 00 00 44 1d db ff ff b8 0c 09 84 41 31 e0 0f 00 00 00 01 ab c8 0e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		if (p instanceof HomeMaticPacketEvent) {
			final HomeMaticPacketEvent e = (HomeMaticPacketEvent) p;
			Assert.assertFalse(e.getLowBat());
			Assert.assertEquals(e.getStatus(), HomeMaticStatus.ON);
		} else {
			Assert.fail("Wrong packet type");
		}
	}

	@Test
	public void testPacketInformationSabotage() {
		log.debug("testPacketInformationSabotage");

		final HomeMaticPacket p = packetFromString("45 31 e0 0f 00 00 00 02 cc e5 ff ff b9 0d 01 86 10 31 e0 0f 00 00 00 06 01 c8 0e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		if (p instanceof HomeMaticPacketInformation) {
			final HomeMaticPacketInformation i = (HomeMaticPacketInformation) p;
			Assert.assertFalse(i.getLowBat());
			Assert.assertEquals(i.getStatus(), 200);
			Assert.assertEquals(i.getError(), HomeMaticError.SABOTAGE);
		} else {
			Assert.fail("Wrong packet type");
		}
	}

	@Test
	public void testPacketRemote() {
		log.debug("testPacketRemote");

		final HomeMaticPacket p = packetFromString("45 33 73 A6 00 00 00 F0 4F 6C FF FF D9 0B B9 84 40 33 73 A6 00 00 00 01 B7 C8 0E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		if (p instanceof HomeMaticPacketRemote) {
			final HomeMaticPacketRemote r = (HomeMaticPacketRemote) p;
			Assert.assertEquals(r.getChannel(), 1);
			Assert.assertFalse(r.getLongPress());
			Assert.assertEquals(r.getCounter(), 183);
		} else {
			Assert.fail("Wrong packet type");
		}
	}

	//@Test
	public void testPacketEventSmokeDetectorTest() {
		log.debug("testPacketEventSmokeDetectorTest");

		final HomeMaticPacket p = packetFromStringLAN("45 33 33 32 32 41 34 2C 30 30 30 30 2C 30 30 30 31 35 38 30 37 2C 46 46 2C 46 46 44 42 2C 30 34 38 34 30 30 33 33 32 32 41 34 30 30 30 30 30 30 31 31 30 30 34 32 34 43 34 35 35 31 33 30 33 37 33 32 33 34 33 32 33 30 33 35 43 44 30 30 30 31 30 30");
		if (p instanceof HomeMaticPacketInformation) {
			final HomeMaticPacketInformation i = (HomeMaticPacketInformation) p;
			Assert.assertFalse(i.getLowBat());
			Assert.assertEquals(i.getStatus(), 200);
			Assert.assertEquals(i.getError(), HomeMaticError.SABOTAGE);
		} else {
			Assert.fail("Wrong packet type");
		}
	}

	@Test
	public void testPacketEventSmokeDetectorOn() {
		log.debug("testPacketEventSmokeDetectorOn");

		final HomeMaticPacket p = packetFromStringLAN("45 33 33 32 32 41 34 2C 30 30 30 30 2C 30 30 30 34 36 42 37 44 2C 46 46 2C 46 46 44 42 2C 34 35 39 34 34 31 33 33 32 32 41 34 33 33 32 32 41 34 30 31 30 31 43 38");
		if (p instanceof HomeMaticPacketEvent) {
			final HomeMaticPacketEvent e = (HomeMaticPacketEvent) p;
			Assert.assertFalse(e.getLowBat());
			Assert.assertEquals(e.getStatus(), HomeMaticStatus.ON);
		} else {
			Assert.fail("Wrong packet type");
		}
	}

	@Test
	public void testPacketEventSmokeDetectorOff() {
		log.debug("testPacketEventSmokeDetectorOff");

		final HomeMaticPacket p = packetFromStringLAN("45 33 33 32 32 41 34 2C 30 30 30 30 2C 30 30 30 35 32 30 31 41 2C 46 46 2C 46 46 44 46 2C 34 39 39 34 34 31 33 33 32 32 41 34 33 33 32 32 41 34 30 31 30 32 30 31");
		if (p instanceof HomeMaticPacketEvent) {
			final HomeMaticPacketEvent e = (HomeMaticPacketEvent) p;
			Assert.assertFalse(e.getLowBat());
			//Assert.assertEquals(e.getStatus(), 200);
			//Assert.assertEquals(e.getError(), HomeMaticError.SABOTAGE);
		} else {
			Assert.fail("Wrong packet type");
		}
	}

}
