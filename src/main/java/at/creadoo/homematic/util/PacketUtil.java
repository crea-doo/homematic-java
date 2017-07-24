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
package at.creadoo.homematic.util;

import java.util.Arrays;

import org.apache.log4j.Logger;

import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.ILink;
import at.creadoo.homematic.packet.HomeMaticPacket;
import at.creadoo.homematic.packet.HomeMaticPacketEvent;
import at.creadoo.homematic.packet.HomeMaticPacketInformation;
import at.creadoo.homematic.packet.HomeMaticPacketRemote;
import at.creadoo.homematic.packet.HomeMaticPacketSet;

public final class PacketUtil {

    private static final Logger log = Logger.getLogger(PacketUtil.class);

	private PacketUtil() {
		//
	}

	public static void logPacket(final byte[] data) {
        if (log.isDebugEnabled() && data != null) {
        	final String sContent = new String(data);
        	final String hContent = Util.toHex(data, true);
        	
            log.debug("String: " + sContent + " (Len: " + sContent.length() + ")");
            log.debug("Hex:    " + hContent + " (Len: " + data.length + ")");
        }
    }

	public static void logPacket(final ILink link, final byte[] data) {
        if (log.isDebugEnabled() && data != null) {
            log.debug("Packet for/from link '" + link.getName() + "': ");
            logPacket(data);
        }
    }
	
	public static int convertRSSI(final int rssi) {
		final int result;
		
		//If RSSI_dec ≥ 128 then RSSI_dBm =
		//(RSSI_dec - 256)/2 – RSSI_offset
		//Else if RSSI_dec < 128 then RSSI_dBm =
		//(RSSI_dec)/2 – RSSI_offset
		if (rssi >= 128)
			result = ((rssi - 256) / 2) - 74;
		else
			result = (rssi / 2) - 74;
		
		return result;
	}

	public static byte[] convertLANPacketToBidCos(final byte[] packet) {
    	final String[] parts = new String(packet).split(",");
        
        char c = (char) packet[0];

        if (c == 'E' || c == 'R') {
			if (parts.length < 6) {
				log.warn("Invalid packet: " + Util.toHex(packet));
				return null;
			}
			
			/*
			Index	Meaning
			0		Sender address
			1		Control and status byte
			2		Time received
			3		AES key index?
			4		RSSI
			5		BidCoS packet
			*/

			int tempNumber = Integer.parseInt(parts[1], 16);
			
			/*
			00: Not set
			01: Packet received, wait for AES handshake
			02: High load
			04: Overload
			*/
			int statusByte = tempNumber >> 8;

			log.debug("tempNumber: " + parts[1]);
			log.debug("tempNumber: " + tempNumber);
			log.debug("statusByte: " + statusByte);
			
			if (statusByte == 4) {
				log.warn("HM-CFG-LAN reached 1% rule.");
			} else if(statusByte == 2) {
				log.warn("HM-CFG-LAN nearly reached 1% rule.");
			}
			
			/*
			00: Not set
			01: ACK or ACK was sent in response to this packet
			02: Message without BIDI bit was sent
			08: No response after three tries
			21: ?
			2*: ?
			30: AES handshake not successful
			4*: AES handshake successful
			50: AES handshake not successful
			8*: ?
			*/
			int controlByte = tempNumber & 0xFF;
			log.debug("controlByte: " + controlByte);
			
			if (parts[5].length() > 18) { // 18 is minimal packet length
				byte[] b = Util.toByteFromHex(parts[5]);
				
				if (b.length < 10) {
					log.warn("Invalid packet: " + Util.toHex(packet));
					return null;
				} else if (b.length > 200) {
					log.warn("Tried to import BidCoS packet larger than 200 bytes.");
					return null;
				}
				
				int rssi = Util.toIntFromHex(parts[4].substring(parts[4].length() - 2));
				//Convert to TI CC1101 format
				if (rssi <= -75)
					rssi = ((rssi + 74) * 2) + 256;
				else
					rssi = (rssi + 74) * 2;
				
				final HomeMaticPacket homeMaticPacket = createPacketByMessageType(Util.prependItem(b, b.length), rssi);
				if (homeMaticPacket == null) {
		        	return null;
		        }

				log.debug("Packet (LAN):");
				logPacket(packet);

				log.debug("Packet (Converted):");
				logPacket(homeMaticPacket.getData());
				
				return homeMaticPacket.getData();
        	} else {
        		log.warn("Packet too short: " + Util.toHex(packet));
        		return null;
        	}
		}

        return null;
	}

	// Procedure for the initialization of new HomeMaticPacket:
	public static HomeMaticPacket createPacket(final byte[] data) {
		char c = (char) data[0];
		log.debug("Packet type '" + c + "'");
		
		switch (c) {
		case 'E':
			int copyLen = Util.toInt(data[13]) + 1;
			byte[] tmp = new byte[copyLen];
			try {
				System.arraycopy(data, 13, tmp, 0, copyLen);
				return createPacketByMessageType(tmp, Util.toInt(data[12]));
			} catch (Throwable ex) {
				log.error("Error parsing message", ex);
			}
			break;
		case 'R':
		case 'I':
			log.debug("Type '" + c + "' not yet implemented");
			break;
		default:
			log.debug("Unkown message: [ " + Arrays.toString(data) + " ]");
			break;
		}
		return null;
	}
	
	public static HomeMaticPacket createPacketByMessageType(final byte[] data) {
		return createPacketByMessageType(data, 0);
	}

	public static HomeMaticPacket createPacketByMessageType(final byte[] data, final int rssi) {
		if (data == null) {
			return null;
		}
		
		final int id = Util.toInt(data[3]);
		log.debug("Message Type ID: " + id + " (0x" + Util.toHex(id) + ")");
		final HomeMaticMessageType messageType = HomeMaticMessageType.getById(id);
		final HomeMaticPacket homeMaticPacket;
		
		switch (messageType) {
		case EVENT:
			homeMaticPacket = new HomeMaticPacketEvent(data, rssi);
			break;
		case INFORMATION:
			homeMaticPacket = new HomeMaticPacketInformation(data, rssi);
			break;
		case REMOTE:
			homeMaticPacket = new HomeMaticPacketRemote(data, rssi);
			break;
		case SET:
			homeMaticPacket = new HomeMaticPacketSet(data, rssi);
			break;
		default:
			log.debug("Packet type not found");
			homeMaticPacket = null;
			break;
		}
		
		return homeMaticPacket;
	}

}
