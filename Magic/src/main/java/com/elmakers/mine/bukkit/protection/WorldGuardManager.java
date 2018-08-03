package com.elmakers.mine.bukkit.protection;

import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class WorldGuardManager implements PVPManager, BlockBreakManager, BlockBuildManager {
    private boolean enabled = false;
    private WorldGuardAPI worldGuard = null;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && worldGuard != null && worldGuard.isEnabled();
    }

    public void initialize(Plugin plugin) {
        if (enabled) {
            if (worldGuard == null) {
                plugin.getLogger().info("WorldGuard not found, region protection and pvp checks will not be used.");
            } else {
                plugin.getLogger().info("WorldGuard found, will respect build permissions for construction spells");
                worldGuard.checkFlagSupport();
            }
        } else {
            worldGuard = null;
            plugin.getLogger().info("WorldGuard integration disabled, region protection and pvp checks will not be used.");
        }
    }

    public void initializeFlags(Plugin plugin) {
        try {
            Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
            if (wgPlugin != null) {
                String[] versionPieces = StringUtils.split(wgPlugin.getDescription().getVersion(), '.');
                int version = Integer.parseInt(versionPieces[0]);
                if (version >= 6) {
                    worldGuard = new WorldGuardAPI(wgPlugin, plugin);
                } else {
                    plugin.getLogger().warning("Only WorldGuard 6 and above are supported- please update! (WG version: " + wgPlugin.getDescription().getVersion() + ")");
                }
            }
        } catch (Throwable ex) {
            plugin.getLogger().log(Level.WARNING, "Error setting up custom WorldGuard flags", ex);
        }
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        if (!enabled || worldGuard == null || location == null)
            return true;
        return worldGuard.isPVPAllowed(player, location);
    }

    public boolean isExitAllowed(Player player, Location location) {
        if (!enabled || worldGuard == null || location == null)
            return true;
        return worldGuard.isExitAllowed(player, location);
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        if (enabled && block != null && worldGuard != null) {
            return worldGuard.hasBuildPermission(player, block);
        }
        return true;
    }

    @Nullable
    public Boolean getCastPermission(Player player, SpellTemplate spell, Location location) {
        if (enabled && worldGuard != null) {
            return worldGuard.getCastPermission(player, spell, location);
        }
        return null;
    }

    @Nullable
    public Boolean getWandPermission(Player player, Wand wand, Location location) {
        if (enabled && worldGuard != null) {
            return worldGuard.getWandPermission(player, wand, location);
        }
        return null;
    }

    @Nullable
    public String getReflective(Player player, Location location) {
        if (enabled && worldGuard != null) {
            return worldGuard.getReflective(player, location);
        }
        return null;
    }

    @Nullable
    public String getDestructible(Player player, Location location) {
        if (enabled && worldGuard != null) {
            return worldGuard.getDestructible(player, location);
        }
        return null;
    }

    @Nullable
    public Set<String> getSpellOverrides(Player player, Location location) {
        if (enabled && worldGuard != null) {
            return worldGuard.getSpellOverrides(player, location);
        }
        return null;
    }

    @Nullable
    public Boolean inTaggedRegion(Location location, Set<String> tags) {
        if (enabled && worldGuard != null) {
            return worldGuard.inTaggedRegion(location, tags);
        }
        return null;
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        return hasBuildPermission(player, block);
    }
}
