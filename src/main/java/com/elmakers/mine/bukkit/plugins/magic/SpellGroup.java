package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.List;

public class SpellGroup implements Comparable<SpellGroup>
{
	public String groupName;
	public List<Spell> spells = new ArrayList<Spell>();
	
	public int compareTo(SpellGroup other) 
	{
		return groupName.compareTo(other.groupName);
	}
}
