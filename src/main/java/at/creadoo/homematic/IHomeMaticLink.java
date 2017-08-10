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

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import at.creadoo.homematic.packet.HomeMaticPacket;

/**
 * Interface for the different adapters (USB or LAN)
 */
public interface IHomeMaticLink {

	/**
	 * Return the name of the link
	 * 
	 * @return the name of the link
	 */
	String getName();

	/**
	 * Check if the link is connected
	 * 
	 * @return true, if the link is connected
	 */
	boolean isConnected();

	/**
	 * Start the link
	 * 
	 * @return true, if the link is connected
	 */
	boolean start();

	/**
	 * Close down the link
	 */
	void close();
	
	/**
	 * Returns all the listener from the list
	 */
	List<IHomeMaticLinkListener> getLinkListeners();
	
	/**
	 * Add a new event listener to the list
	 * 
	 * @param linkListener LinkListener instance
	 */
	void addLinkListener(IHomeMaticLinkListener linkListener);
	
	/**
	 * Remove an event listener from the list
	 * 
	 * @param linkListener LinkListener instance
	 */
	void removeLinkListener(IHomeMaticLinkListener linkListener);

	/**
	 * Send a packet only with the telegram instance
	 * 
	 * @param packet
	 *            HomematicPacket instance, filled with the necessary information
	 */
	boolean send(HomeMaticPacket packet) throws SocketException, IOException;

	/**
	 * Check if the link supports automatic reconnection
	 * 
	 * @return true, if the link supports automatic reconnection
	 */
	boolean isReconnectSupported();
}
