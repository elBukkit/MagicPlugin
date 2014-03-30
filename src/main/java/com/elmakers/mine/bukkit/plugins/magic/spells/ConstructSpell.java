package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.ConstructBatch;
import com.elmakers.mine.bukkit.blocks.ConstructionType;
import com.elmakers.mine.bukkit.blocks.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.TargetType;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ConstructSpell extends BrushSpell
{
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
		
		if (parameters.containsKey("y_offset")) {
			target = target.getRelative(BlockFace.UP, parameters.getInt("y_offset", 0));
		}

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(target.getLocation());
		
		ConstructionType conType = DEFAULT_CONSTRUCTION_TYPE;

		boolean hollow = false;
		String fillType = (String)parameters.getString("fill", "");
		hollow = fillType.equals("hollow");
		
		Vector forceVector = null;
		if (falling)
		{
			if (force != 0) {
				forceVector = getLocation().getDirection();
				forceVector.setY(-forceVector.getY()).normalize().multiply(force);
			}
		}
		String typeString = parameters.getString("type", "");

		ConstructionType testType = ConstructionType.parseString(typeString, ConstructionType.UNKNOWN);
		if (testType != ConstructionType.UNKNOWN)
		{
			conType = testType;
		}

		ConstructBatch batch = new ConstructBatch(this, target.getLocation(), conType, radius, !hollow, falling, orientTo);
		if (forceVector != null) {
			batch.setFallingBlockVelocity(forceVector);
		}
		if (parameters.containsKey("orient_dimension_max")) {
			batch.setOrientDimensionMax(parameters.getInteger("orient_dimension_max", null));
		} else if (parameters.containsKey("odmax")) {
			batch.setOrientDimensionMax(parameters.getInteger("odmax", null));
		}

		if (parameters.containsKey("orient_dimension_min")) {
			batch.setOrientDimensionMin(parameters.getInteger("orient_dimension_min", null));
		} else if (parameters.containsKey("odmin")) {
			batch.setOrientDimensionMax(parameters.getInteger("odmin", null));
		}
		if (timeToLive > 0) {
			batch.setTimeToLive(timeToLive);
		}
		if (parameters.getBoolean("power", false)) {
			batch.setPower(true);
		}
		mage.addPendingBlockBatch(batch);
		
		deactivate();

		return SpellResult.CAST;
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
}
