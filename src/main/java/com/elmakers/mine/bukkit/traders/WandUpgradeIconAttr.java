package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Update Icon", key="wand_upgrade_icon", priority = 5)
public class WandUpgradeIconAttr extends NBTItemAttr {

	public WandUpgradeIconAttr(String key) {
		super(key, "wand", "upgrade_icon");
	}
}
