package dovtech.contracts.util;

import api.faction.StarFaction;
import api.mod.config.PersistentObjectUtil;
import api.network.packets.PacketUtil;
import dovtech.contracts.Contracts;
import dovtech.contracts.faction.FactionData;
import dovtech.contracts.network.client.GetFactionDataListPacket;
import java.util.ArrayList;

public class FactionUtils {


    private static final Contracts instance = Contracts.getInstance();
    public static ArrayList<FactionData> clientFactionDataList = new ArrayList<>();

    public static FactionData getFactionData(StarFaction faction) {
        if(instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            for(Object object : PersistentObjectUtil.getObjects(instance, FactionData.class)) {
                FactionData factionData = (FactionData) object;
                if(factionData.getFactionID() == faction.getID()) return factionData;
            }
            FactionData newData = new FactionData(faction);
            PersistentObjectUtil.addObject(instance, newData);
            PersistentObjectUtil.save(instance);
            return newData;
        } else {
            GetFactionDataListPacket getFactionDataListPacket = new GetFactionDataListPacket();
            PacketUtil.sendPacketToServer(getFactionDataListPacket);
            for(FactionData factionData : clientFactionDataList) {
                if(factionData.getFactionID() == faction.getID()) return factionData;
            }
            return new FactionData(faction);
        }
    }
}