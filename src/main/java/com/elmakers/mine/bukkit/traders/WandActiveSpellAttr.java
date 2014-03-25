package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Active Spell", key="wand_active_spell", priority = 5)
public class WandActiveSpellAttr extends NBTItemAttr {
	
	public WandActiveSpellAttr(String key) {
		super(key, "wand", "active_spell");
	}
}
