package com.elmakers.mine.bukkit.api.spell;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface Spell extends SpellTemplate {
	public boolean cast();
	public boolean cast(String[] parameters);
	public long getCastCount();
	
	public Player getPlayer();
	public CommandSender getCommandSender();
	public Location getLocation();
	public Location getTargetLocation();
	public Entity getTargetEntity();
	public Vector getDirection();
}
