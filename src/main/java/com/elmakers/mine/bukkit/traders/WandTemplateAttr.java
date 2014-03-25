package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Template", key="wand_template", priority = 5)
public class WandTemplateAttr extends NBTItemAttr {
	
	public WandTemplateAttr(String key) {
		super(key, "wand", "template");
	}
}
