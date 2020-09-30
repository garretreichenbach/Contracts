package dovtech.contracts.contracts;

import api.common.GameServer;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.server.Server;
import dovtech.contracts.contracts.target.ContractTarget;
import dovtech.contracts.util.DataUtil;

import java.io.Serializable;
import java.util.ArrayList;

public class Contract implements Serializable {

    private String name;
    private int contractorID;
    private ContractType contractType;
    private int reward;
    private ContractTarget target;
    private ArrayList<String> claimantNames;
    private String uid;

    public Contract(StarFaction contractor, String name, ContractType contractType, int reward, String uid, ContractTarget target) {
        this.name = name;
        this.contractorID = contractor.getID();
        this.contractType = contractType;
        this.reward = reward;
        this.target = target;
        this.claimantNames = new ArrayList<>();
        this.uid = uid;
    }

    public ArrayList<StarPlayer> getClaimants() {
        ArrayList<StarPlayer> claimantsList = new ArrayList<>();
        for(String s : claimantNames) {
            if(Server.getPlayer(s) != null) {
                claimantsList.add(new StarPlayer(Server.getPlayer(s)));
            }
        }
        return claimantsList;
    }

    public void removeClaimant(StarPlayer claimant) {
        this.claimantNames.remove(claimant.getName());
    }

    public void addClaimant(StarPlayer claimant) {
        this.claimantNames.add(claimant.getName());
    }

    public void setClaimants(ArrayList<StarPlayer> claimants) {
        this.claimantNames = new ArrayList<>();
        for(StarPlayer player : claimants) this.claimantNames.add(player.getName());
    }

    public String getName() {
        return name;
    }

    public StarFaction getContractor() {
        if(contractorID != 0) {
            return new StarFaction(GameServer.getServerState().getFactionManager().getFaction(contractorID));
        } else {
            DataUtil.removeContract(this, true);
            return null;
        }
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

    public String getUid() {
        return uid;
    }

    public enum ContractType {
        ALL("All"),
        BOUNTY("Bounty"),
        CARGO_ESCORT("Cargo Escort"),
        MINING("Mining"),
        PRODUCTION("Production");

        public String displayName;

        ContractType(String displayName) {
            this.displayName = displayName;
        }
    }
}
