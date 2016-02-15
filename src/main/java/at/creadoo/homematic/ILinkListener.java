package at.creadoo.homematic;

import at.creadoo.homematic.packets.HomeMaticPacket;

public interface ILinkListener {

    void received(HomeMaticPacket packet);

    void close();

}
