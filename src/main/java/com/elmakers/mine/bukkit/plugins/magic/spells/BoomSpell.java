package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.effects.EffectTrail;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BoomSpell extends Spell {

	protected int defaultSize = 1;

    final static int effectSpeed = 6;
    final static int effectPeriod = 1;
    final static int maxEffectRange = 32;

	public SpellResult createExplosionAt(Location target, float size, boolean incendiary, boolean breakBlocks)
	{
		if (target == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		Block block = target.getBlock();
		if (!hasBuildPermission(block)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		Location l = block.getLocation();
		getPlayer().getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), size, incendiary, breakBlocks);
		controller.updateBlock(block);
		return SpellResult.SUCCESS;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int size = parameters.getInt("size", defaultSize);
		boolean useFire = parameters.getBoolean("fire", false);
		boolean breakBlocks = parameters.getBoolean("break_blocks", true);
		boolean showEffect = parameters.getBoolean("show_effect", true);
		
		size = (int)(mage.getRadiusMultiplier() * size);

		Target target = getTarget();
		if (!target.hasTarget())
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		// Visual effect
		if (showEffect) {
			int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
			Location effectLocation = getPlayer().getEyeLocation();
			Vector effectDirection = effectLocation.getDirection();
			EffectTrail effect = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
			effect.setPeriod(effectPeriod);
			effect.setSpeed(effectSpeed);
			
	        FireworkEffect fireworkEffect = mage.getFireworkEffect(Color.RED, Color.ORANGE, Type.BURST);
	        effect.setFireworkEffect(fireworkEffect, 1);
			effect.start();
		}

		return createExplosionAt(target.getLocation(), size, useFire, breakBlocks);
	}
}
