package com.elmakers.mine.bukkit.plugins.magic.command;

import java.util.ArrayList;
import java.util.List;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

public class SpellGroup implements Comparable<SpellGroup>
{
	public String groupName;
	public List<SpellTemplate> spells = new ArrayList<SpellTemplate>();

	public int compareTo(SpellGroup other) 
	{
		return groupName.compareTo(other.groupName);
	}
}
