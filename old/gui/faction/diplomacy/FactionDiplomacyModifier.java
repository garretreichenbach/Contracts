package thederpgamer.contracts.gui.faction.diplomacy;

public enum FactionDiplomacyModifier {

    ALL("All", 0, 0, 0),

    AT_WAR("Currently at war", -50, 0, 1),
    PREVIOUS_WAR("Previously at war", -30, 3, 2),

    AT_WAR_WITH_ENEMY("At war with one of their enemies", 50, 0, 3),
    PREVIOUS_WAR_WITH_ENEMY("Previously at war with one of their enemies", 30, -3, 4),

    AT_WAR_WITH_ALLY("At war with one of their allies", -30,0, 5),
    PREVIOUS_WAR_WITH_ALLY("Previously at war with one of their allies", -20, 3, 6),

    ALLIED_WITH_ENEMY("Allied to one of their enemies", -30, 0, 7),
    PREVIOUSLY_ALLIED_WITH_ENEMY("Previously allied to one of their enemies", -20, 3, 8),

    TRADING_WITH_ENEMY("Trading with one of their enemies", -15, 3, 9),
    TRADING_WITH_ALLY("Trading with one of their allies", 15, -3, 10),

    PARTICIPATED_COALITION_AGAINST("Participated in a coalition against them", -20, 3, 11),
    PARTICIPATED_COALITION_SAME_SIDE("Participaded in a coalition with them", 20, -3, 12),

    COMMON_ENEMY("Same enemy in an ongoing war", 15, 0, 13),
    CONTRACT_WORK("Did contract work for them", 10, -1, 14),
    IMPROVED_RELATIONS("Improved relations", 30, -1, 15),

    NON_AGGRESSION_PACT("In a non-aggression pact with them", 30, 0, 16),
    BROKE_NON_AGGRESSION_PACT("Broke a non-aggression pact with them", -30, 0, 17),
    DEFENSIVE_ALLIANCE("In a defensive alliance with them", 50, 0, 18),
    FEDERATION_MEMBER("In a federation with them", 80, 0, 19),
    TO_PUPPET("They are your puppet", 30, 0, 20),
    FROM_PUPPET("You are their puppet", 30, 0, 21),

    ADMIN_COMMAND("Set by admin", 300, 0, 22);

    private String display;
    private int modifier;
    private int changePerDay;
    private int ID;
    public String date = "N/A";

    FactionDiplomacyModifier(String display, int modifier, int changePerDay, int ID) {
        this.display = display;
        this.modifier = modifier;
        this.changePerDay = changePerDay;
        this.ID = ID;
    }

    public String getDisplay() {
        return display;
    }

    public int getModifier() {
        return modifier;
    }

    public int getChangePerDay() {
        return changePerDay;
    }

    public int getID() {
        return ID;
    }

    public static FactionDiplomacyModifier fromID(int modifierID) {
        for(FactionDiplomacyModifier m : values()) if(m.getID() == modifierID) return m;
        return ALL;
    }

    public static String[] displayList() {
        StringBuilder builder = new StringBuilder();
        for(FactionDiplomacyModifier action : values()) {
            builder.append(action.display).append(",");
        }
        return builder.toString().split(",");
    }
}
