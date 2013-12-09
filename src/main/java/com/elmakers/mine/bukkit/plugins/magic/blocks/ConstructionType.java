package com.elmakers.mine.bukkit.plugins.magic.blocks;

public enum ConstructionType
{
	SPHERE,
	CUBOID,
	PYRAMID,
	UNKNOWN;

	public static ConstructionType parseString(String s, ConstructionType defaultType)
	{
		ConstructionType construct = defaultType;
		for (ConstructionType t : ConstructionType.values())
		{
			if (t.name().equalsIgnoreCase(s))
			{
				construct = t;
			}
		}
		return construct;
	}
};