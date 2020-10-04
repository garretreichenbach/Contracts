/**
 * Packet [Client -> Server]
 */
package dovtech.contracts.network.client;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import dovtech.contracts.Contracts;
import dovtech.contracts.network.server.ReturnPlayerDataPacket;
import dovtech.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.PlayerNotFountException;
import java.io.IOException;

public class GetPlayerDataPacket extends Packet {

    private String playerName;
    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public GetPlayerDataPacket() {

    }

    public GetPlayerDataPacket(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.SERVER)) {
            playerName = packetReadBuffer.readString();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.CLIENT)) {
            packetWriteBuffer.writeString(playerName);
        }
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        try {
            if(playerName == null) playerName = playerState.getName();
            ReturnPlayerDataPacket returnPlayerDataPacket = new ReturnPlayerDataPacket(DataUtils.getPlayerData(playerName));
            PacketUtil.sendPacket(playerState, returnPlayerDataPacket);
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
        }
    }
}
