package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.EffectUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Random;

public class FireworkAction extends BaseSpellAction
{
    private int power;
    private Color color1 = null;
    private Color color2 = null;
    private Type fireworkType = null;
    private Boolean flicker = null;
    private Boolean trail = null;
    private boolean launch = false;
    private int startDistance;
    private double speed;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {

        Random rand = context.getRandom();
        power = rand.nextInt(2) + 1;
        Mage mage = context.getMage();

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

        launch = parameters.getBoolean("launch", false);
        startDistance = parameters.getInt("start", 0);
        speed = parameters.getDouble("speed", 0.1);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Location location = context.getEyeLocation();
        Vector direction = null;
        if (launch) {
            direction = context.getDirection().normalize();
            if (startDistance > 0) {
                location = location.add(direction.clone().multiply(startDistance));
            }
            direction = direction.multiply(speed);
        } else {
            location = context.getTargetLocation();
        }
	     
        FireworkEffect effect = getFireworkEffect(context, color1, color2, fireworkType, flicker, trail);
        EffectUtils.spawnFireworkEffect(location, effect, power, direction);

		return SpellResult.CAST;
	}

    public FireworkEffect getFireworkEffect(CastContext context, Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType, Boolean flicker, Boolean trail) {
        Mage mage = context.getMage();
        Random random = context.getRandom();
        Color wandColor = mage == null ? null : mage.getEffectColor();
        if (wandColor != null) {
            color1 = wandColor;
            color2 = wandColor.mixColors(color1, Color.WHITE);
        } else {
            if (color1 == null) {
                color1 = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            }
            if (color2 == null) {
                color2 = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            }
        }
        if (fireworkType == null) {
            fireworkType = org.bukkit.FireworkEffect.Type.values()[random.nextInt(org.bukkit.FireworkEffect.Type.values().length)];
        }
        if (flicker == null) {
            flicker = random.nextBoolean();
        }
        if (trail == null) {
            trail = random.nextBoolean();
        }

        return FireworkEffect.builder().flicker(flicker).withColor(color1).withFade(color2).with(fireworkType).trail(trail).build();
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
