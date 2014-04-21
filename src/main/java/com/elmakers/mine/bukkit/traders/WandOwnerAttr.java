package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Owner", key="wand_owner", priority = 5)
public class WandOwnerAttr extends NBTItemAttr {
	
	public WandOwnerAttr(String key) {
		super(key, "wand", "owner");
	}
}
