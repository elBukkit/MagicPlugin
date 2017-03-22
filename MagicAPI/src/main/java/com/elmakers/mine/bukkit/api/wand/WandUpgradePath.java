package com.elmakers.mine.bukkit.api.wand;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;

import java.util.Set;

/**
 * This interface is deprecated and will be replaced by ProgressionPath.
 *
 * Please transition to ProgressionPath as soon as possible to keep your
 * integration future-proof!
 */
// TODO: Actually deprecate this when we can.
//@Deprecated
public interface WandUpgradePath extends ProgressionPath {
    boolean checkUpgradeRequirements(Wand wand, Mage mage);
    Set<String> getTags();
    WandUpgradePath getUpgrade();
    void upgrade(Wand wand, Mage mage);
    void checkMigration(Wand wand);
    boolean canEnchant(Wand wand);
}
