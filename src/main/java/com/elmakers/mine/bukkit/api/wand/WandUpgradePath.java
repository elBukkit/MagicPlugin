package com.elmakers.mine.bukkit.api.wand;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface WandUpgradePath {
    public String getKey();
    public String getName();
    public boolean requiresSpell(String spellKey);
    public boolean hasSpell(String spellKey);
    public String getDescription();
    public boolean hasPath(String pathName);
    public boolean hasUpgrade();
    public boolean checkUpgradeRequirements(Wand wand, Mage mage);
    public WandUpgradePath getUpgrade();
    public void upgrade(Wand wand, Mage mage);
}
