/**
 * Packet [Client -> Server]
 */
package thederpgamer.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.network.server.ReturnAllContractsPacket;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;

public class GetAllContractsPacket extends Packet {

    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public GetAllContractsPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {

    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {

    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        ReturnAllContractsPacket returnAllContractsPacket = new ReturnAllContractsPacket();
        PacketUtil.sendPacket(playerState, returnAllContractsPacket);
    }
}
