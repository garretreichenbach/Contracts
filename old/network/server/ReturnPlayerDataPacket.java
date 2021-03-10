/**
 * Packet [Server -> Client]
 */

package thederpgamer.contracts.network.server;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import thederpgamer.contracts.Contracts;
import thederpgamer.contracts.contracts.Contract;
import thederpgamer.contracts.faction.FactionOpinion;
import thederpgamer.contracts.player.PlayerData;
import thederpgamer.contracts.player.PlayerHistory;
import thederpgamer.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.PlayerNotFountException;

import java.io.IOException;
import java.util.ArrayList;

public class ReturnPlayerDataPacket extends Packet {

    private PlayerData playerData;
    private String playerName;
    private ArrayList<PlayerHistory> playerHistory;
    private ArrayList<String> playerHistoryEvents = new ArrayList<>();
    private ArrayList<String> playerHistoryDates = new ArrayList<>();
    private ArrayList<String> contractUIDs = new ArrayList<>();
    private int factionID = 0;
    private FactionOpinion[] factionOpinions;
    private ArrayList<String> factionOpinionIDs = new ArrayList<>();
    private ArrayList<String> factionOpinionInts = new ArrayList<>();
    private Contracts.Mode gameState = Contracts.getInstance().getGameState();

    public ReturnPlayerDataPacket() {

    }

    public ReturnPlayerDataPacket(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
       if(gameState.equals(Contracts.Mode.CLIENT)) {
            playerName = packetReadBuffer.readString();
            playerHistoryEvents = packetReadBuffer.readStringList();
            playerHistoryDates = packetReadBuffer.readStringList();
            playerHistory = new ArrayList<>();
            for(int h = 0; h < playerHistoryEvents.size(); h ++) {
                playerHistory.add(new PlayerHistory(playerHistoryEvents.get(h), playerHistoryDates.get(h)));
            }
            contractUIDs = packetReadBuffer.readStringList();
            factionID = packetReadBuffer.readInt();
            factionOpinionIDs = packetReadBuffer.readStringList();
            factionOpinionInts = packetReadBuffer.readStringList();
            factionOpinions = new FactionOpinion[factionOpinionIDs.size()];
            for(int o = 0; o < factionOpinions.length; o ++) {
                factionOpinions[o] = new FactionOpinion(Integer.parseInt(factionOpinionIDs.get(o)), Integer.parseInt(factionOpinionInts.get(o)));
            }
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        if(gameState.equals(Contracts.Mode.SERVER)) {
            packetWriteBuffer.writeString(playerName);
            packetWriteBuffer.writeStringList(playerHistoryEvents);
            packetWriteBuffer.writeStringList(playerHistoryDates);
            packetWriteBuffer.writeStringList(contractUIDs);
            packetWriteBuffer.writeInt(factionID);
            packetWriteBuffer.writeStringList(factionOpinionIDs);
            packetWriteBuffer.writeStringList(factionOpinionInts);
        }
    }

    @Override
    public void processPacketOnClient() {
        playerData = new PlayerData(playerName, playerHistory, contractUIDs, factionID, factionOpinions);
        DataUtils.addPlayerDataToLocal(playerData);
        DataUtils.playerData = playerData;
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        try {
            playerData = DataUtils.getPlayerData(playerName);
            playerHistoryEvents = new ArrayList<>();
            playerHistoryDates = new ArrayList<>();
            for (PlayerHistory history : playerData.getHistory()) {
                playerHistoryEvents.add(history.getEvent());
                playerHistoryDates.add(history.getDate());
            }
            contractUIDs = new ArrayList<>();
            for (Contract contract : DataUtils.getPlayerContracts(playerName)) {
                contractUIDs.add(contract.getUID());
            }
            factionID = playerData.getFactionID();
            factionOpinionIDs = new ArrayList<>();
            factionOpinionInts = new ArrayList<>();
            for (FactionOpinion factionOpinion : playerData.getOpinions()) {
                factionOpinionIDs.add(String.valueOf(factionOpinion.getFaction().getID()));
                factionOpinionInts.add(String.valueOf(factionOpinion.getOpinionScore()));
            }
        } catch (PlayerNotFountException e) {
            e.printStackTrace();
        }
    }
}
