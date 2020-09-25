package dovtech.contracts.contracts;

import api.common.GameServer;
import api.entity.StarPlayer;
import api.faction.StarFaction;
import api.server.Server;
import dovtech.contracts.contracts.target.ContractTarget;
import dovtech.contracts.util.DataUtil;
import java.util.ArrayList;

public class Contract {

    private String name;
    private int contractorID;
    private ContractType contractType;
    private int reward;
    private ContractTarget[] target;
    private ArrayList<String> claimantNames;
    private String uid;

    public Contract(StarFaction contractor, String name, ContractType contractType, int reward, String uid, ContractTarget... target) {
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

    public void setClaimants(ArrayList<StarPlayer> claimants) {
        for(StarPlayer claimant : claimants) {
            claimantNames.add(claimant.getName());
        }
    }

    public String getName() {
        return name;
    }

    public StarFaction getContractor() {
        if(GameServer.getServerState().getFactionManager().getFaction(contractorID) != null) {
            return new StarFaction(GameServer.getServerState().getFactionManager().getFaction(contractorID));
        } else {
            DataUtil.removeContract(this);
            return null;
        }
    }

    public ContractType getContractType() {
        return contractType;
    }

    public int getReward() {
        return reward;
    }

    public ContractTarget[] getTarget() {
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
