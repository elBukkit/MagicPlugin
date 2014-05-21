package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Path", key="wand_Path", priority = 5)
public class WandPathAttr extends NBTItemAttr {

	public WandPathAttr(String key) {
		super(key, "wand", "path");
	}
}
