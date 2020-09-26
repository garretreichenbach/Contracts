package dovtech.contracts.player;

import api.entity.StarPlayer;
import api.server.Server;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.util.DataUtil;
import java.util.ArrayList;

public class PlayerData {

    private String playerName;
    private ArrayList<PlayerHistory> history;
    private ArrayList<String> contractUIDs;

    public PlayerData(StarPlayer player) {
        this.playerName = player.getName();
        this.history = new ArrayList<>();
        this.contractUIDs = new ArrayList<>();
    }

    public ArrayList<Contract> getContracts() {
        ArrayList<Contract> contracts = new ArrayList<>();
        for(Contract contract : DataUtil.contracts) {
            if(contractUIDs.contains(contract.getUid())) contracts.add(contract);
        }
        return contracts;
    }

    public void setContracts(ArrayList<Contract> contracts) {
        contractUIDs.clear();
        for(Contract contract : contracts) contractUIDs.add(contract.getUid());
    }

    public ArrayList<PlayerHistory> getHistory() {
        return history;
    }

    public String getPlayerName() {
        return playerName;
    }
}