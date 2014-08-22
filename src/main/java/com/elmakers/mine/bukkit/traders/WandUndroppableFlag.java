package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Undroppable", key=".wand_undroppable", priority = 5)
public class WandUndroppableFlag extends NBTItemFlag {

	public WandUndroppableFlag(String key) {
		super(key, "wand", "undroppable");
	}
}
