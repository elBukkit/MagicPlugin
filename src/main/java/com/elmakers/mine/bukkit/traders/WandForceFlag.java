package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Force Upgrade", key=".wand_force", priority = 5)
public class WandForceFlag extends NBTItemFlag {

	public WandForceFlag(String key) {
		super(key, "wand", "force");
	}
}
