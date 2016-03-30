package com.elmakers.mine.bukkit.metrics;

import org.mcstats.Metrics.Plotter;
import com.elmakers.mine.bukkit.spell.SpellCategory;

public class CategoryCastPlotter extends Plotter {
	private final SpellCategory category;
	
	public CategoryCastPlotter(SpellCategory category) {
		super(category.getName());
		this.category = category;
	}
	
	@Override
	public int getValue() {
		return (int) category.getCastCount();
	}
}
