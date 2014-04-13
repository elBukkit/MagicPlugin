package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public abstract class BrushSpell extends BlockSpell{

	private MaterialBrush brush;
	private boolean hasBrush = false;

	@Override
	protected void processParameters(ConfigurationNode parameters)
	{
		super.processParameters(parameters);
		String materialKey = parameters.getString("material", null);
		materialKey = parameters.getString("m", materialKey);
		if (materialKey != null) {
			brush = new MaterialBrush(mage, getLocation(), materialKey);
			
			if (parameters.containsKey("mm")) {
				brush.update(parameters.getString("mm"));
				brush.update(materialKey);
			}
			
			Double dmxValue = parameters.getDouble("omx", null);
			Double dmyValue = parameters.getDouble("omy", null);
			Double dmzValue = parameters.getDouble("omz", null);
			String dmWorldValue = parameters.getString("omworld", null);
			World targetWorld = null;
			if (dmWorldValue != null && dmWorldValue.length() > 0) {
				targetWorld = Bukkit.getWorld(dmWorldValue);
			}
			if (dmxValue != null || dmyValue != null || dmzValue != null || targetWorld != null) {
				Vector offset = new Vector( 
						dmxValue == null ? 0 : dmxValue, 
						dmyValue == null ? 0 : dmyValue, 
						dmzValue == null ? 0 : dmzValue);
				
				brush.clearCloneTarget();
				brush.setTargetOffset(offset, targetWorld);
			}
			
			if (parameters.getBoolean("mtarget", false)) {
				brush.clearCloneLocation();
			}
		} else {
			brush = null;
		}
	}
	
	@Override
	protected void loadTemplate(ConfigurationNode node)
	{
		super.loadTemplate(node);
		hasBrush = parameters.containsKey("material");
	}
	
	public MaterialBrush getMaterialBrush()
	{
		if (brush != null)
		{
			return brush;
		}
		
		return mage.getBrush();
	}
	
	@Override
	public MaterialAndData getEffectMaterial()
	{
		return brush != null ? brush : mage.getBrush();
	}
	
	public boolean hasBrushOverride() 
	{
		return brush != null || hasBrush;
	}
	
	@Override
	protected MaterialAndData getDisplayMaterial()
	{
		MaterialBrush useBrush = getMaterialBrush();
		if (useBrush != null) {
			return useBrush;
		}
		
		return super.getDisplayMaterial();
	}
}
