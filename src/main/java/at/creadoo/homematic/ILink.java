package at.creadoo.homematic;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import at.creadoo.homematic.packets.HomeMaticPacket;

/**
 * Interface for the different adapters (USB or LAN)
 */
public interface ILink {

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
	List<ILinkListener> getLinkListeners();
	
	/**
	 * Add a new event listener to the list
	 * 
	 * @param linkListener LinkListener instance
	 */
	void addLinkListener(ILinkListener linkListener);
	
	/**
	 * Remove an event listener from the list
	 * 
	 * @param linkListener LinkListener instance
	 */
	void removeLinkListener(ILinkListener linkListener);

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
