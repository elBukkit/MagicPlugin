package com.elmakers.mine.bukkit.protection;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TownyAPI
{
    private final Towny towny;
    private final TownyManager controller;

    public TownyAPI(TownyManager manager, Plugin plugin) throws IllegalArgumentException {
        this.controller = manager;
        if (!(plugin instanceof Towny)) {
            throw new IllegalArgumentException("Towny plugin not an instance of Towny class");
        }
        towny = (Towny) plugin;
    }

    public boolean isPVPAllowed(Location location) {
        if (towny == null || location == null)
            return true;

        if (controller.wildernessBypass && TownyUniverse.isWilderness(location.getBlock())) {
            return true;
        }

        TownBlock townBlock = TownyUniverse.getTownBlock(location);
        if (townBlock == null) return true;
        Town town = null;
        try {
            if (townBlock.hasTown()) {
                town = townBlock.getTown();
            }
        } catch (NotRegisteredException ex){

        }
        if (town == null) return true;

        return town.isPVP();
    }

    public boolean hasBuildPermission(Player player, Block block) {
        if (block != null && towny != null) {
            if (controller.wildernessBypass && TownyUniverse.isWilderness(block)) {
                return true;
            }
            return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getTypeId(), block.getData(), TownyPermission.ActionType.BUILD);
        }
        return true;
    }

    public boolean hasBreakPermission(Player player, Block block) {
        if (block != null && towny != null) {
            if (controller.wildernessBypass && TownyUniverse.isWilderness(block)) {
                return true;
            }
            return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getTypeId(), block.getData(), TownyPermission.ActionType.DESTROY);
        }
        return true;
    }
}
