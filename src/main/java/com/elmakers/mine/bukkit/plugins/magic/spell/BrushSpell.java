package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class BrushSpell extends BlockSpell {

	private MaterialBrush brush;
	private boolean hasBrush = false;
	
	public final static String[] BRUSH_PARAMETERS = {
		"brushmod", "brush", "obx", "oby", "obz", "obworld", "btarget"
	};
	
	@Override
	protected void processParameters(ConfigurationSection parameters)
	{
		super.processParameters(parameters);
		
		String materialKey = parameters.getString("brush", null);

		if (materialKey != null) {
			brush = new MaterialBrush(mage, getLocation(), materialKey);
			
			if (parameters.contains("brushmod")) {
				brush.update(parameters.getString("brushmod"));
				brush.update(materialKey);
			}
			
			Double dmxValue = ConfigurationUtils.getDouble(parameters, "obx", null);
			Double dmyValue = ConfigurationUtils.getDouble(parameters, "oby", null);
			Double dmzValue = ConfigurationUtils.getDouble(parameters, "obz", null);
			String targetWorldName = parameters.getString("obworld", null);
			if (dmxValue != null || dmyValue != null || dmzValue != null || targetWorldName != null) {
				Vector offset = new Vector( 
						dmxValue == null ? 0 : dmxValue, 
						dmyValue == null ? 0 : dmyValue, 
						dmzValue == null ? 0 : dmzValue);
				
				brush.clearCloneTarget();
				brush.setTargetOffset(offset, targetWorldName);
			}
			
			if (parameters.getBoolean("brushtarget", false)) {
				brush.clearCloneLocation();
			}
		} else {
			brush = null;
		}
	}
	
	@Override
	protected void loadTemplate(ConfigurationSection node)
	{
		super.loadTemplate(node);
		hasBrush = parameters.contains("brush");
	}
	
	public com.elmakers.mine.bukkit.api.block.MaterialBrush getMaterialBrush()
	{
		if (brush != null)
		{
			return brush;
		}
		
		return mage.getBrush();
	}
	
	@Override
	public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
	{
		return brush != null ? brush : mage.getBrush();
	}
	
	public boolean hasBrushOverride() 
	{
		return brush != null || hasBrush;
	}
	
	@Override
	protected String getDisplayMaterialName()
	{
		com.elmakers.mine.bukkit.api.block.MaterialBrush useBrush = getMaterialBrush();
		if (useBrush != null) {
			return useBrush.getName();
		}
		
		return super.getDisplayMaterialName();
	}

	@Override
	public void getParameters(Collection<String> parameters)
	{
		super.getParameters(parameters);
		parameters.addAll(Arrays.asList(BRUSH_PARAMETERS));
	}
	
	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		super.getParameterOptions(examples, parameterKey);
		
		if (parameterKey.equals("bmod") || parameterKey.equals("brush")) {
			examples.addAll(controller.getMaterials());
		} else if (parameterKey.equals("btarget")) {
			examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
		} else if (parameterKey.equals("obx") || parameterKey.equals("oby") || parameterKey.equals("obz")) {
			examples.addAll(Arrays.asList(EXAMPLE_VECTOR_COMPONENTS));
		} else if (parameterKey.equals("obworld")) {
			List<World> worlds = Bukkit.getWorlds();
			for (World world : worlds) {
				examples.add(world.getName());
			}
		}
	}
}
