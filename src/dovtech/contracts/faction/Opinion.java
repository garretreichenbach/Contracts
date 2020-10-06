package dovtech.contracts.faction;

public enum Opinion {
    ALL("ALL", 0, 0),
    HATED("HATED", -300, -149),
    HOSTILE("HOSTILE", -150, -66),
    POOR("POOR", -65, -21),
    COOL("COOL", -20, -6),
    NEUTRAL("NEUTRAL", -5, 5),
    CORDIAL("CORDIAL", 6, 20),
    GOOD("GOOD", 21, 64),
    EXCELLENT("EXCELLENT", 65, 149),
    TRUSTED("TRUSTED", 150, 300);

    public String display;
    public int min;
    public int max;
    Opinion(String display, int min, int max) {
        this.display = display;
        this.min = min;
        this.max = max;
    }

    public static Opinion getFromScore(int score) {
        if(score <= HATED.max) {
            return HATED;
        } else if(score <= HOSTILE.max) {
            return HOSTILE;
        } else if(score <= POOR.max) {
            return POOR;
        } else if(score <= COOL.max) {
            return COOL;
        } else if(score <= NEUTRAL.max) {
            return NEUTRAL;
        } else if(score <= CORDIAL.max) {
            return CORDIAL;
        } else if(score <= GOOD.max) {
            return GOOD;
        } else if(score <= EXCELLENT.max) {
            return EXCELLENT;
        } else {
            return TRUSTED;
        }
    }
}