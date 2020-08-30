package com.elmakers.mine.bukkit.protection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;
import com.elmakers.mine.bukkit.api.protection.PlayerWarp;
import com.elmakers.mine.bukkit.api.protection.PlayerWarpManager;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;

public class RedProtectManager implements BlockBreakManager, BlockBuildManager, EntityTargetingManager,
        PlayerWarpManager, PVPManager {
    private final MageController controller;
    private RedProtectAPI redProtect;
    private boolean allowNonPlayerBuild;
    private boolean allowNonPlayerBreak;

    @SuppressWarnings({ "unchecked" })
    public RedProtectManager(Plugin residencePlugin, MageController controller, ConfigurationSection configuration) {
        this.controller = controller;
        this.redProtect = RedProtect.get().getAPI();
        allowNonPlayerBuild = configuration.getBoolean("allow_non_player_build", false);
        allowNonPlayerBreak = configuration.getBoolean("allow_non_player_break", false);
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        try {
            Region region = redProtect.getRegion(block.getLocation());
            if (region == null) {
                return true;
            }
            if (player == null) {
                return allowNonPlayerBuild;
            }

            return region.canBuild(player);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with RedProtect build checks", ex);
            return true;
        }
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        try {
            Region region = redProtect.getRegion(block.getLocation());
            if (region == null) {
                return true;
            }
            if (player == null) {
                return allowNonPlayerBreak;
            }

            return region.canBuild(player);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with RedProtect break checks", ex);
            return true;
        }
    }

    @Override
    public boolean canTarget(Entity source, Entity target) {
        try {
            Region region = redProtect.getRegion(source.getLocation());
            if (region == null) {
                return true;
            }
            if (source instanceof Player && target instanceof Player) {
                return region.canPVP((Player)source, (Player)target);
            }
            if (region.getFlagBool("passives") && !(target instanceof Player)) {
                if (source instanceof Player && region.isMember((Player)source)) {
                    return true;
                }
                return false;
            }

            return true;
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with RedProtect targeting checks", ex);
            return true;
        }
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        try {
            Region region = redProtect.getRegion(location);
            if (region == null) {
                return true;
            }
            return region.getFlagBool("pvp");
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with RedProtect pvp checks", ex);
            return true;
        }
    }

    @Nullable
    @Override
    public Collection<PlayerWarp> getWarps(@Nonnull Player player) {
        Collection<Region> regions = redProtect.getPlayerRegions(player);
        if (regions == null || regions.isEmpty()) {
            return null;
        }
        Collection<PlayerWarp> warps = new ArrayList<>();
        for (Region region : regions) {
            Location location = region.getTPPoint();
            if (location == null) {
                location = region.getCenterLoc();
                location = location.getWorld().getHighestBlockAt(location).getLocation();
            }
            PlayerWarp warp = new PlayerWarp(region.getName(), location);
            warps.add(warp);
        }
        return warps;
    }
}
