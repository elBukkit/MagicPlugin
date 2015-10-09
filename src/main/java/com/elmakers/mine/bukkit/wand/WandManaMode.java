package com.elmakers.mine.bukkit.wand;

public enum WandManaMode {
    NONE(false, false, false, false),
    BAR(true, false, false, false),
    NUMBER(false, true, false, false),
    DURABILITY(false, false, true, false),
    GLOW(false, false, false, true);

    private final boolean useXPBar;
    private final boolean useXPNumber;
    private final boolean useDurability;
    private final boolean useGlow;

    WandManaMode(boolean bar, boolean num, boolean dur, boolean glow) {
        useXPBar = bar;
        useXPNumber = num;
        useDurability = dur;
        useGlow = glow;
    }

    public boolean useXPBar() {
        return useXPBar;
    }

    public boolean useXPNumber() {
        return useXPNumber;
    }

    public boolean useDurability() {
        return useDurability;
    }

    public boolean useGlow() {
        return useGlow;
    }

    public boolean useXP() {
        return useXPBar  || useXPNumber;
    }
}
