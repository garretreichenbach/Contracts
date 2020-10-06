package dovtech.contracts.player;

import api.entity.StarPlayer;
import api.faction.StarFaction;
import dovtech.contracts.contracts.Contract;
import dovtech.contracts.faction.FactionOpinion;
import dovtech.contracts.util.DataUtils;
import java.io.Serializable;
import java.util.ArrayList;

public class PlayerData implements Serializable {

    private String name;
    private ArrayList<PlayerHistory> history;
    private ArrayList<String> contractUIDs;
    private int factionID;
    private FactionOpinion[] opinions;

    public PlayerData(StarPlayer player) {
        this.name = player.getName();
        this.history = new ArrayList<>();
        this.contractUIDs = new ArrayList<>();
        this.factionID = player.getPlayerState().getFactionId();
        DataUtils.genOpinions(this);
    }

    public PlayerData(String name, ArrayList<PlayerHistory> history, ArrayList<String> contractUIDs, int factionID, FactionOpinion[] opinions) {
        this.name = name;
        this.history = history;
        this.contractUIDs = contractUIDs;
        this.factionID = factionID;
        this.opinions = opinions;
        DataUtils.genOpinions(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContractUIDs(ArrayList<String> contractUIDs) {
        this.contractUIDs = contractUIDs;
    }

    public ArrayList<String> getContractUIDs() {
        return contractUIDs;
    }

    public void addContract(Contract contract) {
        this.contractUIDs.add(contract.getUID());
    }

    public void removeContract(Contract contract) {
        this.contractUIDs.remove(contract.getUID());
    }

    public ArrayList<PlayerHistory> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<PlayerHistory> history) {
        this.history = history;
    }

    public String getName() {
        return name;
    }

    public int getFactionID() {
        return factionID;
    }

    public void setFactionID(int factionID) {
        this.factionID = factionID;
    }

    public FactionOpinion[] getOpinions() {
        return opinions;
    }

    public void setOpinions(FactionOpinion[] opinions) {
        this.opinions = opinions;
    }

    public FactionOpinion getOpinion(StarFaction faction) {
        for(int i = 0; i < getOpinions().length; i ++) {
            if(opinions[i].getFaction().getID() == faction.getID()) return opinions[i];
        }
        return new FactionOpinion(faction.getID(), 0);
    }

    public FactionOpinion getOpinionFromID(int ID) {
        for(FactionOpinion opinion : getOpinions()) {
            if(opinion.getFaction().getID() == ID)  return opinion;
        }
        return null;
    }

    public void modOpinionScore(int factionID, int scoreToAdd) {
        for(FactionOpinion opinion : getOpinions()) {
            if(opinion.getFaction().getID() == factionID) {
                opinion.setOpinionScore(opinion.getOpinionScore() + scoreToAdd);
                return;
            }
        }
    }

    public void setOpinionScore(StarFaction faction, int score) {
        for(FactionOpinion opinion : getOpinions()) {
            if(opinion.getFaction().getID() == faction.getID()) {
                opinion.setOpinionScore(score);
                return;
            }
        }
    }
}