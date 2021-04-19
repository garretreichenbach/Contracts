package thederpgamer.contracts.data.contract;

import api.common.GameCommon;
import org.schema.game.common.data.player.faction.Faction;
import thederpgamer.contracts.data.ServerDatabase;
import thederpgamer.contracts.data.player.PlayerData;
import java.util.ArrayList;

public class Contract {

    private String name;
    private int contractorID;
    private ContractType contractType;
    private int reward;
    private Object target;
    private ArrayList<PlayerData> claimants;
    private int id;
    private int timer;
    private boolean finished;

    public Contract(int contractorID, String name, ContractType contractType, int reward, int id, Object target) {
        this.name = name;
        this.contractorID = contractorID;
        this.contractType = contractType;
        this.reward = reward;
        this.target = target;
        this.claimants = new ArrayList<>();
        this.id = id;
        this.timer = -1;
        this.finished = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public ArrayList<PlayerData> getClaimants() {
        return claimants;
    }

    public String getName() {
        return name;
    }

    public Faction getContractor() {
        if(contractorID != 0) {
            return GameCommon.getGameState().getFactionManager().getFaction(contractorID);
        } else {
            ServerDatabase.removeContract(this);
            return null;
        }
    }

    public String getContractorName() {
        return (contractorID != 0) ? getContractor().getName() : "Non-Aligned" ;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public int getReward() {
        return reward;
    }

    public Object getTarget() {
        return target;
    }

    public int getId() {
        return id;
    }

    public enum ContractType {
        ALL("All"),
        BOUNTY("Bounty"),
        MINING("Mining"),
        PRODUCTION("Production");

        public String displayName;

        ContractType(String displayName) {
            this.displayName = displayName;
        }

        public static ContractType fromString(String s) {
            for(ContractType type : values()) {
                if(s.trim().equalsIgnoreCase(type.displayName.trim())) return type;
            }
            return null;
        }
    }
}
