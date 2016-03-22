package at.creadoo.homematic.packets;

import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.util.Util;

/**
 * Class for the storage of an incoming packet from the HomeMaticPacket radio
 * signal sender received by the HomeMaticPacket listener.
 */
public class HomeMaticPacket {
	
	public static final int MIN_PACKET_LEN = 9;
	
	public static final int MIN_PAYLOAD_LEN = 0;
	
	protected long timestamp = 0L;

	// Byte 0
	// Length of packet in bytes not including the length byte.
	protected int packetLength = MIN_PACKET_LEN;
	
	// Byte 1
	// Packets are consecutively numbered from 0x00 to 0xFF.
	// One device has one separate message counter for each device it is paired with.
	protected int messageCounter = 0x0;

	// Byte 2
	protected int controlByte = 0x0;

	// Byte 3
	protected HomeMaticMessageType messageType;

	// Byte 4-6
	protected int senderAddress = 0x0;

	// Byte 7-9
	// For broadcast packets the destination address has to be 0x000000.
	protected int destinationAddress = 0x0;

	// Byte 10+
	protected int[] payload = null;

	/**
	 * This is the unmodified raw data from the recorded frame
	 */
	protected byte[] data = new byte[MIN_PACKET_LEN];
	
	protected int rssi;

	private HomeMaticPacket() {
		this.timestamp = System.currentTimeMillis();
	}
	
	public HomeMaticPacket(final int payloadLen) {
		this();
		if (payloadLen >= MIN_PAYLOAD_LEN) {
			this.payload = new int[payloadLen];
			this.packetLength = MIN_PACKET_LEN + payloadLen;
			this.data = generateRawData();
		}
	}
	
	public HomeMaticPacket(final byte[] data) {
		this();
		parseRawData(data);
	}
	
	public HomeMaticPacket(final byte[] data, final int rssi) {
		this(data);
		this.rssi = Util.convertRSSI(rssi);
	}
	
	public HomeMaticPacket(final int messageCounter, final int controlByte, final int messageType, final int senderAddress, final int destinationAddress) {
		this(messageCounter, controlByte, HomeMaticMessageType.getById(messageType), senderAddress, destinationAddress);
	}
	
	public HomeMaticPacket(final int messageCounter, final int controlByte, final HomeMaticMessageType messageType, final int senderAddress, final int destinationAddress) {
		this(messageCounter, controlByte, messageType, senderAddress, destinationAddress, MIN_PAYLOAD_LEN);
	}
	
	public HomeMaticPacket(final int messageCounter, final int controlByte, final HomeMaticMessageType messageType, final int senderAddress, final int destinationAddress, final int payloadLen) {
		this(payloadLen);
		this.messageCounter = messageCounter;
		this.controlByte = controlByte;
		this.messageType = messageType;
		this.senderAddress = senderAddress;
		this.destinationAddress = destinationAddress;
		this.data = generateRawData();
	}
	
	public HomeMaticPacket(final int messageCounter, final int controlByte, final int messageType, final int senderAddress, final int destinationAddress, final int[] payload) {
		this(messageCounter, controlByte, HomeMaticMessageType.getById(messageType), senderAddress, destinationAddress, payload);
	}
	
	public HomeMaticPacket(final int messageCounter, final int controlByte, final int messageType, final int senderAddress, final int destinationAddress, final byte[] payload) {
		this(messageCounter, controlByte, HomeMaticMessageType.getById(messageType), senderAddress, destinationAddress, Util.toIntArray(payload));
	}
	
	public HomeMaticPacket(final int messageCounter, final int controlByte, final HomeMaticMessageType messageType, final int senderAddress, final int destinationAddress, final int[] payload) {
		this(payload.length);
		this.messageCounter = messageCounter;
		this.controlByte = controlByte;
		this.messageType = messageType;
		this.senderAddress = senderAddress;
		this.destinationAddress = destinationAddress;
		this.payload = payload;
		this.packetLength = MIN_PACKET_LEN + payload.length;
		this.data = generateRawData();
	}

	public int getControlByte() {
		return controlByte;
	}

	public final byte[] getData() {
		return this.data;
	}

	public int getDestinationAddress() {
		return destinationAddress;
	}

	public int getMessageCounter() {
		return messageCounter;
	}

	public final void setMessageCounter(final int messageCounter) {
		if (messageCounter < 0) {
			this.messageCounter = 0;
		} else if (messageCounter > 255) {
			this.messageCounter = messageCounter % 255;
		} else {
			this.messageCounter = messageCounter;
		}
		this.data = generateRawData();
	}

