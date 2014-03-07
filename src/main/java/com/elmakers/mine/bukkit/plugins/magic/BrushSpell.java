package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.Location;

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
			
			Double dmxValue = parameters.getDouble("dmx", null);
			Double dmyValue = parameters.getDouble("dmy", null);
			Double dmzValue = parameters.getDouble("dmz", null);
			if (dmxValue != null || dmyValue != null || dmzValue != null) {
				Location location = getLocation();
				location = new Location(location.getWorld(), 
						location.getX() + (dmxValue == null ? 0 : dmxValue), 
						location.getY() + (dmyValue == null ? 0 : dmyValue), 
						location.getZ() + (dmzValue == null ? 0 : dmzValue),
						location.getYaw(), location.getPitch());
				
				brush.clearCloneTarget();
				brush.setTarget(location);
			} 
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
