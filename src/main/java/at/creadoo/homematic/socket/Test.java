package at.creadoo.homematic.socket;

import java.net.InetSocketAddress;

import at.creadoo.homematic.util.Util;

public class Test {

	public static void main(String[] args) {
		final SocketLink hardware = new SocketLink(new InetSocketAddress("192.168.0.1", 1000));
		hardware.setAESEnabled(true);
		hardware.setAESLANKey(Util.toByteFromHex("00112233445566778899AABBCCDDEEFF".toLowerCase()));
		hardware.setAESRFKey(Util.toByteFromHex("00112233445566778899AABBCCDDEEFF".toLowerCase()));
		
		try {
			hardware.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
