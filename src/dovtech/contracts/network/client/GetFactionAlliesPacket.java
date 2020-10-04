/**
 * Packet [Client -> Server]
 */
package dovtech.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import dovtech.contracts.Contracts;
import dovtech.contracts.network.server.ReturnFactionAlliesPacket;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;

public class GetFactionAlliesPacket extends Packet {

    private int playerFactionID;
    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public GetFactionAlliesPacket() {

    }

    public GetFactionAlliesPacket(int playerFactionID) {
        this.playerFactionID = playerFactionID;
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
        ReturnFactionAlliesPacket returnFactionAlliesPacket = new ReturnFactionAlliesPacket(playerFactionID);
        PacketUtil.sendPacket(playerState, returnFactionAlliesPacket);
    }
}
