package dovtech.contracts.faction;

public enum Opinion {
    HATED("Hated", -300, -149),
    HOSTILE("Hostile", -150, -66),
    POOR("Poor", -65, -21),
    COOL("Cool", -20, -6),
    NEUTRAL("Neutral", -5, 5),
    CORDIAL("Cordial", 6, 20),
    GOOD("Good", 21, 64),
    EXCELLENT("Excellent", 65, 149),
    TRUSTED("Trusted", 150, 300);

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