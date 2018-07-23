package com.elmakers.mine.bukkit.protection;

import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

public class PreciousStonesManager implements BlockBuildManager, BlockBreakManager, PVPManager {
    private boolean enabled = false;
    private boolean override = true;
    private PreciousStonesAPI api;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public boolean isEnabled() {
        return enabled && api != null && api.isEnabled();
    }

    public void initialize(Plugin plugin) {
        if (enabled) {
            try {
                Plugin psPlugin = plugin.getServer().getPluginManager().getPlugin("PreciousStones");
                if (psPlugin != null) {
                    api = new PreciousStonesAPI(plugin, psPlugin);
                    plugin.getLogger().info("PreciousStones found, will respect build and PVP permissions for force fields");
                }
            } catch (Throwable ignored) {
            }
        } else {
            plugin.getLogger().info("PreciousStones manager disabled, field protection and pvp checks will not be used.");
            api = null;
        }
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        if (!enabled || api == null || location == null)
        {
            return true;
        }
        return api.isPVPAllowed(player, location);
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        if (!enabled || block == null || api == null)
        {
            return true;
        }
        return api.hasBuildPermission(player, block);
    }

    @Nullable
    public Boolean getCastPermission(Player player, SpellTemplate spell, Location location) {
        if (!override || !enabled || api == null || location == null)
        {
            return null;
        }
        return api.getCastPermission(player, spell, location);
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        return hasBuildPermission(player, block);
    }

    public boolean canTarget(Entity source, Entity target) {
        if (!enabled || target == null || api == null)
        {
            return true;
        }

        return api.canTarget(source, target);
    }

    public boolean createField(Location location, Player player) {
        if (!enabled || api == null || location == null || player == null)
            return false;

        return api.createField(location, player);
    }

    public boolean rentField(Location signLocation, Player player, String rent, String timePeriod, BlockFace signDirection) {
        if (!enabled || api == null || signLocation == null || player == null)
            return false;

        return api.rentField(signLocation, player, rent, timePeriod, signDirection);
    }

    @Nullable
    public Map<String, Location> getFieldLocations(Player player) {
        if (!enabled || api == null || player == null)
            return null;

        return api.getFieldLocations(player);
    }
}
