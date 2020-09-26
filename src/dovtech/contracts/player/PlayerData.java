package dovtech.contracts.player;

import api.entity.StarPlayer;
import api.server.Server;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.util.DataUtil;

import java.io.Serializable;
import java.util.ArrayList;

public class PlayerData implements Serializable {

    private String playerName;
    private ArrayList<PlayerHistory> history;
    private ArrayList<String> contractUIDs;

    public PlayerData(StarPlayer player) {
        this.playerName = player.getName();
        this.history = new ArrayList<>();
        this.contractUIDs = new ArrayList<>();
    }

    public ArrayList<Contract> getContracts() {
        if(contractUIDs == null) contractUIDs = new ArrayList<>();
        ArrayList<Contract> contracts = new ArrayList<>();
        for(Contract contract : DataUtil.contracts.values()) {
            if(contractUIDs.contains(contract.getUid())) contracts.add(contract);
        }
        return contracts;
    }

    public void addContract(Contract contract) {
        this.contractUIDs.add(contract.getUid());
    }

    public void removeContract(Contract contract) {
        this.contractUIDs.remove(contract.getUid());
    }

    public ArrayList<PlayerHistory> getHistory() {
        return history;
    }

    public String getPlayerName() {
        return playerName;
    }
}