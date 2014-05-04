package com.elmakers.mine.bukkit.metrics;

import java.util.Collection;

import org.mcstats.Metrics.Plotter;

import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.spell.SpellCategory;

public class CategoryCastPlotter extends Plotter {
	private final SpellCategory category;
	
	public CategoryCastPlotter(SpellCategory category) {
		super(category.getName());
		this.category = category;
	}
	
	@Override
	public int getValue() {
		long castCount = 0;
		try {
			Collection<SpellTemplate> spells = category.getSpells();
			for (SpellTemplate spell : spells) {
				if (spell instanceof MageSpell) {
					castCount += ((MageSpell)spell).getCastCount();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return (int)castCount;
	}
}
