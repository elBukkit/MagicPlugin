package com.elmakers.mine.bukkit.plugins.magic;

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
			
			Double dmxValue = parameters.getDouble("dmx", null);
			Double dmyValue = parameters.getDouble("dmy", null);
			Double dmzValue = parameters.getDouble("dmz", null);
			if (dmxValue != null || dmyValue != null || dmzValue != null) {
				Vector offset = new Vector( 
						dmxValue == null ? 0 : dmxValue, 
						dmyValue == null ? 0 : dmyValue, 
						dmzValue == null ? 0 : dmzValue);
				
				brush.clearCloneTarget();
				brush.setTargetOffset(offset);
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
	public String getMessage(String messageKey, String def) {
		String message = super.getMessage(messageKey, def);
		
		// TODO: Default material.. ?
		String materialName = "None";
		MaterialBrush useBrush = getMaterialBrush();
		if (useBrush != null) {
			materialName = useBrush.getName();
		}
		
		return message.replace("$material", materialName);
	}
}
