package at.creadoo.homematic.packets;

import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.util.Util;

public class HomeMaticPacketRemote extends HomeMaticPacket {

	public static final int PAYLOAD_LEN = 2;

	private int channel;

	private boolean longPress;

	private int counter;

	public HomeMaticPacketRemote() {
		super(PAYLOAD_LEN);
	}

	public HomeMaticPacketRemote(final int messageCounter, final int senderAddress, final int destinationAddress) {
		this(messageCounter, 0, senderAddress, destinationAddress);
	}
	
	public HomeMaticPacketRemote(final int messageCounter, final int controlByte, final int senderAddress, final int destinationAddress) {
		super(messageCounter, controlByte, HomeMaticMessageType.REMOTE, senderAddress, destinationAddress);
		generatePayload();
	}

	public HomeMaticPacketRemote(final byte[] data) {
		super(data);
	}

	public HomeMaticPacketRemote(final byte[] data, final int rssi) {
		super(data, rssi);
	}

	public final int getChannel() {
		return channel;
	}

	public final void setChannel(final int channel) {
		this.channel = channel & 0x0000000f;
		generatePayload();
	}

	public final boolean getLongPress() {
		return longPress;
	}

	public final void setLongPress(final boolean longPress) {
		this.longPress = longPress;
		generatePayload();
	}

	public final int getCounter() {
		return counter;
	}

	public final void setCounter(final int counter) {
		this.counter = counter;
		generatePayload();
	}

	@Override
	protected final void parsePayload() {		
		this.channel = payload[0] & 0x0000000f;
		this.longPress = Util.isBitSet(payload[0], 6);

		this.counter = payload[1];
	}

	@Override
	protected final void generatePayload() {
		final int[] payload = new int[PAYLOAD_LEN];

		payload[0] = channel & 0x0000000f;
		if (longPress) {
			Util.setBit(payload[0], 6);
		} else {
			Util.unsetBit(payload[0], 6);
		}
		
		payload[1] = counter;
		
		setPayload(payload);
	}

	@Override
	protected final String payloadToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("channel=").append(channel).append(", ");
		sb.append("longPress=").append(longPress).append(", ");
		sb.append("counter=").append(counter);
		return sb.toString();
	}
}
