package com.elmakers.mine.bukkit.regions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardManager {
	private boolean enabled = false;
	 private Method regionManagerCanBuildMethod = null;
	 private Object regionManager = null;
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void initialize(Plugin plugin) {
		if (enabled) {
			try {
				regionManager = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
				regionManagerCanBuildMethod = regionManager.getClass().getMethod("canBuild", Player.class, Block.class);
				if (regionManagerCanBuildMethod != null) {
					plugin.getLogger().info("WorldGuard found, will respect build permissions for construction spells");
				} else {
					regionManager = null;
				}
			} catch (Throwable ex) {
			}
				
			if (regionManager == null) {
				plugin.getLogger().info("WorldGuard not found, region protection and pvp checks will not be used.");
			} 
		} else {
			plugin.getLogger().info("Region manager disabled, region protection and pvp checks will not be used.");
			regionManager = null;
		}
	}
	
	public boolean isPVPAllowed(Location location) {
		if (!enabled || regionManager == null || location == null) return true;
				 
		try {
			Method getRegionManagerMethod = regionManager.getClass().getMethod("getRegionManager", World.class);
			if (getRegionManagerMethod == null) throw new Exception("Can't hook to getRegionManager method");
			Object worldManager = getRegionManagerMethod.invoke(regionManager, location.getWorld());
			if (worldManager == null) return true;
			Class<?> regionManagerClass = Class.forName("com.sk89q.worldguard.protection.managers.RegionManager");
			Method getApplicableRegionsMethod = regionManagerClass.getMethod("getApplicableRegions", Location.class);
			if (getApplicableRegionsMethod == null) throw new Exception("Can't hook to getApplicableRegions method");
			Object applicableSet = getApplicableRegionsMethod.invoke(worldManager, location);
			if (applicableSet == null) return true;
			Class<?> stateFlagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag");
			Method allowsMethod = applicableSet.getClass().getMethod("allows", stateFlagClass);
			if (allowsMethod == null) throw new Exception("Can't hook to allows method");
			
			// This is super-duper hacky :\
			// There's no real API for worldguard though, and I don't want a hard dependency. Maybe I could
			// encapsulate somehow?
			Class<?> defaultFlagClass = Class.forName("com.sk89q.worldguard.protection.flags.DefaultFlag");
			Field pvpField = defaultFlagClass.getField("PVP");
			if (pvpField == null) throw new Exception("Can't find PVP field");
			Object pvpFlag = pvpField.get(null);
			return (Boolean)allowsMethod.invoke(applicableSet, pvpFlag);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		return false;
	}
	
	public boolean hasBuildPermission(Player player, Block block) {
		if (enabled && player != null && block != null && regionManager != null && regionManagerCanBuildMethod != null) {
			try {
				return (Boolean)regionManagerCanBuildMethod.invoke(regionManager, player, block);
			} catch (Throwable ex) {
				ex.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
}
