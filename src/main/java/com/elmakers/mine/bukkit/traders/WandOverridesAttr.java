package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Cast Overrides", key="wand_overrides", priority = 5)
public class WandOverridesAttr extends NBTItemAttr {

	public WandOverridesAttr(String key) {
		super(key, "wand", "overrides");
	}
}
