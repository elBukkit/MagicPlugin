package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FactionsManager {
	private boolean enabled = false;
	private Class<?> factionsManager = null;
	private Method factionsCanBuildMethod = null;
	private Method psFactoryMethod = null;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void initialize(Plugin plugin) {
		if (enabled) {
			try {
				Class<?> psClass = Class.forName("com.massivecraft.mcore.ps.PS");
				factionsManager = Class.forName("com.massivecraft.factions.listeners.FactionsListenerMain");
				factionsCanBuildMethod = factionsManager.getMethod("canPlayerBuildAt", Player.class, psClass, Boolean.TYPE);
				psFactoryMethod = psClass.getMethod("valueOf", Location.class);
				if (factionsManager != null && factionsCanBuildMethod != null && psFactoryMethod != null) {
					plugin.getLogger().info("Factions found, build permissions will be respected.");
				} else {
					factionsManager = null;
					factionsCanBuildMethod = null;
					psFactoryMethod = null;
				}
			} catch (Throwable ex) {
			}
			
			if (factionsManager == null) {
				plugin.getLogger().info("Factions not found, will not integrate.");
			}
		} else {
			plugin.getLogger().info("Factions integration disabled");
		}
	}
	
	public boolean hasBuildPermission(Player player, Block block) {
		if (enabled && block != null && factionsManager != null && factionsCanBuildMethod != null && psFactoryMethod != null) {
			
			// Disallows building via command blocks, or while offline, when Factions is present.
			if (player == null) return false;
			
			try {
				Object loc = psFactoryMethod.invoke(null, block.getLocation());
				return loc != null && (Boolean)factionsCanBuildMethod.invoke(null, player, loc, false);
			} catch (Throwable ex) {
				ex.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
}
