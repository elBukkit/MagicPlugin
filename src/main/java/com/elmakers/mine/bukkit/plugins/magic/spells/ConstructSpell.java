package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.ConstructBatch;
import com.elmakers.mine.bukkit.blocks.ConstructionType;
import com.elmakers.mine.bukkit.effects.EffectUtils;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.effects.SpellEffect;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ConstructSpell extends BrushSpell
{
	private ConstructionType defaultConstructionType = ConstructionType.SPHERE;
	private int				defaultRadius			= 2;
	private Block targetBlock 						= null;
	
	private static final int DEFAULT_MAX_DIMENSION = 128;

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Block target = getTarget().getBlock();

		if (target == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		int timeToLive = parameters.getInt("undo", 0);
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
				
				SpellEffect effect = getEffect("target");
				// Hacked until config-driven.
				effect.particleType = ParticleType.WATER_DRIPPING;
				effect.startTrailEffect(mage, getEyeLocation(), effectLocation);
				
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
		
		ConstructionType conType = defaultConstructionType;

		boolean hollow = false;
		String fillType = (String)parameters.getString("fill", "");
		hollow = fillType.equals("hollow");
		
		Vector forceVector = null;
		if (falling)
		{
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
					
		ConstructBatch batch = new ConstructBatch(this, target.getLocation(), conType, radius, !hollow, falling);
		if (forceVector != null) {
			batch.setFallingBlockVelocity(forceVector);
		}
		if (parameters.containsKey("y_max")) {
			batch.setYMax(parameters.getInteger("y_max", null));
		}

		if (parameters.containsKey("y_min")) {
			batch.setYMin(parameters.getInteger("y_min", null));
		}
		if (timeToLive > 0) {
			batch.setTimeToLive(timeToLive);
		}
		mage.addPendingBlockBatch(batch);

		SpellEffect effect = getEffect("cast");
		// Hacked until config-driven.
		effect.particleType = null;
		effect.effect = Effect.STEP_SOUND;
		effect.data = buildWith.getMaterial().getId();
		effect.startTrailEffect(mage, getEyeLocation(), target.getLocation());
		if (this.targetBlock != null) {
			effect.startTrailEffect(mage, getEyeLocation(), this.targetBlock.getLocation());
		}
		
		deactivate();

		return SpellResult.SUCCESS;
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
			deactivate();
			return true;
		}
		
		return false;
	}
}
