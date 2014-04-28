package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.EffectUtils;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;

public class FireworkSpell extends TargetingSpell
{	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Random rand = new Random();
		int power = rand.nextInt(2) + 1;
		
		Color color1 = null;
		Color color2 = null;
		Type fireworkType = null;
		Boolean flicker = null;
		Boolean trail = null;
		
		// Configuration overrides
		power = parameters.getInt("size", power);
		if (parameters.contains("color1")) {
			color1 = getColor(parameters.getString("color1"));
		} else if (mage.getEffectColor() != null) {
			color1 = mage.getEffectColor();
		}
		if (parameters.contains("color2")) {
			color2 = getColor(parameters.getString("color2"));
		}
		if (parameters.contains("type")) {
			fireworkType = getType(parameters.getString("type"));
		}
		flicker = parameters.getBoolean("flicker");
		trail = parameters.getBoolean("trail");
		
		int flareCount = parameters.getInt("count", 1);
		Block target = getTarget().getBlock();
	     
        FireworkEffect effect = getFireworkEffect(color1, color2, fireworkType, flicker, trail);
        
		for (int i = 0; i < flareCount; i++)
		{
			// TODO: Spread locations
			EffectUtils.spawnFireworkEffect(target.getLocation(), effect, power);
			//spawnFirework(target.getLocation(), flareCount);
		}

		return SpellResult.CAST;
	}
	
	protected Color getColor(String name) {
		try {
			Field colorConstant = Color.class.getField(name.toUpperCase());
			return (Color)colorConstant.get(null);
		} catch (Exception ex) {
		}
		
		return Color.WHITE;
	}
	
	protected Type getType(String name) {
		for (Type t : Type.values()) {
			if (t.name().equalsIgnoreCase(name)) {
				return t;
			}
		}
		
		return Type.BALL;
	}
}
