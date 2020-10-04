/**
 * Packet [Server -> Client]
 */
package dovtech.contracts.network.server;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import dovtech.contracts.Contracts;
import dovtech.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;
import java.util.ArrayList;

public class ReturnFactionAlliesPacket extends Packet {

    private ArrayList<String> allies = new ArrayList<>();
    private int factionID = 0;
    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public ReturnFactionAlliesPacket() {

    }

    public ReturnFactionAlliesPacket(int factionID) {
        this.factionID = factionID;
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.SERVER)) {
            factionID = packetReadBuffer.readInt();
        } else if(gameState.equals(Contracts.Mode.CLIENT)) {
            allies = packetReadBuffer.readStringList();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.SERVER)) {
            packetWriteBuffer.writeStringList(allies);
        }
    }

    @Override
    public void processPacketOnClient() {
        DataUtils.localFactionAllies.clear();
        for(String s : allies) DataUtils.localFactionAllies.add(Integer.parseInt(s));
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        for(int i : DataUtils.getAllies(factionID)) allies.add(String.valueOf(i));
    }
}
