package dovtech.contracts.network.server;

import api.faction.StarFaction;
import api.mod.StarLoader;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dovtech.contracts.Contracts;
import dovtech.contracts.faction.FactionData;
import dovtech.contracts.gui.faction.diplomacy.FactionDiplomacyModifier;
import dovtech.contracts.util.FactionUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class ReturnFactionDataListPacket extends Packet {

    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    private int factionID = 0;
    private double factionPower = 0;
    private String diplomacyModifierString = "";

    public ReturnFactionDataListPacket() {

    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        if (gameState.equals(Contracts.Mode.CLIENT)) {
            factionID = packetReadBuffer.readInt();
            factionPower = packetReadBuffer.readDouble();
            diplomacyModifierString = packetReadBuffer.readString();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if (gameState.equals(Contracts.Mode.SERVER) || gameState.equals(Contracts.Mode.SINGLEPLAYER)) {
            packetWriteBuffer.writeInt(factionID);
            packetWriteBuffer.writeDouble(factionPower);
            packetWriteBuffer.writeString(diplomacyModifierString);
        }
    }

    @Override
    public void processPacketOnClient() {
        FactionData factionData = new FactionData();
        factionData.setFactionID(factionID);
        factionData.setFactionPower(factionPower);

        Gson gson = new Gson();
        Type mapType = new TypeToken<HashMap<Integer, ArrayList<Integer>>>() {}.getType();
        HashMap<Integer, ArrayList<Integer>> modifiers = gson.fromJson(diplomacyModifierString, mapType);
        for (int factionID : modifiers.keySet()) {
            StarFaction fromFaction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(factionID));
            ArrayList<Integer> modIDs = modifiers.get(factionID);
            for (int modID : modIDs) {
                FactionDiplomacyModifier modifier = FactionDiplomacyModifier.fromID(modID);
                factionData.addModifier(fromFaction, modifier);
            }
        }

        for (FactionData fData : FactionUtils.clientFactionDataList) {
            if (fData.getFactionID() == factionData.getFactionID()) {
                FactionUtils.clientFactionDataList.remove(fData);
                break;
            }
        }
        FactionUtils.clientFactionDataList.add(factionData);
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        ArrayList<StarFaction> factions = new ArrayList<>();
        for (Faction f : StarLoader.getGameState().getFactionManager().getFactionCollection()) {
            factions.add(new StarFaction(f));
        }
        for (StarFaction faction : factions) {
            FactionData data = FactionUtils.getFactionData(faction);
            HashMap<Integer, ArrayList<Integer>> allModifiers = data.getAllModifiers();
            Gson gson = new Gson();
            diplomacyModifierString = gson.toJson(allModifiers);
        }
    }
}
