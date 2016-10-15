package at.creadoo.homematic.util;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.ILink;
import at.creadoo.homematic.packets.HomeMaticPacket;
import at.creadoo.homematic.packets.HomeMaticPacketEvent;
import at.creadoo.homematic.packets.HomeMaticPacketInformation;
import at.creadoo.homematic.packets.HomeMaticPacketSet;

public class Util {

	private static final Logger log = Logger.getLogger(Util.class);

	public static final String CONFIG_PREFIX = "homematic.";
	public static final String MODE_HID = "hid";
	public static final String MODE_IP = "ip";

	public static void logPacket(final byte[] data) {
        if (log.isDebugEnabled() && data != null) {
            log.debug("String: " + new String(data) + " (Len: " + new String(data).length() + ")");
            log.debug("Hex:    " + toHex(data, true));
        }
    }

	public static void logPacket(final ILink link, final byte[] data) {
        if (log.isDebugEnabled() && data != null) {
            log.debug("Packet received at link '" + link.getName() + "': ");
            logPacket(data);
        }
    }
	
	public static int toInt(final byte data) {
		return ((int) data) & 0xFF;
	}
	
	public static int toInt(final String data) {
		return Integer.valueOf(data).intValue();
	}
	
	public static int toIntFromHex(final String data) {
		return Integer.valueOf(data, 16).intValue();
	}
	
