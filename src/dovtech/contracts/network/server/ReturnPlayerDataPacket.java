/**
 * Packet [Server -> Client]
 */

package dovtech.contracts.network.server;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.faction.FactionOpinion;
import dovtech.contracts.player.PlayerData;
import dovtech.contracts.player.PlayerHistory;
import dovtech.contracts.util.DataUtils;
import org.schema.game.common.data.player.PlayerState;
import java.io.IOException;
import java.util.ArrayList;

public class ReturnPlayerDataPacket extends Packet {

    private PlayerData playerData;
    private String playerName;
    private ArrayList<PlayerHistory> playerHistory;
    private ArrayList<String> playerHistoryEvents;
    private ArrayList<String> playerHistoryDates;
    private ArrayList<String> contractUIDs;
    private int factionID;
    private FactionOpinion[] factionOpinions;
    private ArrayList<String> factionOpinionIDs;
    private ArrayList<String> factionOpinionInts;

    public ReturnPlayerDataPacket() {

    }

    public ReturnPlayerDataPacket(PlayerData playerData) {
        this.playerData = playerData;
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
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

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        packetWriteBuffer.writeString(playerName);
        packetWriteBuffer.writeStringList(playerHistoryEvents);
        packetWriteBuffer.writeStringList(playerHistoryDates);
        packetWriteBuffer.writeStringList(contractUIDs);
        packetWriteBuffer.writeInt(factionID);
        packetWriteBuffer.writeStringList(factionOpinionIDs);
        packetWriteBuffer.writeStringList(factionOpinionInts);
    }

    @Override
    public void processPacketOnClient() {
        playerData = new PlayerData();
        playerData.setName(playerName);
        playerData.setHistory(playerHistory);
        playerData.setContractUIDs(contractUIDs);
        playerData.setFactionID(factionID);
        playerData.setOpinions(factionOpinions);
        DataUtils.playerData = playerData;
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {
        playerData = DataUtils.getPlayerData(playerState.getName());
        playerName = playerData.getName();
        playerHistoryEvents = new ArrayList<>();
        playerHistoryDates = new ArrayList<>();
        for(PlayerHistory history : playerData.getHistory()) {
            playerHistoryEvents.add(history.getEvent());
            playerHistoryDates.add(history.getDate());
        }
        contractUIDs = new ArrayList<>();
        for(Contract contract : DataUtils.getPlayerContracts(playerName)) {
            contractUIDs.add(contract.getUID());
        }
        factionID = playerData.getFactionID();
        factionOpinionIDs = new ArrayList<>();
        factionOpinionInts = new ArrayList<>();
        for(FactionOpinion factionOpinion : playerData.getOpinions()) {
            factionOpinionIDs.add(String.valueOf(factionOpinion.getFaction().getID()));
            factionOpinionInts.add(String.valueOf(factionOpinion.getOpinionScore()));
        }
    }
}
