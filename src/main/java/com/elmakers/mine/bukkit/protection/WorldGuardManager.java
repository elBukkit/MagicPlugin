package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WorldGuardManager {
	private boolean enabled = false;
	private WorldGuardPlugin worldGuard = null;
    private WGCustomFlagsManager customFlags = null;
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled && worldGuard != null;
	}
	
	public void initialize(Plugin plugin) {
		if (enabled) {
			try {
				Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
				if (wgPlugin instanceof WorldGuardPlugin) {
					String[] versionPieces = StringUtils.split(wgPlugin.getDescription().getVersion(), '.');
					int version = Integer.parseInt(versionPieces[0]);
					if (version >= 6) {
						worldGuard = (WorldGuardPlugin)wgPlugin;
					} else {
						plugin.getLogger().warning("Only WorldGuard 6 and above are supported- please update! (WG version: " + wgPlugin.getDescription().getVersion() + ")");
					}
				}
			} catch (Throwable ex) {
			}
				
			if (worldGuard == null) {
				plugin.getLogger().info("WorldGuard not found, region protection and pvp checks will not be used.");
			}  else {
                try {
                    Plugin customFlagsPlugin = plugin.getServer().getPluginManager().getPlugin("WGCustomFlags");
                    if (customFlagsPlugin != null) {
                        customFlags = new WGCustomFlagsManager(customFlagsPlugin);
                    }
                } catch (Throwable ex) {
                }

				plugin.getLogger().info("WorldGuard found, will respect build permissions for construction spells");
			    if (customFlags != null) {
                    plugin.getLogger().info("WGCustomFlags found, adding allowed-spells and blocked-spells flags");
                }
            }
		} else {
			plugin.getLogger().info("Region manager disabled, region protection and pvp checks will not be used.");
			worldGuard = null;
		}
	}
	
	public boolean isPVPAllowed(Location location) {
		if (!enabled || worldGuard == null || location == null) return true;
				 
		RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        if (regionManager == null) return true;

		ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
		if (checkSet == null) return true;

		return checkSet.queryState(null, DefaultFlag.PVP) != StateFlag.State.DENY;
	}
	
	public boolean isPassthrough(Location location) {
		if (!enabled || worldGuard == null || location == null) return true;
				 
		RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        if (regionManager == null) return true;

		ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
		if (checkSet == null) return true;
		return checkSet.size() == 0 || checkSet.testState(null, DefaultFlag.PASSTHROUGH);
	}
	
	public boolean hasBuildPermission(Player player, Block block) {
		if (enabled && block != null && worldGuard != null) {
			
			// Disallow building in non-passthrough regions from a command block or offline player
			if (player == null) {
				return isPassthrough(block.getLocation());
			}
			
			return worldGuard.canBuild(player, block);
		}
		
		return true;
	}

    public boolean hasCastPermission(Player player, SpellTemplate spell) {
        if (player != null && worldGuard != null && customFlags != null) {
            Location location = player.getLocation();
            RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
            if (regionManager == null) return true;

            ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
            if (checkSet == null) return true;

           return customFlags.canCast(checkSet, spell.getSpellKey().getBaseKey());
        }
        return true;
    }
}
