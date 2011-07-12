package com.elmakers.mine.bukkit.plugins.magic;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class PlayerSpells 
{
    protected Player player;
	protected HashMap<String, Spell> spells = new HashMap<String, Spell>();
	
	public PlayerSpells(Player player)
	{
	    this.player = player;
	}
	
	public Spell getSpell(String name)
	{
	    return spells.get(name);
	}
	
	protected void addSpell(Spell spell)
	{
	    spells.put(spell.getName(), spell);
	}
	
	public void cancel()
	{
	    for (Spell spell : spells.values())
        {
            spell.cancel();
        }
	}
}
