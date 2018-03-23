package com.elmakers.mine.bukkit.elementals;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import info.nothingspecial.api.elementals.ElementalsAPI;

public class ElementalsController {
	private ElementalsAPI api;
	
	public ElementalsController(Plugin plugin) {
		api = (ElementalsAPI)plugin;
	}

	public boolean isElemental(Entity entity) {
		return api.isElemental(entity);
	}

	public boolean damageElemental(Entity entity, double damage, int fireTicks, CommandSender attacker) {
		return api.damageElemental(entity, damage, fireTicks, attacker);
	}

	public boolean setElementalScale(Entity entity, double scale) {
		return api.setElementalScale(entity, scale);
	}

	public double getElementalScale(Entity entity) {
		return api.getElementalScale(entity);
	}
	
	public boolean createElemental(Location location, String templateName, CommandSender creator) {
		return api.createElemental(location, templateName, creator);
	}
}
