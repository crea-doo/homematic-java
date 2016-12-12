package at.creadoo.homematic.packets;

public class HomeMaticPacketPlain extends HomeMaticPacket {

	public HomeMaticPacketPlain() {
		super(0);
	}

	public HomeMaticPacketPlain(final int messageCounter, final int senderAddress, final int destinationAddress) {
		this(messageCounter, 0, senderAddress, destinationAddress);
	}
	
	public HomeMaticPacketPlain(final int messageCounter, final int controlByte, final int senderAddress, final int destinationAddress) {
		super(messageCounter, controlByte, null, senderAddress, destinationAddress);
		generatePayload();
	}

	public HomeMaticPacketPlain(final byte[] data) {
		super(data);
	}

	public HomeMaticPacketPlain(final byte[] data, final int rssi) {
		super(data, rssi);
	}

	@Override
	protected final void parsePayload() {
		//
	}

	@Override
	protected final void generatePayload() {
		//
	}

	@Override
	protected final String payloadToString() {
		return null;
	}

}
