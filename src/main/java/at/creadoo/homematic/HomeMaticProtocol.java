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
