package com.elmakers.mine.bukkit.api.protection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicProvider;

/**
 * Register via PreLoadEvent.register()
 */
public interface BlockBreakManager extends MagicProvider {
    boolean hasBreakPermission(Player player, Block block);
}
