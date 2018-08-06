package com.elmakers.mine.bukkit.api.protection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface BlockBuildManager {
    public boolean hasBuildPermission(Player player, Block block);
}
