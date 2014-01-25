package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public abstract class BrushSpell extends BlockSpell{

	private MaterialBrush brush;

	@Override
	protected void loadTemplate(String key, ConfigurationNode node)
	{
		super.loadTemplate(key, node);
		Material override = parameters.getMaterial("material", null);
		if (override != null) {
			brush = new MaterialBrush(controller, override, (byte)0);
		}
	}
	
	public MaterialBrush getMaterialBrush()
	{
		if (brush != null)
		{
			return brush;
		}
		
		return mage.getBrush();
	}
	
	public boolean hasBrushOverride() 
	{
		return brush != null;
	}
}
