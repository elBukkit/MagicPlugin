package com.elmakers.mine.bukkit.api.spell;

import org.bukkit.Color;

public interface SpellCategory extends Comparable<SpellCategory> {
	public abstract String getKey();
	public abstract String getName();
	public abstract String getDescription();
	public abstract Color getColor();
	public abstract void addCast();
	public abstract long getCastCount();
	public abstract long getLastCast();
}