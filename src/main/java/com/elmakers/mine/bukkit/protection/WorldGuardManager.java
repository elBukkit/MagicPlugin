package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardManager {
    private boolean enabled = false;
    private WorldGuardAPI worldGuard = null;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && worldGuard != null && worldGuard.isEnabled();
    }

    public void initialize(Plugin plugin) {
        worldGuard = null;
        if (enabled) {
            try {
                Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
                if (wgPlugin != null) {
                    String[] versionPieces = StringUtils.split(wgPlugin.getDescription().getVersion(), '.');
                    int version = Integer.parseInt(versionPieces[0]);
                    if (version >= 6) {
                        worldGuard = new WorldGuardAPI(plugin);
                    } else {
                        plugin.getLogger().warning("Only WorldGuard 6 and above are supported- please update! (WG version: " + wgPlugin.getDescription().getVersion() + ")");
                    }
                }
            } catch (Throwable ex) {
            }

            if (worldGuard == null) {
                plugin.getLogger().info("WorldGuard not found, region protection and pvp checks will not be used.");
            } else {
                plugin.getLogger().info("WorldGuard found, will respect build permissions for construction spells");
            }
        } else {
            plugin.getLogger().info("WorldGuard integration disabled, region protection and pvp checks will not be used.");
        }
    }

    public boolean isPVPAllowed(Player player, Location location) {
        if (!enabled || worldGuard == null || location == null)
            return true;
        return worldGuard.isPVPAllowed(player, location);
    }

    public boolean hasBuildPermission(Player player, Block block) {
        if (enabled && block != null && worldGuard != null) {
            return worldGuard.hasBuildPermission(player, block);
        }
        return true;
    }

    public boolean hasCastPermission(Player player, SpellTemplate spell) {
        if (enabled && worldGuard != null) {
            return worldGuard.hasCastPermission(player, spell);
        }
        return true;
    }

    public boolean hasCastPermissionOverride(Player player, SpellTemplate spell) {
        if (enabled && worldGuard != null) {
            return worldGuard.hasCastPermissionOverride(player, spell);
        }
        return true;
    }
}
