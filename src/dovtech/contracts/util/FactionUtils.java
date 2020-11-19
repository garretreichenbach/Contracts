package dovtech.contracts.util;

import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import api.network.packets.PacketUtil;
import api.server.Server;
import dovtech.contracts.Contracts;
import dovtech.contracts.faction.FactionData;
import dovtech.contracts.faction.FactionOpinion;
import dovtech.contracts.faction.Opinion;
import dovtech.contracts.gui.faction.diplomacy.FactionDiplomacyModifier;
import dovtech.contracts.network.client.GetFactionDataListPacket;
import dovtech.contracts.network.server.ReturnFactionDataListPacket;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation;

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
            newData.setFactionPower(100.0);
            PersistentObjectUtil.addObject(instance, newData);
            PersistentObjectUtil.save(instance);
            return newData;
        } else {
            GetFactionDataListPacket getFactionDataListPacket = new GetFactionDataListPacket();
            PacketUtil.sendPacketToServer(getFactionDataListPacket);
            for(FactionData factionData : clientFactionDataList) {
                if(factionData.getFactionID() == faction.getID()) return factionData;
            }
            FactionData newData = new FactionData(faction);
            newData.setFactionPower(100.0);
            return newData;
        }
    }

    public static void updateFactionData(FactionData factionData) {
        if (instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            for (Object object : PersistentObjectUtil.getObjects(instance, FactionData.class)) {
                FactionData oldData = (FactionData) object;
                if (oldData.getFactionID() == factionData.getFactionID()) {
                    PersistentObjectUtil.removeObject(instance, oldData);
                    break;
                }
            }
            PersistentObjectUtil.addObject(instance, factionData);
            PersistentObjectUtil.save(instance);

            //Update faction data for all currently connected clients
            //Todo: In future, clients should only be sent the updated data if and when they actually need it
            for(StarPlayer player : Server.getOnlinePlayers()) {
                ReturnFactionDataListPacket returnFactionDataListPacket = new ReturnFactionDataListPacket();
                PacketUtil.sendPacket(player.getPlayerState(), returnFactionDataListPacket);
            }
        }
    }

    public static ArrayList<FactionDiplomacyModifier> getOpinionModifiers(StarFaction from, StarFaction to) {
        FactionData fromData = getFactionData(from);
        return fromData.getModifiers(to);
    }

    public static int getOpinionScore(StarFaction from, StarFaction to) {
        int opinionScore = 0;
        for(FactionDiplomacyModifier modifier : getOpinionModifiers(from, to)) {
            opinionScore += modifier.getModifier();
        }
        return opinionScore;
    }

    public static FactionOpinion getOpinion(StarFaction from, StarFaction to) {
        return new FactionOpinion(to.getID(), getOpinionScore(from, to));
    }

    public static void setOpinionAdmin(StarFaction from, StarFaction to) {
        if(instance.getGameState().equals(Contracts.Mode.SERVER) || instance.getGameState().equals(Contracts.Mode.SINGLEPLAYER)) {
            FactionDiplomacyModifier modifier = FactionDiplomacyModifier.ADMIN_COMMAND;
            FactionData fromData = null;
            for (Object object : PersistentObjectUtil.getObjects(instance, FactionData.class)) {
                FactionData factionData = (FactionData) object;
                if(factionData.getFactionID() == from.getID()) {
                    fromData = factionData;
                }
            }
            if(fromData != null) {
                fromData.addModifier(to, modifier);
                PersistentObjectUtil.removeObject(instance, fromData);
                PersistentObjectUtil.addObject(instance, fromData);
                PersistentObjectUtil.save(instance);
            }
        }
    }
}