package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.EffectTrail;
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
		Location l = block.getLocation();
		player.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), size, incendiary, breakBlocks);
		spells.updateBlock(block);
		return SpellResult.SUCCESS;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int size = parameters.getInt("size", defaultSize);
		boolean useFire = parameters.getBoolean("fire", false);
		boolean breakBlocks = parameters.getBoolean("break_blocks", true);
		boolean showEffect = parameters.getBoolean("show_effect", true);
		String targetType = (String)parameters.getString("target", "");
		
		size = (int)(playerSpells.getPowerMultiplier() * size);
		
		if (targetType.equals("here"))
		{
			player.damage(player.getMaxHealth() * 10);
			return createExplosionAt(player.getLocation(), size, useFire, breakBlocks);
		}

		Target target = getTarget();
		if (!target.hasTarget())
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		// Visual effect
		if (showEffect) {
			int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
			Location effectLocation = player.getEyeLocation();
			Vector effectDirection = effectLocation.getDirection();
			EffectTrail effect = new EffectTrail(spells.getPlugin(), effectLocation, effectDirection, effectRange);
			effect.setPeriod(effectPeriod);
			effect.setSpeed(effectSpeed);
			
	        FireworkEffect fireworkEffect = FireworkEffect.builder().flicker(true).
	        	withColor(Color.RED).withFade(Color.ORANGE).with(Type.BURST).build();
	        effect.setFireworkEffect(fireworkEffect, 1);
			effect.start();
		}

		return createExplosionAt(target.getLocation(), size, useFire, breakBlocks);
	}
}
