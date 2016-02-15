package at.creadoo.homematic.socket;

import java.net.InetSocketAddress;

public class Test {

	public static void main(String[] args) {
		final SocketLink hardware = new SocketLink(new InetSocketAddress("192.168.0.1", 1000));
		
		try {
			hardware.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
