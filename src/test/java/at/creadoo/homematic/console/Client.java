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
package at.creadoo.homematic.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.creadoo.homematic.HomeMaticStatus;
import at.creadoo.homematic.IHomeMaticLink;
import at.creadoo.homematic.IHomeMaticLinkListener;
import at.creadoo.homematic.link.HMCFGLANLink;
import at.creadoo.homematic.link.HMCFGUSBLink;
import at.creadoo.homematic.packet.HomeMaticPacket;
import at.creadoo.homematic.packet.HomeMaticPacketSet;

public class Client {

    private static IHomeMaticLinkListener linkListener;
    private static IHomeMaticLink currentLink;
	
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
		linkListener = new IHomeMaticLinkListener() {

			@Override
			public void received(final IHomeMaticLink link, final HomeMaticPacket packet) {
				System.out.println("Paket received: " + packet.toString());
			}

			@Override
			public void linkStarted(final IHomeMaticLink link) {
				System.out.println("Link '" + link.getName() + "' started");
			}

			@Override
			public void linkClosed(final IHomeMaticLink link) {
				System.out.println("Link '" + link.getName() + "' closed");
			}

			@Override
			public void linkTerminated(final IHomeMaticLink link) {
				System.out.println("Link '" + link.getName() + "' terminated");
			}
		};
    }

    private static IHomeMaticLink setupHIDLink() {
		try {
			return new HMCFGUSBLink(linkListener);
		} catch (Throwable ex) {
			System.out.println("[ERROR] " + ex.getMessage());
			ex.printStackTrace(System.out);
		}
		return null;
    }

    private static IHomeMaticLink setupSocketLink() {
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
