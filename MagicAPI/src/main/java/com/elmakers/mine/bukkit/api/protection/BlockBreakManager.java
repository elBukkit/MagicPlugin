package com.elmakers.mine.bukkit.api.protection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface BlockBreakManager {
    boolean hasBreakPermission(Player player, Block block);
}
