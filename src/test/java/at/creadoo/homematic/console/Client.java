package at.creadoo.homematic.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import at.creadoo.homematic.ILink;
import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.jhid.HidLink;
import at.creadoo.homematic.packets.HomeMaticPacket;
import at.creadoo.homematic.socket.SocketLink;

public class Client {

    private static ILinkListener linkListener;
    private static ILink currentLink;
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		setup();
		
		printUsage();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			printLineHeader();

			for (String line = in.readLine().trim(); line != null; line = in.readLine().trim()) {
				if (line.isEmpty()) {
					printLineHeader();
					continue;
				}
				
				try {
					if (line.equalsIgnoreCase(Command.HELP.getValue())) {
						printUsage();
					} else if (line.equalsIgnoreCase(Command.EXIT.getValue())) {
						break;
					} else if (line.equalsIgnoreCase(Command.CLOSE.getValue())) {
						currentLink.close();
					} else if (line.equalsIgnoreCase(Command.CONNECT_HID.getValue())) {
						currentLink = setupHIDLink();
					} else if (line.equalsIgnoreCase(Command.CONNECT_SOCKET.getValue())) {
						currentLink = setupSocketLink();
					} else {
						System.out.println("Command not found: '" + line + "'");
					}
				} catch (Throwable ex) {
					System.out.println("[ERROR] " + ex.getMessage());
					ex.printStackTrace(System.out);
				}
				
				printLineHeader();
			}

			in.close();

		} catch (Exception ex) {
			//
		}
	}
    
    /**
     * Prepare everything
     */
    private static void setup() {
		linkListener = new ILinkListener() {
			
			@Override
			public void received(final HomeMaticPacket packet) {
				System.out.println("Paket received: " + packet.toString());
			}
			
			@Override
			public void close() {
				System.out.println("Link closed");
			}
		};
    }
    

    private static ILink setupHIDLink() {
		try {
			return new HidLink(linkListener);
		} catch (Throwable ex) {
			System.out.println("[ERROR] " + ex.getMessage());
			ex.printStackTrace(System.out);
		}
		return null;
    }

    private static ILink setupSocketLink() {
		try {
			final InetSocketAddress socketRemoteIp = new InetSocketAddress("192.168.0.17", 1000);
			return new SocketLink(socketRemoteIp, linkListener);
		} catch (Throwable ex) {
			System.out.println("[ERROR] " + ex.getMessage());
			ex.printStackTrace(System.out);
		}
		return null;
    }
    
    /**
     * Print usage function
     */
    private static void printUsage() {
    	System.out.println("Available commands:");
    	
    	for (Command command : Command.values()) {
        	System.out.println("   " + command.getValue());
    	}
    	
    	System.out.println("");
    }
    
    /**
     * Print line header function
     */
    private static void printLineHeader() {
    	System.out.print("> ");
    }
    
    private enum Command {

    	HELP("help"),
    	EXIT("exit"),
    	CLOSE("close"),
    	CONNECT_HID("connect-hid"),
    	CONNECT_SOCKET("connect-socket");
    	
    	private String value;

    	private Command(final String value) {
    		this.value = value;
    	}

    	public String getValue() {
    		return this.value;
    	}
    }
}
