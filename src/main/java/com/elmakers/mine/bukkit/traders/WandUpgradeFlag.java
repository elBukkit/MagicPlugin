package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name = "Wand", key = ".wand_upgrade")
public class WandUpgradeFlag extends NBTItemFlag {

	public WandUpgradeFlag(String key)
	{
		super(key, "wand_upgrade", null);
	}
}
