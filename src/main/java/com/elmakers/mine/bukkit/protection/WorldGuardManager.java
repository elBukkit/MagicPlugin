package com.elmakers.mine.bukkit.protection;

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
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void initialize(Plugin plugin) {
		if (enabled) {
			try {
				Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
				if (wgPlugin instanceof WorldGuardPlugin) {
					worldGuard = (WorldGuardPlugin)wgPlugin;
				}
			} catch (Throwable ex) {
			}
				
			if (worldGuard == null) {
				plugin.getLogger().info("WorldGuard not found, region protection and pvp checks will not be used.");
			}  else {
				plugin.getLogger().info("WorldGuard found, will respect build permissions for construction spells");
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
		if (checkSet == null || checkSet.size() == 0) return true;
		return checkSet.allows(DefaultFlag.PVP);
	}
	
	public boolean isPassthrough(Location location) {
		if (!enabled || worldGuard == null || location == null) return true;
				 
		RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        if (regionManager == null) return true;

		ApplicableRegionSet checkSet = regionManager.getApplicableRegions(location);
		if (checkSet == null || checkSet.size() == 0) return true;
		return checkSet.allows(DefaultFlag.PASSTHROUGH);
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
}
