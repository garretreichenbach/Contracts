package dovtech.contracts.faction;

import api.faction.StarFaction;
import dovtech.contracts.gui.faction.diplomacy.FactionDiplomacyModifier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class FactionData implements Serializable {

    private int factionID;
    private HashMap<Integer, ArrayList<Integer>> diplomacyModifiers;

    public FactionData(StarFaction faction) {
        this.factionID = faction.getID();
        this.diplomacyModifiers = new HashMap<>();
    }

    public FactionData() {
        this.diplomacyModifiers = new HashMap<>();
    }

    public void setFactionID(int factionID) {
        this.factionID = factionID;
    }

    public int getFactionID() {
        return factionID;
    }

    public void addModifier(StarFaction faction, FactionDiplomacyModifier modifier) {
        ArrayList<Integer> modifiers = new ArrayList<>();
        if(diplomacyModifiers.containsKey(faction.getID())) modifiers = diplomacyModifiers.get(faction.getID());
        modifiers.add(modifier.getID());
        diplomacyModifiers.put(faction.getID(), modifiers);
    }

    public ArrayList<FactionDiplomacyModifier> getModifiers(StarFaction faction) {
        ArrayList<FactionDiplomacyModifier> modifiers = new ArrayList<>();
        if(diplomacyModifiers.containsKey(faction.getID())) {
            for(int i : diplomacyModifiers.get(faction.getID())) modifiers.add(FactionDiplomacyModifier.fromID(i));
        }
        return modifiers;
    }

    public HashMap<Integer, ArrayList<Integer>> getAllModifiers() {
        return diplomacyModifiers;
    }
}