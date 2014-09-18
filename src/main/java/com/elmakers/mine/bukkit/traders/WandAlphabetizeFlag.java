package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Auto-Alphabetize", key=".wand_alphabetize", priority = 5)
public class WandAlphabetizeFlag extends NBTItemFlag {

	public WandAlphabetizeFlag(String key) {
		super(key, "wand", "alphabetize");
	}
}
