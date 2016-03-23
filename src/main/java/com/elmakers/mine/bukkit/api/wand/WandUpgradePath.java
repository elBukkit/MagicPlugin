package com.elmakers.mine.bukkit.api.wand;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;

import java.util.Collection;
import java.util.Set;

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
    public Set<String> getTags();
    public boolean hasTag(String tag);
    public boolean hasAnyTag(Collection<String> tagSet);
    public boolean hasAllTags(Collection<String> tagSet);
    public Set<String> getMissingTags(Collection<String> tagSet);
    public WandUpgradePath getUpgrade();
    public String translatePath(String path);
    public void upgrade(Wand wand, Mage mage);
    public void checkMigration(Wand wand);
    public Collection<String> getSpells();
    public Collection<String> getRequiredSpells();
    public boolean canEnchant(Wand wand);
    public boolean earnsSP();
}
