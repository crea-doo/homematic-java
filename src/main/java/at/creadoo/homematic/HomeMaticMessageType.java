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
package at.creadoo.homematic;

public enum HomeMaticMessageType {
	
	 DEVICE_INFO(0x00),
	 CONFIGURATION(0x01),
	 ACKNOWLEDGE(0x02),
	 AES(0x03),
	 AES_KEY(0x04),
	 INFORMATION(0x10),
	 SET(0x11),
	 WAKE_UP(0x12),
	 SWITCH(0x3e), // ??
	 TIMESTAMP(0x3F),
	 REMOTE(0x40),
	 EVENT(0x41),
	 PUSHBUTTON(0x43),
	 SINGLEBUTTON(0x44),
	 POWERMETER(0x51),
	 WATER_SENSOR(0x53),
	 CLIMATE_EVENT(0x58),
	 KFM100(0x60),
	 WEATHER_EVENT(0x70),
	 THREESTATESENSOR(0x80),
	 MOTIONDETECTOR(0x81),
	 KEYMATIC(0xc0),
	 WINMATIC(0xc1),
	 TIPTRONIC(0xc3),
	 SMOKEDETECTOR(0xcd);
	 
	 private Integer id;
	 
	 private HomeMaticMessageType(final Integer id) {
		 this.id = id;
	 }
	 
	 public int getId() {
		 return id;
	 }
	 
	 public static HomeMaticMessageType getById(final Integer id) {
		 for (HomeMaticMessageType t: HomeMaticMessageType.values()) {
			 if (id.equals(t.getId())) {
				 return t;
			 }
		 }
		 return null;
	 }
	 
}
