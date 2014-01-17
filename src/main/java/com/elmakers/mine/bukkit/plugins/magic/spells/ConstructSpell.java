package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.blocks.ConstructBatch;
import com.elmakers.mine.bukkit.plugins.magic.blocks.ConstructionType;
import com.elmakers.mine.bukkit.utilities.EffectUtils;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ConstructSpell extends Spell
{
	private ConstructionType defaultConstructionType = ConstructionType.SPHERE;
	private int				defaultRadius			= 2;
	private Block targetBlock 						= null;
	
	private static final int DEFAULT_MAX_DIMENSION = 128;

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		targetThrough(Material.GLASS);
		Block target = getTarget().getBlock();

		if (target == null)
		{
			initializeTargeting(getPlayer());
			noTargetThrough(Material.GLASS);
			target = getTarget().getBlock();
		}

		if (target == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		int timeToLive = parameters.getInt("undo", 0);
		Set<Material> indestructible = parameters.getMaterials("indestructible", "");
		int radius = parameters.getInt("radius", defaultRadius);
		radius = parameters.getInt("size", radius);
		boolean falling = parameters.getBoolean("falling", false);
		float force = 0;
		force = (float)parameters.getDouble("speed", force);
		
		String targetString = parameters.getString("target", "");
		if (targetString.equals("select")) {
			if (targetBlock == null) {
				targetBlock = target;
				Location effectLocation = targetBlock.getLocation();
				effectLocation.add(0.5f, 0.5f, 0.5f);
				EffectUtils.playEffect(effectLocation, ParticleType.HAPPY_VILLAGER, 0.3f, 0.3f, 0.3f, 1.5f, 10);
				castMessage("Cast again to construct");
				activate();
				return SpellResult.COST_FREE;
			} else {
				radius = (int)targetBlock.getLocation().distance(target.getLocation());
				target = targetBlock;
			}
		} else {
			radius = (int)(mage.getRadiusMultiplier() * (float)radius);			
		}

		int maxDimension = (int)(mage.getConstructionMultiplier() * (float)parameters.getInteger("max_dimension", DEFAULT_MAX_DIMENSION));

		int diameter = radius * 2;
		if (diameter > maxDimension)
		{
			sendMessage("Dimension is too big!");
			return SpellResult.FAILURE;
		}
		
		if (parameters.containsKey("y_offset")) {
			target = target.getRelative(BlockFace.UP, parameters.getInt("y_offset", 0));
		}
		
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		Material material = target.getType();
		byte data = target.getData();

		ItemStack buildWith = getBuildingMaterial();
		if (buildWith != null)
		{
			material = buildWith.getType();
			data = getItemData(buildWith);
		}

		ConstructionType conType = defaultConstructionType;

		boolean hollow = false;
		String fillType = (String)parameters.getString("fill", "");
		hollow = fillType.equals("hollow");

		Material materialOverride = parameters.getMaterial("material");
		if (materialOverride != null)
		{
			material = materialOverride;
			data = 0;
		}
		
		Vector forceVector = null;
		if (falling)
		{
			material = Material.AIR;
			data = 0;
			
			if (force != 0) {
				forceVector = getPlayer().getLocation().getDirection();
				forceVector.setY(-forceVector.getY()).normalize().multiply(force);
			}
		}
		String typeString = parameters.getString("type", "");

		ConstructionType testType = ConstructionType.parseString(typeString, ConstructionType.UNKNOWN);
		if (testType != ConstructionType.UNKNOWN)
		{
			conType = testType;
		}

		fillArea(target, radius, material, data, !hollow, conType, timeToLive, indestructible, falling, forceVector);
		deactivate();

		return SpellResult.SUCCESS;
	}

	public void fillArea(Block target, int radius, Material material, byte data, boolean fill, ConstructionType type, int timeToLive, Set<Material> indestructible, boolean falling, Vector forceVector)
	{
		ConstructBatch batch = new ConstructBatch(this, target.getLocation(), type, radius, fill, material, data, indestructible, falling);
		if (forceVector != null) {
			batch.setFallingBlockVelocity(forceVector);
		}
		if (timeToLive > 0) {
			batch.setTimeToLive(timeToLive);
		}
		controller.addPendingBlockBatch(batch);
	}
	
	@Override
	public boolean usesMaterial() {
		return true;
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
			sendMessage("Cancelled construct");
			targetBlock = null;
			return true;
		}
		
		return false;
	}
}
