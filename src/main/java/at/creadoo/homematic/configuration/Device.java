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
package at.creadoo.homematic.configuration;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Device implements Serializable {

	private static final transient long serialVersionUID = 1L;

	private final String address;
	
	public Device(final String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof Device) {
			Device other = (Device) obj;
			EqualsBuilder builder = new EqualsBuilder()
				.append(getAddress(), other.getAddress());
			return builder.isEquals();
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return new HashCodeBuilder(1, 31)
			.append(getAddress())
			.toHashCode();
	}

	@Override
	public final String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
