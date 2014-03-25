package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Auto-Organize", key="wand_organize", priority = 5)
public class WandOrganizeFlag extends NBTItemFlag {
	
	public WandOrganizeFlag(String key) {
		super(key, "wand", "organize");
	}
}
