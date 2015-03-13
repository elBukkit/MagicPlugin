package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.Method;
import java.util.logging.Level;

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
		return enabled && factionsManager != null;
	}
	
	public void initialize(Plugin plugin) {
		if (enabled) {
			Plugin factionsPlugin = plugin.getServer().getPluginManager().getPlugin("Factions");
			if (factionsPlugin != null)
			{
				try {
					Class<?> psClass = Class.forName("com.massivecraft.massivecore.ps.PS");
					factionsManager = Class.forName("com.massivecraft.factions.engine.EngineMain");
					factionsCanBuildMethod = factionsManager.getMethod("canPlayerBuildAt", Object.class, psClass, Boolean.TYPE);
					psFactoryMethod = psClass.getMethod("valueOf", Location.class);
					if (factionsManager == null || factionsCanBuildMethod == null || psFactoryMethod == null) {
						factionsManager = null;
						factionsCanBuildMethod = null;
						psFactoryMethod = null;
					}
				} catch (Throwable ex) {
					// Try for Factions "Limited"
					psFactoryMethod = null;
                    try {
                        factionsManager = Class.forName("com.massivecraft.factions.listeners.FactionsBlockListener");
                        factionsCanBuildMethod = factionsManager.getMethod("playerCanBuildDestroyBlock", Player.class, Block.class, String.class, Boolean.TYPE);
                        if (factionsManager == null || factionsCanBuildMethod == null) {
                            factionsManager = null;
                            factionsCanBuildMethod = null;
                        } else {
                            plugin.getLogger().info("Factions 1.8.2+ build found");
                        }
                    } catch(Throwable ex2) {
                        plugin.getLogger().log(Level.WARNING, "Failed to find mcore", ex);
                        plugin.getLogger().log(Level.WARNING, "Failed to find FactionsBlockListener", ex2);
                    }
				}

				if (factionsManager == null) {
					plugin.getLogger().info("Factions integration failed.");
				} else {
					plugin.getLogger().info("Factions found, will integrate for build checks.");
				}
			} else {
				plugin.getLogger().info("Factions not found, will not integrate.");
			}
		} else {
			plugin.getLogger().info("Factions integration disabled");
		}
	}
	
	public boolean hasBuildPermission(Player player, Block block) {
		if (enabled && block != null && factionsManager != null && factionsCanBuildMethod != null) {
			
			// Disallows building via command blocks, or while offline, when Factions is present.
			if (player == null) return false;
			
			try {
                if (psFactoryMethod != null) {
                    Object loc = psFactoryMethod.invoke(null, block.getLocation());
                    return loc != null && (Boolean)factionsCanBuildMethod.invoke(null, player, loc, false);
                }
                return (Boolean)factionsCanBuildMethod.invoke(null, player, block, "destroy", true);
			} catch (Throwable ex) {
				ex.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
}
