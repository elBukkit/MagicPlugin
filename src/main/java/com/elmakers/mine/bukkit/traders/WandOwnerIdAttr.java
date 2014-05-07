package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Owner Id", key="wand_owner_id", priority = 5)
public class WandOwnerIdAttr extends NBTItemAttr {

	public WandOwnerIdAttr(String key) {
		super(key, "wand", "owner_id");
	}
}
