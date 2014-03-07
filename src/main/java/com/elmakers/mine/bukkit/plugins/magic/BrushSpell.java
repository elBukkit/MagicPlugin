package com.elmakers.mine.bukkit.plugins.magic;

import com.elmakers.mine.bukkit.blocks.MaterialBrush;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public abstract class BrushSpell extends BlockSpell{

	private MaterialBrush brush;

	@Override
	protected void processParameters(ConfigurationNode parameters)
	{
		super.processParameters(parameters);
		String materialKey = parameters.getString("material", null);
		if (materialKey != null) {
			brush = new MaterialBrush(controller, getLocation(), materialKey);
		} else {
			brush = null;
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
