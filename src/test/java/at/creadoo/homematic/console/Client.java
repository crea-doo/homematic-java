package at.creadoo.homematic.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.creadoo.homematic.HomeMaticStatus;
import at.creadoo.homematic.ILink;
import at.creadoo.homematic.ILinkListener;
import at.creadoo.homematic.link.HMCFGLANLink;
import at.creadoo.homematic.link.HMCFGUSBLink;
import at.creadoo.homematic.packet.HomeMaticPacket;
import at.creadoo.homematic.packet.HomeMaticPacketSet;

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
					if (Command.HELP.getValues().contains(line.trim().toLowerCase())) {
						printUsage();
					} else if (Command.EXIT.getValues().contains(line.trim().toLowerCase())) {
						if (currentLink != null && currentLink.isConnected()) {
							currentLink.close();
						}
						currentLink = null;
						break;
					} else if (Command.CLOSE.getValues().contains(line.trim().toLowerCase())) {
						if (currentLink != null && currentLink.isConnected()) {
							currentLink.close();
						}
					} else if (Command.SEND.getValues().contains(line.trim().toLowerCase())) {
						if (currentLink != null && currentLink.isConnected()) {
							currentLink.send(new HomeMaticPacketSet(0x26, 0x31e00f, 0x35366f, HomeMaticStatus.ON));
						} else {
							System.out.println("Link not connected");
						}
					} else if (Command.CONNECT_HID.getValues().contains(line.trim().toLowerCase())) {
						currentLink = setupHIDLink();
						currentLink.start();
						if (currentLink.isConnected()) {
							System.out.println("Link not connected");
						}
					} else if (Command.CONNECT_SOCKET.getValues().contains(line.trim().toLowerCase())) {
						currentLink = setupSocketLink();
						currentLink.start();
						if (currentLink.isConnected()) {
							System.out.println("Link not connected");
						}
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
			return new HMCFGUSBLink(linkListener);
		} catch (Throwable ex) {
			System.out.println("[ERROR] " + ex.getMessage());
			ex.printStackTrace(System.out);
		}
		return null;
    }

    private static ILink setupSocketLink() {
		try {
			final InetSocketAddress socketRemoteIp = new InetSocketAddress("10.0.1.111", 1000);
			return new HMCFGLANLink(socketRemoteIp, linkListener);
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
    	
    	final List<String> commands = new ArrayList<String>();
    	for (final Command command : Command.values()) {
    		commands.addAll(command.getValues());
    	}
    	Collections.sort(commands);
    	
    	for (String commandValue : commands) {
			System.out.println("   " + commandValue.trim());
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
    	SEND("send"),
    	CONNECT_HID("connect-hid", "hid"),
    	CONNECT_SOCKET("connect-socket", "socket");
    	
    	private final List<String> values = new ArrayList<String>();

    	private Command(final String ... values) {
    		for (String value : values) {
    			this.values.add(value.toLowerCase().trim());
    		}
    	}

    	public List<String> getValues() {
    		return Collections.unmodifiableList(this.values);
    	}
    }
}
