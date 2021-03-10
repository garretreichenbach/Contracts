/**
 * Packet [Client -> Server]
 */

package thederpgamer.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import thederpgamer.contracts.network.server.ReturnFederationsPacket;
import org.schema.game.common.data.player.PlayerState;

public class GetFederationsPacket extends Packet {

    public GetFederationsPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) {

    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) {

    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        ReturnFederationsPacket returnFederationsPacket = new ReturnFederationsPacket();
        PacketUtil.sendPacket(playerState, returnFederationsPacket);
    }
}
