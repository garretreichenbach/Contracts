/**
 * Packet [Server -> Client]
 */
package thederpgamer.contracts.network.server;

import api.entity.StarPlayer;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;

public class ReturnClientSectorStationFactionPacket extends Packet {

    private int stationFactionID = 0;
    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public ReturnClientSectorStationFactionPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.CLIENT)) {
            stationFactionID = packetReadBuffer.readInt();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.SERVER)) {
            packetWriteBuffer.writeInt(stationFactionID);
        }
    }

    @Override
    public void processPacketOnClient() {
        DataUtils.clientSectorStationFaction = stationFactionID;
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        stationFactionID = DataUtils.getSectorStationFactionID(new StarPlayer(playerState));
    }
}
