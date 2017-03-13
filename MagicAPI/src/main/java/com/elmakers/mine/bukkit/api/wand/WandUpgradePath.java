package com.elmakers.mine.bukkit.api.wand;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;

import java.util.Collection;
import java.util.Set;

public interface WandUpgradePath {
    String getKey();
    String getName();
    MaterialAndData getIcon();
    boolean requiresSpell(String spellKey);
    boolean hasSpell(String spellKey);
    boolean hasExtraSpell(String spellKey);
    String getDescription();
    boolean hasPath(String pathName);
    boolean hasUpgrade();
    boolean checkUpgradeRequirements(Wand wand, Mage mage);
    Set<String> getTags();
    boolean hasTag(String tag);
    boolean hasAnyTag(Collection<String> tagSet);
    boolean hasAllTags(Collection<String> tagSet);
    Set<String> getMissingTags(Collection<String> tagSet);
    WandUpgradePath getUpgrade();
    String translatePath(String path);
    void upgrade(Wand wand, Mage mage);
    void checkMigration(Wand wand);
    Collection<String> getSpells();
    Collection<String> getExtraSpells();
    Collection<String> getRequiredSpells();
    boolean canEnchant(Wand wand);
    boolean earnsSP();
}
