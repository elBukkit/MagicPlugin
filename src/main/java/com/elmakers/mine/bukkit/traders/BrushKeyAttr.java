package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Magic Material Brush", key="magic_brush", priority = 5)
public class BrushKeyAttr extends NBTItemAttr {
	
	public BrushKeyAttr(String key) {
		super(key, "brush", "key");
	}
}
