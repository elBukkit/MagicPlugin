package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.magic.Spell;

public class HealSpell extends Spell 
{
	@Override
	public boolean onCast(String[] parameters) 
	{
		castMessage(player, "You heal yourself");
		player.setHealth(20);
		return true;
	}

	@Override
	public String getName() 
	{
		return "heal";
	}

	@Override
	public String getDescription() 
	{
		return "Heal yourself";
	}

	@Override
	public String getCategory() 
	{
		return "help";
	}

	@Override
	public Material getMaterial()
	{
		return Material.BREAD;
	}
}
