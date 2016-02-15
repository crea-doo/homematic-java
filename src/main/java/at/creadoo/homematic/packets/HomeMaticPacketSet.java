package at.creadoo.homematic.packets;

import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.HomeMaticStatus;

public class HomeMaticPacketSet extends HomeMaticPacket {
	
	public static final int PAYLOAD_LEN = 6;

	private HomeMaticStatus status;

	public HomeMaticPacketSet(final int messageCounter, final int senderAddress, final int destinationAddress, final HomeMaticStatus status) {
		this(messageCounter, 0, senderAddress, destinationAddress, status);
	}
	
	public HomeMaticPacketSet(final int messageCounter, final int controlByte, final int senderAddress, final int destinationAddress, final HomeMaticStatus status) {
		super(messageCounter, controlByte, HomeMaticMessageType.SET, senderAddress, destinationAddress);
		this.status = status;
		generatePayload();
	}

	public HomeMaticPacketSet(final byte[] data) {
		super(data);
	}

	public HomeMaticPacketSet(final byte[] data, final int rssi) {
		super(data, rssi);
	}

	public final HomeMaticStatus getStatus() {
		return status;
	}

	public final void setStatus(final HomeMaticStatus status) {
		this.status = status;
		generatePayload();
	}

	@Override
	protected final void parsePayload() {
		if (this.payload[2] == 0x00) {
			status = HomeMaticStatus.OFF;
		} else if (this.payload[2] == 0xC8) {
			status = HomeMaticStatus.ON;
		}
	}

	@Override
	protected final void generatePayload() {
		final int[] payload = new int[PAYLOAD_LEN];
		
		if (status == HomeMaticStatus.OFF) {
			payload[2] = 0x00;
		} else if (status == HomeMaticStatus.ON) {
			payload[2] = 0xC8;
		}
		
		setPayload(payload);
	}

	@Override
	protected final String payloadToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("status=").append(status);
		return sb.toString();
	}

}
