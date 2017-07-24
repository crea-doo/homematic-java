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
