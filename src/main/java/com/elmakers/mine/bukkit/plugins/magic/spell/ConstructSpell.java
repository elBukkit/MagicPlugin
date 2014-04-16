package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.block.ConstructBatch;
import com.elmakers.mine.bukkit.block.ConstructionType;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.TargetType;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ConstructSpell extends BrushSpell
{
	public final static String[] CONSTRUCT_PARAMETERS = {
		"radius", "undo", "falling", "speed", "max_dimension", "replace",
		"type", "thickness", "orient_dimension_max", "orient_dimension_min",
		"power"
	};
	
	private static final ConstructionType DEFAULT_CONSTRUCTION_TYPE = ConstructionType.SPHERE;
	private static final int DEFAULT_RADIUS						= 2;
	private static final int DEFAULT_MAX_DIMENSION 				= 16;
	
	private Block targetBlock = null;
	private boolean powered = false;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target t = getTarget();
		Block target = t.getBlock();
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}

		int timeToLive = parameters.getInt("undo", 0);
		int radius = parameters.getInt("radius", DEFAULT_RADIUS);
		radius = parameters.getInt("r", radius);
		radius = parameters.getInt("size", radius);
		boolean falling = parameters.getBoolean("falling", false);
		float force = 0;
		force = (float)parameters.getDouble("speed", force);
		Location orientTo = null;
		
		if (getTargetType() == TargetType.SELECT) {

			if (targetLocation2 != null) {
				this.targetBlock = targetLocation2.getBlock();
			}
			
			if (targetBlock == null) {
				targetBlock = target;
				activate();
				
				return SpellResult.TARGET_SELECTED;
			} else {
				radius = (int)targetBlock.getLocation().distance(target.getLocation());
				orientTo = target.getLocation();
				target = targetBlock;
			}
		} 

		int maxDimension = parameters.getInteger("max_dimension", DEFAULT_MAX_DIMENSION);
		maxDimension = parameters.getInteger("md", maxDimension);
		maxDimension = (int)(mage.getConstructionMultiplier() * (float)maxDimension);

		int diameter = radius * 2;
		if (diameter > maxDimension)
		{
			return SpellResult.FAIL;
		}
		
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		if (parameters.getBoolean("replace", false)) {
			Set<Material> destructible = new HashSet<Material>();
			Material targetMaterial = targetBlock != null ? targetBlock.getType() : target.getType();
			destructible.add(targetMaterial);
			
			// A bit hacky, but is very handy!
			if (targetMaterial == Material.STATIONARY_WATER)
			{
				destructible.add(Material.WATER);
			}
			else if (targetMaterial == Material.WATER)
			{
				destructible.add(Material.STATIONARY_WATER);
			}
			else if (targetMaterial == Material.STATIONARY_LAVA)
			{
				destructible.add(Material.LAVA);
			}
			else if (targetMaterial == Material.LAVA)
			{
				destructible.add(Material.STATIONARY_LAVA);
			}
			
			setDestructible(destructible);
		}
		
		// TODO : Is this needed? Or just use "ty"?
		if (parameters.containsKey("y_offset")) {
			target = target.getRelative(BlockFace.UP, parameters.getInt("y_offset", 0));
		}

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(target.getLocation());
		
		ConstructionType conType = DEFAULT_CONSTRUCTION_TYPE;

		int thickness = parameters.getInt("thickness", 0);
		
		String typeString = parameters.getString("type", "");

		ConstructionType testType = ConstructionType.parseString(typeString, ConstructionType.UNKNOWN);
		if (testType != ConstructionType.UNKNOWN)
		{
			conType = testType;
		}

		ConstructBatch batch = new ConstructBatch(this, target.getLocation(), conType, radius, thickness, falling, orientTo);
		
		// Check for command block overrides
		if (parameters.containsKey("commands"))
		{
			ConfigurationNode commandMap = parameters.getNode("commands");
			List<String> keys = commandMap.getKeys();
			for (String key : keys) {
				batch.addCommandMapping(key, commandMap.getString(key));
			}
		} 
		
		if (falling) {
			batch.setFallingBlockSpeed(force);
		}
		if (parameters.containsKey("orient_dimension_max")) {
			batch.setOrientDimensionMax(parameters.getInteger("orient_dimension_max", null));
		} else if (parameters.containsKey("odmax")) {
			batch.setOrientDimensionMax(parameters.getInteger("odmax", null));
		}

		if (parameters.containsKey("orient_dimension_min")) {
			batch.setOrientDimensionMin(parameters.getInteger("orient_dimension_min", null));
		} else if (parameters.containsKey("odmin")) {
			batch.setOrientDimensionMin(parameters.getInteger("odmin", null));
		}
		if (timeToLive > 0) {
			batch.setTimeToLive(timeToLive);
		}
		if (parameters.getBoolean("power", false)) {
			batch.setPower(true);
		}
		boolean success = mage.addPendingBlockBatch(batch);
		
		deactivate();

		return success ? SpellResult.CAST : SpellResult.FAIL;
	}
	
	
	@Override
	public void onDeactivate() {
		targetBlock = null;
	}

	@Override
	public boolean onCancel()
	{
		if (targetBlock != null)
		{
			deactivate();
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean hasBrushOverride() 
	{
		return powered || super.hasBrushOverride();
	}
	
	@Override
	protected void loadTemplate(ConfigurationNode node)
	{
		super.loadTemplate(node);
		powered = parameters.getBoolean("power", false);
	}

	@Override
	public void getParameters(Collection<String> parameters)
	{
		super.getParameters(parameters);
		parameters.addAll(Arrays.asList(CONSTRUCT_PARAMETERS));
	}
	
	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		super.getParameterOptions(examples, parameterKey);
	
		if (parameterKey.equals("undo")) {
			examples.addAll(Arrays.asList(EXAMPLE_DURATIONS));
		} else if (parameterKey.equals("radius") || parameterKey.equals("max_dimension") 
				|| parameterKey.equals("orient_dimension_max") || parameterKey.equals("orient_dimension_min")
				|| parameterKey.equals("thickness") || parameterKey.equals("speed")) {
			examples.addAll(Arrays.asList(EXAMPLE_SIZES));
		} else if (parameterKey.equals("type")) {
			ConstructionType[] constructionTypes = ConstructionType.values();
			for (ConstructionType constructionType : constructionTypes) {
				examples.add(constructionType.name().toLowerCase());
			}
		} else if (parameterKey.equals("power") || parameterKey.equals("replace") || parameterKey.equals("falling")) {
			examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
		}
	}
}
