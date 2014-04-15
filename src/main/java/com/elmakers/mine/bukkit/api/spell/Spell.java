package com.elmakers.mine.bukkit.api.spell;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;

public interface Spell extends Comparable<Spell> {
	public String getName();
	public String getDescription();
	public String getKey();
	public String getCategory();
	public String getUsage();
	public MaterialAndData getIcon();
	public boolean hasSpellPermission(CommandSender sender);
	public boolean cast();
	public boolean cast(String[] parameters);
	public Collection<CastingCost> getCosts();
	public Collection<CastingCost> getActiveCosts();
	public long getCastCount();
}
