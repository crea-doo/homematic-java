package at.creadoo.homematic.packets;

import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.HomeMaticStatus;
import at.creadoo.homematic.util.Util;

public class HomeMaticPacketRemote extends HomeMaticPacket {

	public static final int PAYLOAD_LEN = 0;

	/*
	private HomeMaticStatus status;

	private boolean lowBat = false;
	*/

	public HomeMaticPacketRemote() {
		super(PAYLOAD_LEN);
	}

	public HomeMaticPacketRemote(final int messageCounter, final int senderAddress, final int destinationAddress, final HomeMaticStatus status) {
		this(messageCounter, 0, senderAddress, destinationAddress, status);
	}
	
	public HomeMaticPacketRemote(final int messageCounter, final int controlByte, final int senderAddress, final int destinationAddress, final HomeMaticStatus status) {
		super(messageCounter, controlByte, HomeMaticMessageType.EVENT, senderAddress, destinationAddress);
		this.status = status;
		generatePayload();
	}

	public HomeMaticPacketRemote(final byte[] data) {
		super(data);
	}

	public HomeMaticPacketRemote(final byte[] data, final int rssi) {
		super(data, rssi);
	}

	/*
	public final HomeMaticStatus getStatus() {
		return status;
	}

	public final void setStatus(final HomeMaticStatus status) {
		this.status = status;
		generatePayload();
	}

	public final boolean getLowBat() {
		return lowBat;
	}

	public final void setLowBat(final boolean lowBat) {
		this.lowBat = lowBat;
		generatePayload();
	}
	*/

	@Override
	protected final void parsePayload() {
		/*
		if (this.payload[2] == 0x00) {
			status = HomeMaticStatus.OFF;
		} else if (this.payload[2] == 0x01) {
			//TODO What does 0x01 mean?
			//status = HomeMaticStatus.ON;
		} else if (this.payload[2] == 0xC8) {
			status = HomeMaticStatus.ON;
		}

		lowBat = Util.isBitSet(this.payload[0], 7);
		*/
	}

	@Override
	protected final void generatePayload() {
		/*
		final int[] payload = new int[PAYLOAD_LEN];
		
		if (status == HomeMaticStatus.OFF) {
			payload[2] = 0x00;
		} else if (status == HomeMaticStatus.ON) {
			payload[2] = 0xC8;
		}
		
		if (lowBat) {
			Util.setBit(payload[0], 7);
		} else {
			Util.unsetBit(payload[0], 7);
		}
		
		setPayload(payload);
		*/
	}

	@Override
	protected final String payloadToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("status=").append(status).append(", ");
		sb.append("lowBat=").append(lowBat);
		return sb.toString();
	}

}
