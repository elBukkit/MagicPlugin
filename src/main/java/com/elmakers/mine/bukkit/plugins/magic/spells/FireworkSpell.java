package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.EffectUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FireworkSpell extends Spell
{	
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
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
		if (parameters.containsKey("color1")) {
			color1 = getColor(parameters.getString("color1"));
		} else if (playerSpells.getEffectColor() != null) {
			color1 = playerSpells.getEffectColor();
		}
		if (parameters.containsKey("color2")) {
			color2 = getColor(parameters.getString("color2"));
		}
		if (parameters.containsKey("type")) {
			fireworkType = getType(parameters.getString("type"));
		}
		flicker = parameters.getBoolean("flicker", flicker);
		trail = parameters.getBoolean("trail", trail);
		
		int flareCount = parameters.getInt("count", 1);
		Block target = getTarget().getBlock();
	     
        FireworkEffect effect = playerSpells.getFireworkEffect(color1, color2, fireworkType, flicker, trail);
        
		for (int i = 0; i < flareCount; i++)
		{
			// TODO: Spread locations
			EffectUtils.spawnFireworkEffect(target.getLocation(), effect, power);
			//spawnFirework(target.getLocation(), flareCount);
		}

		castMessage("You fire some magical flares");

		return SpellResult.SUCCESS;
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
