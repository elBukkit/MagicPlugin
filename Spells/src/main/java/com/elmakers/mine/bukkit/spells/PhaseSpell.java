package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.magic.Spell;

public class PhaseSpell extends Spell
{
	public PhaseSpell(NetherManager nether)
	{
		this.nether = nether;
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
		if (nether == null)
		{
			return false;
		}
		
		String worldName = null;
		if (parameters.length > 0)
		{
			worldName = parameters[0];
		}
		return nether.go(player, worldName) != null;
	}

	@Override
	protected String getName()
	{
		return "phase";
	}

	@Override
	public String getCategory()
	{
		return "nether";
	}

	@Override
	public String getDescription()
	{
		return "Phase between worlds";
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLD_RECORD;
	}

	private NetherManager nether;
}
