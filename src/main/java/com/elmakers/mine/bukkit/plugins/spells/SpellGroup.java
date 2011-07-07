package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.List;

public class SpellGroup implements Comparable<SpellGroup>
{
	public String groupName;
	public List<SpellVariant> spells = new ArrayList<SpellVariant>();
	
	public int compareTo(SpellGroup other) 
	{
		return groupName.compareTo(other.groupName);
	}
}