	public static int[] toIntArray(final byte[] data) {
		final int[] result = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = toInt(data[i]);
		}
		return result;
	}
	
	public static byte toByte(final int data) {
		return Integer.valueOf(data).byteValue();
	}
	
	public static byte[] toByteArray(final int[] data) {
		final byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = toByte(data[i]);
		}
		return result;
	}
	
	public static String toHex(final byte data) {
		return String.format("%02x", Byte.valueOf(data)).toUpperCase();
	}

	public static String toHex(final int data) {
		return String.format("%02x", Integer.valueOf(data)).toUpperCase();
	}

	public static String toHex(final int[] data, final boolean prettyPrint) {
		return toHex(toByteArray(data), prettyPrint);
    }

	public static String toHex(final long data) {
		return String.format("%02x", Long.valueOf(data)).toUpperCase();
	}
	
	public static String toHex(final String data) {
		return toHex(data.getBytes());
	}
    
	public static String toHex(final String data, final boolean prettyPrint) {
		return toHex(data.getBytes(), prettyPrint);
	}
	
	public static String toHex(final byte[] data) {
		return toHex(data, false);
	}
	
	public static String toHex(final byte[] data, final boolean prettyPrint) {
        final StringBuilder sb = new StringBuilder();
        for (byte aD : data) {
            final int val = 0xFF & (int) aD;
            sb.append(String.format("%02x", val).toUpperCase());
            if (prettyPrint) {
            	sb.append(" ");
            }
        }
        return sb.toString();
    }
    
	public static byte[] toByteFromHex(final byte[] data) {
		return toByteFromHex(toString(data));
	}
    
	public static byte[] toByteFromHex(final String data) {
		final String temp = data.toLowerCase();
		int len = temp.length();
		byte[] result = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			result[i / 2] = (byte) ((Character.digit(temp.charAt(i), 16) << 4) + Character.digit(temp.charAt(i + 1), 16));
		}
		return result;
	}
    
	public static String toStringFromHex(final byte[] data) {
		return new String(toByteFromHex(data));
	}
    
	public static String toStringFromHex(final String s) {
		return new String(toByteFromHex(s));
	}

	public static byte[] subset(final byte[] data, final int start) {
		return subset(data, start, data.length - 1);
	}

	public static byte[] subset(final byte[] data, final int start, final int end) {
		if (data == null || start < 0 || end < start || end >= data.length) {
			return null;
		}
		
		byte[] result = new byte[end - start + 1];
		for (int i = start; i <= end; i++) {
			result[i - start] = data[i];
		}
		return result;
	}

	public static String toString(final byte[] data) {
		if (data == null || data.length <= 0) {
			return "";
		}
		return toString(data, 0, data.length - 1);
	}

	public static String toString(final byte[] data, final int start) {
		return toString(data, start, data.length);
	}

	public static String toString(final byte[] data, final int start, final int end) {
		byte[] result = subset(data, start, end);
		if (result == null || result.length <= 0) {
			return "";
		}
		return new String(result);
	}
	
	public static String padRight(final String data, final int size, final String padChar) {
		return padRight(data, size, padChar.charAt(0));
	}
	
	public static String padRight(final String data, final int size, final char padChar) {
		final StringBuilder padded = new StringBuilder(data);
		while (padded.length() < size) {
			padded.append(padChar);
		}
		return padded.toString();
	}
	
	public static String padLeft(final String data, final int size, final String padChar) {
		return padLeft(data, size, padChar.charAt(0));
	}
	
	public static String padLeft(final String data, final int size, final char padChar) {
		final StringBuilder padded = new StringBuilder();
		while (padded.length() < size - data.length()) {
			padded.append(padChar);
		}
		padded.append(data);
		return padded.toString();
	}
	
	public static byte[] appendItem(final byte[] data, final byte item) {
		byte[] result = new byte[data.length + 1];
		System.arraycopy(data, 0, result, 0, data.length);
		result[data.length - 1] = item;
		return result;
	}
	
	public static byte[] appendItem(final byte[] data, final byte[] items) {
		byte[] result = new byte[data.length + items.length];
		System.arraycopy(data, 0, result, 0, data.length);
		System.arraycopy(items, 0, result, data.length, items.length);
		return result;
	}
	
	public static byte[] appendItem(final byte[] data, final int item) {
		return appendItem(data, toByte(item));
	}
	
	public static byte[] prependItem(final byte[] data, final byte item) {
		byte[] result = new byte[data.length + 1];
		System.arraycopy(data, 0, result, 1, data.length);
		result[0] = item;
		return result;
	}
	
	public static byte[] prependItem(final byte[] data, final byte[] items) {
		byte[] result = new byte[data.length + items.length];
		System.arraycopy(items, 0, result, 0, items.length);
		System.arraycopy(data, 0, result, items.length, data.length);
		return result;
	}
	
	public static byte[] prependItem(final byte[] data, final int item) {
		return prependItem(data, toByte(item));
	}
	
	public static boolean isBitSet(final int i, final int bit) {
	    return isBitSet(Integer.valueOf(i).byteValue(), bit);
	}
	
	public static boolean isBitSet(final byte b, final int bit) {
	    int bitPosition = bit % 8;  // Position of this bit in a byte

	    return (b >> bitPosition & 1) == 1;
	}
	
	public static void setBit(final int i, final int bit) {
		setBit(Integer.valueOf(i).byteValue(), bit);
	}
	
	public static void setBit(byte b, final int bit) {
	    int bitPosition = bit % 8;  // Position of this bit in a byte

	    b |= (1 << bitPosition);
	}
	
	public static void unsetBit(final int i, final int bit) {
		unsetBit(Integer.valueOf(i).byteValue(), bit);
	}
	
	public static void unsetBit(byte b, final int bit) {
	    int bitPosition = bit % 8;  // Position of this bit in a byte

	    b &= ~(1 << bitPosition);
	}
	
	public static int bitsToInt(final int i, final int offset, final int bits) {
	    final int rightShifted = i >>> offset;
	    final int mask = (1 << bits) - 1;
	    return rightShifted & mask;
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
				
				final HomeMaticPacket homeMaticPacket = Util.createPacketByMessageType(Util.prependItem(b, b.length), rssi);
				if (homeMaticPacket == null) {
		        	return null;
		        }

				log.debug("Packet (LAN):");
				Util.logPacket(packet);

				log.debug("Packet (Converted):");
				Util.logPacket(homeMaticPacket.getData());
				
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
		
		final int id = toInt(data[3]);
		log.debug("Message Type ID: " + id + " (0x" + Util.toHex(id) + ")");
		final HomeMaticMessageType messageType = HomeMaticMessageType.getById(id);
		
		switch (messageType) {
		case EVENT:
			return new HomeMaticPacketEvent(data, rssi);
		case INFORMATION:
			return new HomeMaticPacketInformation(data, rssi);
		case SET:
			return new HomeMaticPacketSet(data, rssi);
		default:
			log.debug("Packet type not found");
			return null;
		}
	}
	
    public static String hardwareIdToString(final Long hardwareId) {
    	if (hardwareId != null) {
    		return "HardwareId: " + hardwareId +" (" + toHex(hardwareId) + ")";
    	}
    	return "";
	}
    
    public static Map<String, String> dictionaryToMap(final Dictionary<String, ?> source) {
    	final Map<String, String> sink = new HashMap<String, String>();
        for (Enumeration<String> keys = source.keys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            sink.put(key, source.get(key).toString());
        }
        return sink;
    }

	/**
	 * Extracts a specific property key subset from the map passed. The
	 * prefix may be removed from the keys in the resulting map, or it
	 * may be kept. In the latter case, exact matches on the prefix will also be
	 * copied into the resulting map.
	 *
	 *
	 * @param prefix
	 *            is the key prefix to filter the map keys by.
	 * @param keepPrefix
	 *            if true, the key prefix is kept in the resulting map.
	 *            As side-effect, a key that matches the prefix exactly will
	 *            also be copied. If false, the resulting dictionary's keys are
	 *            shortened by the prefix. An exact prefix match will not be
	 *            copied, as it would result in an empty string key.
	 * @return a map matching the filter key. May be an empty
	 *         map, if no prefix matches were found.
	 *
	 * @see Map#get(String) Map.get() is used to assemble matches
	 */
	public static Map<String, String> matchingSubset(final Map<String, String> properties, final String prefix, final boolean keepPrefix) {
		final Map<String, String> result = new HashMap<String, String>();

		// sanity check
		if (prefix == null || prefix.length() == 0) {
			return result;
		}

		String prefixMatch; // match prefix strings with this
		String prefixSelf; // match self with this
		if (prefix.charAt(prefix.length() - 1) != '.') {
			// prefix does not end in a dot
			prefixSelf = prefix;
			prefixMatch = prefix + '.';
		} else {
			// prefix does end in one dot, remove for exact matches
			prefixSelf = prefix.substring(0, prefix.length() - 1);
			prefixMatch = prefix;
		}
		// POSTCONDITION: prefixMatch and prefixSelf are initialized!

		// now add all matches into the resulting properties.
		// Remark 1: #propertyNames() will contain the System properties!
		// Remark 2: We need to give priority to System properties. This is done
		// automatically by calling this class's getProperty method.
		for (String key : properties.keySet()) {
			if (keepPrefix) {
				// keep full prefix in result, also copy direct matches
				if (key.startsWith(prefixMatch) || key.equals(prefixSelf)) {
					result.put(key, properties.get(key));
				}
			} else {
				// remove full prefix in result, don't copy direct matches
				if (key.startsWith(prefixMatch)) {
					result.put(key.substring(prefixMatch.length()), properties.get(key));
				}
			}
		}

		// done
		return result;
	}
	
	public static String randomHex(final int length) {
		final Random randomService = new Random();
		final StringBuilder sb = new StringBuilder();
		while (sb.length() < length) {
			sb.append(Integer.toHexString(randomService.nextInt(16)));
		}
		sb.setLength(length);
		return sb.toString().toLowerCase();
	}

}
