package com.elmakers.mine.bukkit.warp;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.locations.NamedLocation;
import com.sk89q.commandbook.locations.RootLocationManager;
import com.sk89q.commandbook.locations.WarpsComponent;
import com.zachsthings.libcomponents.ComponentManager;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;

public class WarpController {
	private RootLocationManager<NamedLocation> locationManager = null;
	
	public Location getWarp(String warpName) {
		if (locationManager == null) return null;
		NamedLocation location = locationManager.get(null, warpName);
		if (location == null) return null;
		return location.getLocation();
	}
	
	public boolean setCommandBook(Plugin commandBook) {
		if (commandBook instanceof CommandBook) {
			ComponentManager<BukkitComponent> componentManager = ((CommandBook)commandBook).getComponentManager();
			WarpsComponent component = componentManager.getComponent(WarpsComponent.class);
			if (component == null) return false;
			
			locationManager = component.getManager();
			return locationManager != null;
		}
		
		return false;
	}
}
