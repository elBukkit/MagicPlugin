package com.elmakers.mine.bukkit.api.wand;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;

/**
 * This interface is deprecated and will be replaced by ProgressionPath.
 *
 * <p>Please transition to ProgressionPath as soon as possible to keep your
 * integration future-proof!
 */
// TODO: Actually deprecate this when we can.
//@Deprecated
public interface WandUpgradePath extends ProgressionPath {
    @Override
    Set<String> getTags();
    @Nullable
    WandUpgradePath getUpgrade();
    void upgrade(@Nonnull Wand wand, Mage mage);
    void checkMigration(Wand wand);
    boolean canEnchant(Wand wand);
}
