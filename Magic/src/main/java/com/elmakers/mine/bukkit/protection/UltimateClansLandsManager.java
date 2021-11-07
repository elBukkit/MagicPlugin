package com.elmakers.mine.bukkit.protection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.magic.MagicController;

import me.ulrich.lands.api.LandsAPI;

public class UltimateClansLandsManager implements BlockBuildManager, BlockBreakManager {
    public UltimateClansLandsManager(MagicController controller) {
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        return hasBuildPermission(player, block);
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        return LandsAPI.getInstance().canBuild(player, block.getLocation());
    }
}
