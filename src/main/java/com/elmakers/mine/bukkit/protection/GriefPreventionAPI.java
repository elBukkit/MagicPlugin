package com.elmakers.mine.bukkit.protection;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GriefPreventionAPI
{
    private final GriefPrevention griefPrevention;

    public GriefPreventionAPI(Plugin plugin) throws IllegalArgumentException {

        if (!(plugin instanceof GriefPrevention)) {
            throw new IllegalArgumentException("GriefPrevention plugin not an instance of GriefPrevention class");
        }
        griefPrevention = GriefPrevention.instance;
    }

    public boolean hasBuildPermission(Player player, Block block) {
        if (block != null && griefPrevention != null) {
            Claim claim = griefPrevention.dataStore.getClaimAt(block.getLocation(), false, null);
            if (claim != null) {
                return claim.allowEdit(player) == null;
            }
        }
        return true;
    }

    public boolean hasBreakPermission(Player player, Block block) {
        if (block != null && griefPrevention != null) {
            Claim claim = griefPrevention.dataStore.getClaimAt(block.getLocation(), false, null);
            if (claim != null) {
                return claim.allowEdit(player) == null;
            }
        }
        return true;
    }

    public boolean isPVPAllowed(Location location) {
        if (location != null && griefPrevention != null && griefPrevention.config_pvp_noCombatInPlayerLandClaims) {
            Claim claim = griefPrevention.dataStore.getClaimAt(location, false, null);
            return (claim != null);
        }
        return true;
    }
}
