package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Rename Flag", key=".wand_rename", priority = 5)
public class WandRenameFlag extends NBTItemFlag {

	public WandRenameFlag(String key) {
		super(key, "wand", "rename");
	}
}
