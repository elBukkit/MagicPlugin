package com.elmakers.mine.bukkit.block;

public enum ConstructionType
{
    SPHERE,
    CUBOID,
    PYRAMID,
    CYLINDER,
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
}
