package dovtech.contracts.network.server;

import api.faction.StarFaction;
import api.mod.StarLoader;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import com.google.gson.Gson;
import dovtech.contracts.Contracts;
import dovtech.contracts.faction.FactionData;
import dovtech.contracts.util.FactionUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ReturnFactionDataListPacket extends Packet {

    private FactionData factionData;
    private int factionID = 0;
    private ArrayList<String> diplomacyModifierStringList = new ArrayList<>();
    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public ReturnFactionDataListPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.CLIENT)) {
            factionID = packetReadBuffer.readInt();
            diplomacyModifierStringList = packetReadBuffer.readStringList();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.SERVER)) {
            packetWriteBuffer.writeInt(factionID);
            packetWriteBuffer.writeStringList(diplomacyModifierStringList);
        }
    }

    @Override
    public void processPacketOnClient() {
        factionData = new FactionData();

        for(FactionData fData : FactionUtils.clientFactionDataList) {
            if(fData.getFactionID() == factionData.getFactionID()) {
                FactionUtils.clientFactionDataList.remove(fData);
                FactionUtils.clientFactionDataList.add(factionData);
                return;
            }
        }
        FactionUtils.clientFactionDataList.add(factionData);
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        ArrayList<StarFaction> factions = new ArrayList<>();
        for(Faction f : StarLoader.getGameState().getFactionManager().getFactionCollection()) {
            factions.add(new StarFaction(f));
        }
        for(StarFaction faction : factions) {
            FactionData data = FactionUtils.getFactionData(faction);
            HashMap<Integer, ArrayList<Integer>> allModifiers = data.getAllModifiers();
            Gson gson = new Gson();
            diplomacyModifierStringList.add(gson.toJson(allModifiers));
        }
    }
}
