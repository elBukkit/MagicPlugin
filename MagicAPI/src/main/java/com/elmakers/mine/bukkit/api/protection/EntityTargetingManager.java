package com.elmakers.mine.bukkit.api.protection;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MagicProvider;

/**
 * Register via MageController.register()
 */
public interface EntityTargetingManager extends MagicProvider {
    boolean canTarget(Entity source, Entity target);
}
