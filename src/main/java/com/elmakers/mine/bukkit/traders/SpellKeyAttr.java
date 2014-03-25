package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Magic Spell", key="magic_spell", priority = 5)
public class SpellKeyAttr extends NBTItemAttr {
	
	public SpellKeyAttr(String key) {
		super(key, "spell", "key");
	}
}
