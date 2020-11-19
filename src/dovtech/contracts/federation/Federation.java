package dovtech.contracts.federation;

import api.faction.StarFaction;
import api.mod.StarLoader;
import java.io.Serializable;
import java.util.ArrayList;

public class Federation implements Serializable {

    private int federationID;
    private String name;
    private int govTypeID;
    private ArrayList<Integer> memberFactionIDs;
    private String description;
    private int power;

    public Federation(int federationID) {
        this(federationID, "No Name", FederationGovernmentType.values()[0], "No Description", 0);
    }

    public Federation(int federationID, String name, FederationGovernmentType govType, String description, int power) {
        this.federationID = federationID;
        this.name = name;
        this.govTypeID = govType.ordinal();
        this.memberFactionIDs = new ArrayList<>();
        this.description = description;
        this.power = power;
    }

    public int getFederationID() {
        return federationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FederationGovernmentType getGovernmentType() {
        return FederationGovernmentType.values()[govTypeID];
    }

    public void setGovernmentType(FederationGovernmentType govType) {
        this.govTypeID = govType.ordinal();
    }

    public ArrayList<StarFaction> getMemberFactions() {
        ArrayList<StarFaction> memberFactions = new ArrayList<>();
        for(int factionID : memberFactionIDs) {
            StarFaction faction = new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(factionID));
            memberFactions.add(faction);
        }
        return memberFactions;
    }

    public void addMemberFaction(StarFaction faction) {
        memberFactionIDs.add(faction.getID());
    }

    public void removeMemberFaction(StarFaction faction) {
        memberFactionIDs.remove(faction.getID());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }
}
