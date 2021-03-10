package thederpgamer.contracts.faction;

import api.faction.StarFaction;
import api.mod.StarLoader;
import thederpgamer.contracts.federation.Federation;
import thederpgamer.contracts.gui.faction.diplomacy.FactionDiplomacyModifier;
import thederpgamer.contracts.util.FactionUtils;
import thederpgamer.contracts.util.FederationUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class FactionData implements Serializable {

    private int factionID;
    private HashMap<Integer, ArrayList<Integer>> diplomacyModifiers;
    private double factionPower;
    private int federationID;

    public FactionData(StarFaction faction) {
        this.factionID = faction.getID();
        this.diplomacyModifiers = new HashMap<>();
        this.factionPower = 100.0;
        this.federationID = 0;
    }

    public FactionData() {
        this.factionID = 0;
        this.diplomacyModifiers = new HashMap<>();
        this.factionPower = 100.0;
        this.federationID = 0;
    }

    public void setFactionID(int factionID) {
        this.factionID = factionID;
    }

    public int getFactionID() {
        return factionID;
    }

    public void setFactionPower(double factionPower) {
        this.factionPower = factionPower;
    }

    public double getFactionPower() {
        return factionPower;
    }

    public void addModifier(StarFaction faction, FactionDiplomacyModifier modifier) {
        ArrayList<Integer> modifiers = new ArrayList<>();
        if(faction.getInternalFaction() != null)  {
            if(diplomacyModifiers.containsKey(faction.getID())) {
                modifiers = diplomacyModifiers.get(faction.getID());
            }
        } else {
            diplomacyModifiers = new HashMap<>();
        }
        modifiers.add(modifier.getID());
        if(faction.getInternalFaction().getIdFaction() != 0) diplomacyModifiers.put(faction.getID(), modifiers);
    }

    public ArrayList<FactionDiplomacyModifier> getModifiers(StarFaction faction) {
        ArrayList<FactionDiplomacyModifier> modifiers = new ArrayList<>();
        if(faction.getInternalFaction() != null && faction.getInternalFaction().getIdFaction() != 0) {
            modifiers = FactionUtils.getOpinionModifiers(getFaction(), faction);
        }
        return modifiers;
    }

    public HashMap<Integer, ArrayList<Integer>> getAllModifiers() {
        return diplomacyModifiers;
    }

    public StarFaction getFaction() {
        if(factionID != 0) {
            return new StarFaction(StarLoader.getGameState().getFactionManager().getFaction(factionID));
        } else {
            return null;
        }
    }

    public Federation getFederation() {
        if(federationID != 0) {
            return FederationUtils.getFederationFromID(federationID);
        } else {
            return null;
        }
    }
}