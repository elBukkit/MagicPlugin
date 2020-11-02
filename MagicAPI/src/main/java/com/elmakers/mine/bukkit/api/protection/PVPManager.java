package com.elmakers.mine.bukkit.api.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicProvider;

/**
 * Register via MageController.register()
 */
public interface PVPManager extends MagicProvider {
    boolean isPVPAllowed(Player player, Location location);
}
