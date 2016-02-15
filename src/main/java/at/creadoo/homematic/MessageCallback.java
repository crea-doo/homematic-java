package at.creadoo.homematic;

public interface MessageCallback {

	void received(byte[] packet);
	
	void connectionTerminated();

}