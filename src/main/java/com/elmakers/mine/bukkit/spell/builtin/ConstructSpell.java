package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.block.ConstructionType;
import com.elmakers.mine.bukkit.block.batch.ConstructBatch;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ConstructSpell extends BrushSpell
{
	public final static String[] CONSTRUCT_PARAMETERS = {
		"radius", "falling", "speed", "max_dimension", "replace",
		"type", "thickness", "orient_dimension_max", "orient_dimension_min",
		"power"
	};
	
	private static final ConstructionType DEFAULT_CONSTRUCTION_TYPE = ConstructionType.SPHERE;
	private static final int DEFAULT_RADIUS						= 2;
	private static final int DEFAULT_MAX_DIMENSION 				= 16;
	
	private Block targetBlock = null;
	private boolean powered = false;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target t = getTarget();
		Block target = t.getBlock();
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}

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

		int maxDimension = parameters.getInt("max_dimension", DEFAULT_MAX_DIMENSION);
		maxDimension = parameters.getInt("md", maxDimension);
		maxDimension = (int)(mage.getConstructionMultiplier() * (float)maxDimension);

		int diameter = radius * 2;
		if (diameter > maxDimension)
		{
			return SpellResult.FAIL;
		}
		
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		// TODO : Is this needed? Or just use "ty"?
		if (parameters.contains("y_offset")) {
			target = target.getRelative(BlockFace.UP, parameters.getInt("y_offset", 0));
		}

		MaterialBrush buildWith = getBrush();
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
		
		if (parameters.getBoolean("replace", false)) {
			List<Material> replaceMaterials = new ArrayList<Material>();
			Material targetMaterial = targetBlock != null ? targetBlock.getType() : target.getType();
			replaceMaterials.add(targetMaterial);
			
			// A bit hacky, but is very handy!
			if (targetMaterial == Material.STATIONARY_WATER)
			{
				replaceMaterials.add(Material.WATER);
			}
			else if (targetMaterial == Material.WATER)
			{
				replaceMaterials.add(Material.STATIONARY_WATER);
			}
			else if (targetMaterial == Material.STATIONARY_LAVA)
			{
				replaceMaterials.add(Material.LAVA);
			}
			else if (targetMaterial == Material.LAVA)
			{
				replaceMaterials.add(Material.STATIONARY_LAVA);
			}
			
			batch.setReplace(replaceMaterials);
		}
		
		// Check for command block overrides
		if (parameters.contains("commands"))
		{
			ConfigurationSection commandMap = parameters.getConfigurationSection("commands");
			Set<String> keys = commandMap.getKeys(false);
			for (String key : keys) {
				batch.addCommandMapping(key, commandMap.getString(key));
			}
		}

        // Check for sign overrides
        if (parameters.contains("signs"))
        {
            ConfigurationSection signMap = parameters.getConfigurationSection("signs");
            Set<String> keys = signMap.getKeys(false);
            for (String key : keys) {
                String text = signMap.getString(key);
                Player targetPlayer = null;
                if (text.equals("$target")) {
                    Entity targetEntity = t.getEntity();
                    if (targetEntity != null && targetEntity instanceof Player) {
                        targetPlayer = (Player)targetEntity;
                    }
                } else if (text.equals("$name")) {
                    targetPlayer = mage.getPlayer();
                }
                if (targetPlayer != null) {
                    text = targetPlayer.getName();
                }
                batch.addSignMapping(key, text);
            }
        }

        if (falling) {
			batch.setFallingBlockSpeed(force);
		}
		if (parameters.contains("orient_dimension_max")) {
			batch.setOrientDimensionMax(parameters.getInt("orient_dimension_max"));
		} else if (parameters.contains("odmax")) {
			batch.setOrientDimensionMax(parameters.getInt("odmax"));
		}

		if (parameters.contains("orient_dimension_min")) {
			batch.setOrientDimensionMin(parameters.getInt("orient_dimension_min"));
		} else if (parameters.contains("odmin")) {
			batch.setOrientDimensionMin(parameters.getInt("odmin"));
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
	protected void loadTemplate(ConfigurationSection node)
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
	
		if (parameterKey.equals("radius") || parameterKey.equals("max_dimension") 
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
