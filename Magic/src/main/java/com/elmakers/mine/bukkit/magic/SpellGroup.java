package com.elmakers.mine.bukkit.magic;

import java.util.ArrayList;
import java.util.List;

import com.elmakers.mine.bukkit.magic.dao.SpellVariant;

public class SpellGroup implements Comparable<SpellGroup>
{
    public String             groupName;
    public List<SpellVariant> spells = new ArrayList<SpellVariant>();

    public int compareTo(SpellGroup other)
    {
        return groupName.compareTo(other.groupName);
    }
}
