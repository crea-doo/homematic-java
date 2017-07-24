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
package at.creadoo.homematic.link.test;

import java.net.InetSocketAddress;

import at.creadoo.homematic.link.HMLGWLink;

public class HMLGWLinkTest {

	public static void main(String[] args) {
		final HMLGWLink hardware = new HMLGWLink(new InetSocketAddress("192.168.0.1", 2000), 2001);
		hardware.setCentralAddress("FD666A");
		hardware.setAESEnabled(false);
		hardware.setLANPassword("00112233445566778899AABBCCDDEEFF");
		hardware.setAESRFKey("00112233445566778899AABBCCDDEEFF");
		
		try {
			hardware.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
