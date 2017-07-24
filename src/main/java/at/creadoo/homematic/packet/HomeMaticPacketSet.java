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
