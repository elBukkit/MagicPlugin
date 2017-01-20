package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.logging.Level;

public class WorldGuardAPI {
	private WorldGuardPlugin worldGuard = null;
    private WGCustomFlagsManager customFlags = null;

	public boolean isEnabled() {
		return worldGuard != null;
	}
	
	public WorldGuardAPI(Plugin plugin, Plugin owningPlugin) {
        if (plugin instanceof WorldGuardPlugin) {
            worldGuard = (WorldGuardPlugin)plugin;
            try {
                owningPlugin.getLogger().info("Pre-check for WorldGuard custom flag registration");
                customFlags = new WGCustomFlagsManager(owningPlugin, worldGuard);
            } catch (NoSuchMethodError incompatible) {
                owningPlugin.getLogger().log(Level.WARNING, "Failed to set up custom flags, please make sure you are on WorldGuard 6.2 or above");
            } catch (Throwable ex) {
                owningPlugin.getLogger().log(Level.WARNING, "Unexpected error setting up custom flags, please make sure you are on WorldGuard 6.2 or above", ex);
            }
        }
	}

    protected RegionAssociable getAssociable(Player player) {
        RegionAssociable associable;
        if (player == null) {
            associable = Associables.constant(Association.NON_MEMBER);
        } else {
            associable = worldGuard.wrapPlayer(player);
        }

        return associable;
    }
	
	public boolean isPVPAllowed(Player player, Location location) {
		if (worldGuard == null || location == null) return true;
				 
		RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        if (regionManager == null) return true;

		ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
		if (checkSet == null) return true;

		return checkSet.queryState(getAssociable(player), DefaultFlag.PVP) != StateFlag.State.DENY;
	}

	public boolean hasBuildPermission(Player player, Block block) {
		if (block != null && worldGuard != null) {
            RegionContainer container = worldGuard.getRegionContainer();
			return container.createQuery().testState(block.getLocation(), getAssociable(player), DefaultFlag.BUILD);
		}

		return true;
	}

    public Boolean getCastPermission(Player player, SpellTemplate spell, Location location) {
        if (location != null && worldGuard != null && customFlags != null)
        {
            RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
            if (regionManager == null) {
                return null;
            }

            ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getCastPermission(getAssociable(player), checkSet, spell);
        }
        return null;
    }

    public Boolean getWandPermission(Player player, Wand wand, Location location) {
        if (location != null && worldGuard != null && customFlags != null)
        {
            RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
            if (regionManager == null) {
                return null;
            }

            ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getWandPermission(getAssociable(player), checkSet, wand);
        }
        return null;
    }

    public String getReflective(Player player, Location location) {
        if (location != null && worldGuard != null && customFlags != null)
        {
            RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
            if (regionManager == null) {
                return null;
            }

            ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getReflective(getAssociable(player), checkSet);
        }
        return null;
    }

    public Set<String> getSpellOverrides(Player player, Location location) {
        if (location != null && worldGuard != null && customFlags != null)
        {
            RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
            if (regionManager == null) {
                return null;
            }

            ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getSpellOverrides(getAssociable(player), checkSet);
        }
        return null;
    }

    public String getDestructible(Player player, Location location) {
        if (location != null && worldGuard != null && customFlags != null)
        {
            RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
            if (regionManager == null) {
                return null;
            }

            ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getDestructible(getAssociable(player), checkSet);
        }
        return null;
    }
}
