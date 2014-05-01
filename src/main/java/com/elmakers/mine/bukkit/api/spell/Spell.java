package com.elmakers.mine.bukkit.api.spell;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

/**
 * Represents a Spell that may be cast by a Mage.
 * 
 * Each Spell is based on a SpellTemplate, which are defined
 * by the spells configuration files.
 * 
 * Every spell uses a specific Class that must extend from
 * com.elmakers.mine.bukkit.plugins.magic.spell.Spell. Implementing the Spell interface
 * is required, but not sufficient, for a custom Spell.
 * 
 * This means that custom spells require building against MagicPlugin, which also provides
 * a variety of helpful base classes to start from, such as com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell
 * and com.elmakers.mine.bukkit.plugins.magic.spell.BrushSpell.
 */
public interface Spell extends SpellTemplate {
	public boolean cast();
	public boolean cast(String[] parameters);
	public boolean cast(String[] parameters, Location defaultLocation);
	public long getCastCount();
	public Player getPlayer();
	public CommandSender getCommandSender();
	public Location getLocation();
	public Location getTargetLocation();
	public Entity getTargetEntity();
	public Vector getDirection();
	
	public void activate();
	public void deactivate();
	public boolean isActive();
	public boolean cancel();
	
	public void setMage(Mage mage);
	public Mage getMage();
	
	public void load(ConfigurationSection node);
	public void save(ConfigurationSection node);
	
	public void tick();
	
	public void initialize(MageController controller);
	public boolean hasBrushOverride();
}
