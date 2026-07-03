package arc.struct;

public enum Team{
    blue, red, purple, green, orange, yellow, pink, gray, derelict;
    public boolean isPlayer(){ return this == blue || this == red; }
    public static Team get(int id){ return values()[id]; }
    public int id(){ return ordinal(); }
}