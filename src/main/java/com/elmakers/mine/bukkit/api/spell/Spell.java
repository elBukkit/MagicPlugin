package com.elmakers.mine.bukkit.api.spell;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Represents a Spell that may be cast by a Mage.
 * 
 * Each Spell is based on a SpellTemplate, which are defined
 * by the spells configuration files.
 * 
 * Every spell uses a specific Class that must extend from
 * com.elmakers.mine.bukkit.plugins.magic.spell.Spell.
 * 
 * To create a new custom spell from scratch, you must also
 * implement the MageSpell interface.
 */
public interface Spell extends SpellTemplate {
	public boolean cast();
	public boolean cast(String[] parameters);
	public boolean cast(String[] parameters, Location defaultLocation);
	public Player getPlayer();
	public CommandSender getCommandSender();
	public Location getLocation();
	public Location getTargetLocation();
	public Entity getTargetEntity();
	public Vector getDirection();
	public boolean isActive();
	public boolean hasBrushOverride();
}
