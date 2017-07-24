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
package at.creadoo.homematic.packet;

import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.HomeMaticStatus;
import at.creadoo.homematic.util.Util;

public class HomeMaticPacketEvent extends HomeMaticPacket {

	public static final int PAYLOAD_LEN = 3;

	private HomeMaticStatus status;

	private boolean lowBat;

	public HomeMaticPacketEvent() {
		super(PAYLOAD_LEN);
	}

	public HomeMaticPacketEvent(final int messageCounter, final int senderAddress, final int destinationAddress, final HomeMaticStatus status) {
		this(messageCounter, 0, senderAddress, destinationAddress, status);
	}
	
	public HomeMaticPacketEvent(final int messageCounter, final int controlByte, final int senderAddress, final int destinationAddress, final HomeMaticStatus status) {
		super(messageCounter, controlByte, HomeMaticMessageType.EVENT, senderAddress, destinationAddress);
		this.status = status;
		generatePayload();
	}

	public HomeMaticPacketEvent(final byte[] data) {
		super(data);
	}

	public HomeMaticPacketEvent(final byte[] data, final int rssi) {
		super(data, rssi);
	}

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

	@Override
	protected final void parsePayload() {
		if (payload[2] == 0x00) {
			status = HomeMaticStatus.OFF;
		} else if (payload[2] == 0x01) {
			//TODO What does 0x01 mean?
			//status = HomeMaticStatus.ON;
		} else if (payload[2] == 0xC8) {
			status = HomeMaticStatus.ON;
		}

		lowBat = Util.isBitSet(payload[0], 7);
	}

	@Override
	protected final void generatePayload() {
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
	}

	@Override
	protected final String payloadToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("status=").append(status).append(", ");
		sb.append("lowBat=").append(lowBat);
		return sb.toString();
	}

}