	public HomeMaticMessageType getMessageType() {
		return messageType;
	}

	public final void setMessageType(final HomeMaticMessageType messageType) {
		this.messageType = messageType;
		this.data = generateRawData();
	}

	public int getPacketLength() {
		return packetLength;
	}

	public int[] getPayload() {
		return payload;
	}

	public int getRssi() {
		return rssi;
	}

	public final int getSenderAddress() {
		return senderAddress;
	}

	public final void setSenderAddress(final int senderAddress) {
		this.senderAddress = senderAddress;
		this.data = generateRawData();
	}

	public final long getTimestamp() {
		return timestamp;
	}

	public final void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
		this.data = generateRawData();
	}
	
	public final Long getHardwareId() {
		//TODO: Return real hardware id
		return null;
	}
	
	final void setPayload(final int[] payload) {
		if (payload != null) {
			this.payload = payload;
			this.packetLength = MIN_PACKET_LEN + payload.length;
		} else {
			this.payload = new int[MIN_PAYLOAD_LEN];
			this.packetLength = MIN_PACKET_LEN;
		}
		this.data = generateRawData();
	}

	/**
	 * This method is called when a packet's contents is converted to a string
	 * representation. When it comes to the processing of the payload portion
	 * this method should be overridden when implementing a class that handles
	 * packets of one type in a more specific way.
	 */
	protected String payloadToString() {
		return null;
	}
	
	/**
	 * This method is called when a packet is created out of an byte array and
	 * it comes to process the payload portion of the packet. This method should
	 * be overridden when implementing a class that handles packets of one type
	 * in a more specific way.
	 */
	protected void parsePayload() {
		return;
	}

	/**
	 * This method is called when a packet is built out of its stored values and
	 * is converted to a byte array. This method should be overridden when
	 * implementing a class that handles packets of one type in a more specific
	 * way.
	 */
	protected void generatePayload() {
		return;
	}

	/**
	 * This method is called when a packet's contents is converted to a string
	 * representation.
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("HomeMaticPacket [");
		sb.append("rssi=").append(rssi).append("dBm, ");
		sb.append("length=").append(packetLength).append(", ");
		sb.append("type=").append(messageType != null ? messageType.name() : ("0x" + Integer.toHexString(Util.toInt(this.data[3]))) + " (Unkown)").append(", ");
		sb.append("sender=0x").append(Integer.toHexString(senderAddress)).append(", ");
		sb.append("destination=0x").append(Integer.toHexString(destinationAddress)).append(", ");
		sb.append("packet_number=0x").append(Integer.toHexString(messageCounter)).append(", ");
		sb.append("payload_length=").append(payload != null ? payload.length : 0).append(", ");;
		final String sPayload = payloadToString();
		if (sPayload == null || sPayload.isEmpty()) {
			sb.append("payload= [ ");
			for (int i = 0; i < payload.length; i++) {
				if (i != 0)
					sb.append(", ");
				sb.append(Integer.toHexString(payload[i]));
			}
			sb.append(" ]");
		} else {
			sb.append(sPayload);
		}
		sb.append(" ]");
		return sb.toString();
	}

	private void parseRawData(final byte[] data) {
		packetLength = Util.toInt(data[0]); // first value holds the packet size without the size byte
		
		this.data = new byte[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
		
		final int[] tempData = Util.toIntArray(data);
		
		messageCounter = tempData[1];
		controlByte = tempData[2];
		messageType = HomeMaticMessageType.getById(tempData[3]);
		senderAddress = (tempData[4] << 16) + (tempData[5] << 8) + tempData[6];
		destinationAddress = (tempData[7] << 16) + (tempData[8] << 8) + tempData[9];
		
		int payload_size = packetLength - MIN_PACKET_LEN;
		payload = new int[payload_size];
		for (int i = 0; i < payload_size; i++) {
			payload[i] = tempData[10 + i];
		}
		parsePayload();
	}

	private byte[] generateRawData() {
		final int[] tempData = new int[packetLength + 1];
		
		tempData[0] = packetLength;
		tempData[1] = messageCounter;
		tempData[2] = controlByte;
		tempData[3] = (messageType != null ? messageType.getId() : 0);
		tempData[4] = senderAddress >> 16;
		tempData[5] = senderAddress >> 8;
		tempData[6] = senderAddress;
		tempData[7] = destinationAddress >> 16;
		tempData[8] = destinationAddress >> 8;
		tempData[9] = destinationAddress;
		
		for (int i = 0; i < payload.length; i++) {
			tempData[10 + i] = payload[i];
		}
		
		return Util.toByteArray(tempData);
	}

}
