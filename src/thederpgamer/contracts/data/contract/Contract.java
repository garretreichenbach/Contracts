package thederpgamer.contracts.data.contract;

import api.common.GameCommon;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.PlayerNotFountException;
import thederpgamer.contracts.data.ServerDatabase;
import thederpgamer.contracts.data.contract.target.ContractTarget;
import thederpgamer.contracts.data.player.PlayerData;
import java.io.Serializable;
import java.util.ArrayList;

public class Contract implements Serializable {

    private String name;
    private int contractorID;
    private ContractType contractType;
    private int reward;
    private ContractTarget target;
    private ArrayList<PlayerData> claimants;
    private String uid;
    private int timer;
    private boolean finished;

    public Contract(int contractorID, String name, ContractType contractType, int reward, String uid, ContractTarget target) {
        this.name = name;
        this.contractorID = contractorID;
        this.contractType = contractType;
        this.reward = reward;
        this.target = target;
        this.claimants = new ArrayList<>();
        this.uid = uid;
        this.timer = -1;
        this.finished = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setTarget(ContractTarget target) {
        this.target = target;
    }

    public ArrayList<PlayerData> getClaimants() {
        return claimants;
    }

    public String getName() {
        return name;
    }

    public Faction getContractor() throws PlayerNotFountException {
        if(contractorID != 0) {
            return GameCommon.getGameState().getFactionManager().getFaction(contractorID);
        } else {
            ServerDatabase.removeContract(this);
            return null;
        }
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

    public ContractTarget getTarget() {
        return target;
    }

    public String getUID() {
        return uid;
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
    }
}
