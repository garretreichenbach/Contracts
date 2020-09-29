package dovtech.contracts.player;

import api.entity.StarPlayer;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.util.DataUtil;

import java.io.Serializable;
import java.util.ArrayList;

public class PlayerData implements Serializable {

    private String name;
    private ArrayList<PlayerHistory> history;
    private ArrayList<String> contractUIDs;
    private int factionID;

    public PlayerData(StarPlayer player) {
        this.name = player.getName();
        this.history = new ArrayList<>();
        this.contractUIDs = new ArrayList<>();
        this.factionID = player.getPlayerState().getFactionId();
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

    public String getName() {
        return name;
    }

    public int getFactionID() {
        return factionID;
    }
}