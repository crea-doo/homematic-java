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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import at.creadoo.homematic.HomeMaticError;
import at.creadoo.homematic.HomeMaticMessageType;
import at.creadoo.homematic.util.Util;

public class HomeMaticPacketInformation extends HomeMaticPacket {

	public static final int PAYLOAD_LEN = 4;

	private int status;

	private HomeMaticError error;

	private boolean lowBat;

	public HomeMaticPacketInformation(final int messageCounter, final int senderAddress, final int destinationAddress, final int status, final HomeMaticError error) {
		this(messageCounter, 0, senderAddress, destinationAddress, status, error);
	}
	
	public HomeMaticPacketInformation(final int messageCounter, final int controlByte, final int senderAddress, final int destinationAddress, final int status, final HomeMaticError error) {
		super(messageCounter, controlByte, HomeMaticMessageType.INFORMATION, senderAddress, destinationAddress, status);
		this.status = status;
		this.error = error;
		generatePayload();
	}

	public HomeMaticPacketInformation(final byte[] data) {
		super(data);
	}

	public HomeMaticPacketInformation(final byte[] data, final int rssi) {
		super(data, rssi);
	}

	public final int getStatus() {
		return status;
	}

	public final void setStatus(final int status) {
		this.status = status;
		generatePayload();
	}

	public final HomeMaticError getError() {
		return error;
	}

	public final void setError(final HomeMaticError error) {
		this.error = error;
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
		status = this.payload[2];

		final int iError = Util.bitsToInt(this.payload[3], 1, 3);
		if (iError == 0) {
			error = HomeMaticError.NO_ERROR;
		} else if(iError == 7) {
			error = HomeMaticError.SABOTAGE;
		} else {
			error = HomeMaticError.ERROR;
		}

		lowBat = Util.isBitSet(this.payload[3], 7);
	}

	@Override
	protected final void generatePayload() {
		final int[] payload = new int[PAYLOAD_LEN];
		
		//TODO: Set Error
		/*
		if (status == Status.OFF) {
			payload[2] = 0x00;
		} else if (status == Status.ON) {
			payload[2] = 0xC8;
		}
		*/
		
		if (lowBat) {
			Util.setBit(payload[3], 7);
		} else {
			Util.unsetBit(payload[3], 7);
		}
		
		setPayload(payload);
	}

	@Override
	protected final String payloadToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("status=").append(status).append(", ");
		sb.append("error=").append(error).append(", ");
		sb.append("lowBat=").append(lowBat);
		return sb.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof HomeMaticPacket) {
			final HomeMaticPacketInformation other = (HomeMaticPacketInformation) obj;
			EqualsBuilder builder = new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(getStatus(), other.getStatus())
				.append(getError(), other.getError())
				.append(getLowBat(), other.getLowBat());
			return builder.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(1, 31)
			.appendSuper(super.hashCode())
			.append(getStatus())
			.append(getError())
			.append(getLowBat())
			.toHashCode();
	}

}
