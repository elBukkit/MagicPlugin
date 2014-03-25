package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Cost Reduction", key="wand_cost_reduction", priority = 5)
public class WandCostReductionAttr extends NBTItemAttr {
	
	public WandCostReductionAttr(String key) {
		super(key, "wand", "cost_reduction");
	}
}
