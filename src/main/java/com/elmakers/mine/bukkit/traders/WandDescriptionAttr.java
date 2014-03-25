package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Description", key="wand_description", priority = 5)
public class WandDescriptionAttr extends NBTItemAttr {
	
	public WandDescriptionAttr(String key) {
		super(key, "wand", "description");
	}
}
