package com.elmakers.mine.bukkit.metrics;

import org.mcstats.Metrics.Plotter;

import com.elmakers.mine.bukkit.api.spell.Spell;

public class SpellCastPlotter extends Plotter {
	private final Spell spell;
	
	public SpellCastPlotter(Spell spell) {
		super(spell.getName());
		this.spell = spell;
	}
	
	@Override
	public int getValue() {
		return (int)spell.getCastCount();
	}
}
