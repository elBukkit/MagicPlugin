package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.persistence.dao.PlayerData;

public class SpellVariant implements Comparable<SpellVariant>
{
	private String name;
	private String description;
	private String category;
	private String[] parameters;
	private Spell spell;
	private Material material;
	
	public SpellVariant(Spell spell)
	{
		this.spell = spell;
		this.name = spell.getName();
		this.category = spell.getCategory();
		this.description = spell.getDescription();
		this.material = spell.getMaterial();
		this.parameters = new String[0];
	}
	
	public SpellVariant(Spell spell, String name, Material material, String category, String description, String[] parameters)
	{
		this.spell = spell;
		this.name = name;
		this.category = category;
		this.description = description;
		this.parameters = parameters;
		this.material = material;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getCategory()
	{
		return category;
	}
	
	public String[] getParameters()
	{
		return parameters;
	}
	
	public boolean isMatch(String spell, String[] params)
	{
		if (params == null) params = new String[0];
		return (name.equalsIgnoreCase(spell) && parameters.equals(params));
	}
	
	public Spell getSpell()
	{
		return spell;
	}
	
	public int compareTo(SpellVariant other)
	{
		return getName().compareTo(other.getName());
	}
	
	public Material getMaterial()
	{
		return material;
	}
	
	public boolean cast(String[] extraParameters, Player player)
	{
    	String[] spellParameters = parameters;
    	
    	if (extraParameters.length > 0)
    	{
    		spellParameters = new String[extraParameters.length + parameters.length];
	    	for (int i = 0; i < parameters.length; i++)
	    	{
	    		spellParameters[i] = parameters[i];
	    	}
	    	for (int i = 0; i < extraParameters.length; i++)
	    	{
	    		spellParameters[i + parameters.length] = extraParameters[i];
	    	}
    	}
 
		return spell.cast(spellParameters, player);
	}
	
	public String getPermissionNode()
	{
		return "Spells.cast." + getName();
	}
	
	public boolean hasSpellPermission(Player player)
	{
		if (player == null) return false;
		PlayerData playerData = spell.getPersistence().get(player.getName(), PlayerData.class);
		if (playerData == null) return false;
		return playerData.isSet(getPermissionNode());
	}
}