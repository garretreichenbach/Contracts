package dovtech.contracts.faction;

import api.common.GameServer;
import api.faction.StarFaction;
import java.io.Serializable;

public class FactionOpinion implements Serializable {

    private int factionID;
    private int opinionScore;

    public FactionOpinion(int factionID, int opinionScore) {
        this.factionID = factionID;
        this.opinionScore = opinionScore;
    }


    public StarFaction getFaction() {
        if(factionID != 0) {
            return new StarFaction(GameServer.getServerState().getFactionManager().getFaction(factionID));
        } else {
            return null;
        }
    }

    public Opinion getOpinion() {
        return Opinion.getFromScore(opinionScore);
    }

    public int getOpinionScore() {
        return opinionScore;
    }

    public void setOpinionScore(int opinionScore) {
        this.opinionScore = opinionScore;
    }
}
