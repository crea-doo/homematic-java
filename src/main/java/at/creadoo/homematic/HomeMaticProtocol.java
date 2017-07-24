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

public class HomeMaticProtocol {

	// send initially to keep the device awake
	public static boolean isWakeupSet(int control) {
		return (control & 1) == 1;
	}

	// awake - hurry up to send messages
	public static boolean isWakeMeUpSet(int control) {
		return (control & 2) == 2;
	}

	// Broadcast - to all my peers parallel
	public static boolean isCFGSet(int control) {
		return (control & 4) == 4;
	}

	// currently unkown
	public static boolean isParam3Set(int control) {
		return (control & 8) == 8;
	}

	// set if burst is required by device
	public static boolean isBurstSet(int control) {
		return (control & 16) == 16;
	}

	// response is expected
	public static boolean isBIDISet(int control) {
		return (control & 32) == 32;
	}

	// repeated (repeater operation)
	public static boolean isRPTSet(int control) {
		return (control & 64) == 64;
	}

	// set in every message. Meaning?
	public static boolean isRPTENSet(int control) {
		return (control & 128) == 128;
	}

}
