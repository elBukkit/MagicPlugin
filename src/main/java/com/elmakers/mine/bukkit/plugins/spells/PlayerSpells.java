package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;

public class PlayerSpells 
{
	private Material material = Material.AIR;
	private byte data = 0;
	private boolean usingMaterial;
	
	public byte getData()
	{
		return data;
	}
	
	public Material getMaterial()
	{
		return material;
	}
	
	public boolean isUsingMaterial()
	{
		return usingMaterial;
	}
	
	public void startMaterialUse(Material mat, byte data)
	{
		setMaterial(mat);
		setData(data);
		usingMaterial = true;
	}

	public Material finishMaterialUse()
	{
		usingMaterial = false;
		return material;
	}
	
	public void setData(byte d)
	{
		data = d;
	}
	
	public void setMaterial(Material mat)
	{
		material = mat;
	}
	
}
