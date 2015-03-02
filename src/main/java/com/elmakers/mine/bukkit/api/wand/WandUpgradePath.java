package com.elmakers.mine.bukkit.api.wand;

public interface WandUpgradePath {
    public String getKey();
    public String getName();
    public boolean requiresSpell(String spellKey);
    public boolean hasSpell(String spellKey);
    public String getDescription();
    public boolean hasPath(String pathName);
}
