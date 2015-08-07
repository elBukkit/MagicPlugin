package com.elmakers.mine.bukkit.protection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface BlockBreakManager {
    public boolean hasBreakPermission(Player player, Block block);
}
