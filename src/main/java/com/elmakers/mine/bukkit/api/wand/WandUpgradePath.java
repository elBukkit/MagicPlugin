package com.elmakers.mine.bukkit.api.wand;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;

import java.util.Collection;

public interface WandUpgradePath {
    public String getKey();
    public String getName();
    public MaterialAndData getIcon();
    public boolean requiresSpell(String spellKey);
    public boolean hasSpell(String spellKey);
    public String getDescription();
    public boolean hasPath(String pathName);
    public boolean hasUpgrade();
    public boolean checkUpgradeRequirements(Wand wand, Mage mage);
    public WandUpgradePath getUpgrade();
    public String translatePath(String path);
    public void upgrade(Wand wand, Mage mage);
    public void checkMigration(Wand wand);
    public Collection<String> getSpells();
    public Collection<String> getRequiredSpells();
    public boolean canEnchant(Wand wand);
    public boolean earnsSP();
}
