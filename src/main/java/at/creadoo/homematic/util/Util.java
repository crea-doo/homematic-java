package at.creadoo.homematic.util;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Util {

	public static final String CONFIG_PREFIX = "homematic.";
	public static final String MODE_HID = "hid";
	public static final String MODE_IP = "ip";
	
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
	
	public static boolean isHex(final String data) {
		if (data == null) {
			return false;
		}
		return data.matches("\\p{XDigit}+");
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

	public static int[] subset(final int[] data, final int start) {
		return subset(data, start, data.length - 1);
	}

	public static int[] subset(final int[] data, final int start, final int end) {
		if (data == null || start < 0 || end < start || end >= data.length) {
			return null;
		}
		
		int[] result = new int[end - start + 1];
		for (int i = start; i <= end; i++) {
			result[i - start] = data[i];
		}
		return result;
	}

	public static boolean endsWith(final byte[] data, final byte[] marker) {
		if (data == null || marker == null || data.length < marker.length) {
			return false;
		}
		
		boolean result = true;
		int offset = data.length - marker.length;
		for (int i = 0; i < marker.length; i++) {
			if (data[offset + i] != marker[i]) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public static List<byte[]> split(final byte[] data, final byte[] marker) {
		final List<byte[]> result = new ArrayList<byte[]>();

		int index = -1;
		int offset = 0;
		do {
			index = indexOf(data, marker, offset);
			
			if (index >= 0) {
				final byte[] part = new byte[index - offset];
				System.arraycopy(data, offset, part, 0, index - offset);
				result.add(part);
				
				// Set new offset
				offset = index + marker.length;
			}
		} while(index > 0 && offset < (data.length - marker.length));
		
		// Append final part
		if (data.length - offset > 0) {
			final byte[] part = new byte[data.length - offset];
			System.arraycopy(data, offset, part, 0, data.length - offset);
			result.add(part);
		}
		
		return result;
	}

	public static int indexOf(final byte[] data, final byte[] marker) {
		return indexOf(data, marker, 0);
	}
	
	public static int indexOf(final byte[] data, final byte[] marker, final int offset) {
		if (data == null || marker == null || data.length < marker.length || offset < 0 || offset > data.length) {
			return -1;
		}

		int result = -1;
		for (int i = offset; i < data.length; i++) {
			if (i > (data.length - marker.length)) {
				break;
			}
			
			if (data[i] == marker[0]) {
				boolean matching = true;
				
				if (marker.length >= 1) {
					for (int j = 1; j < marker.length; j++) {
						if (data[i + j] != marker[j]) {
							matching = false;
							break;
						}
					}
				}
				
				if (matching) {
					result = i;
					return result;
				}
			}
		}
		return result;
	}

	public static byte[] strip(final byte[] data, final byte[] marker) {
		if (data == null || marker == null) {
			return null;
		}
		
		if (endsWith(data, marker)) {
			final byte[] result = new byte[data.length - marker.length];
			System.arraycopy(data, 0, result, 0, data.length - marker.length);
			return result;
		}
		return data;
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

	public static String toString(final int[] data) {
		if (data == null || data.length <= 0) {
			return "";
		}
		return toString(data, 0, data.length - 1);
	}

	public static String toString(final int[] data, final int start) {
		return toString(data, start, data.length);
	}

	public static String toString(final int[] data, final int start, final int end) {
		int[] result = subset(data, start, end);
		if (result == null || result.length <= 0) {
			return "";
		}
		return new String(toByteArray(result));
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
