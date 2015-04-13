package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.action.TriggeredCompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.EffectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;

public class FireworkAction extends TriggeredCompoundAction
{
    private int power;
    private Integer ticksFlown;
    private Integer expectedLifespan;
    private Color color1 = null;
    private Color color2 = null;
    private Type fireworkType = null;
    private Boolean flicker = null;
    private Boolean trail = null;
    private boolean launch = false;
    private int startDistance;
    private double speed;
    private double dyOffset;
    private int checkInterval;
    private boolean setTarget;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {

        Random rand = context.getRandom();
        power = rand.nextInt(2) + 1;
        Mage mage = context.getMage();

        // Configuration overrides
        power = parameters.getInt("power", power);
        if (parameters.contains("color")) {
            color1 = getColor(parameters.getString("color"));
        } else if (mage.getEffectColor() != null) {
            color1 = mage.getEffectColor();
        }
        if (parameters.contains("color2")) {
            color2 = getColor(parameters.getString("color2"));
        }
        if (parameters.contains("firework")) {
            fireworkType = getType(parameters.getString("firework"));
        }
        flicker = parameters.getBoolean("flicker");
        trail = parameters.getBoolean("trail");

        launch = parameters.getBoolean("launch", false);
        startDistance = parameters.getInt("start", 0);
        speed = parameters.getDouble("speed", 0.1);
        dyOffset = parameters.getDouble("dy_offset", 0);
        checkInterval = parameters.getInt("check_interval", 10);
        setTarget = parameters.getBoolean("set_target", false);
        if (parameters.contains("ticks_flown")) {
            ticksFlown = parameters.getInt("ticks_flown");
        } else {
            ticksFlown = null;
        }
        if (parameters.contains("expected_lifespan")) {
            expectedLifespan = parameters.getInt("expected_lifespan");
        } else {
            expectedLifespan = null;
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Location location = context.getEyeLocation();
        Vector direction = null;
        if (launch) {
            direction = context.getDirection();
            if (dyOffset != 0) {
                direction.setY(direction.getY() + dyOffset);
            }
            direction = direction.normalize();
            if (startDistance > 0) {
                location = location.add(direction.clone().multiply(startDistance));
            }
            direction = direction.multiply(speed);
        } else {
            location = context.getTargetLocation();
        }
	     
        FireworkEffect effect = EffectUtils.getFireworkEffect(context, color1, color2, fireworkType, flicker, trail);
        Entity firework = EffectUtils.spawnFireworkEffect(location, effect, power, direction, expectedLifespan, ticksFlown);

        if (firework != null)
        {
            Collection<EffectPlayer> projectileEffects = context.getEffects("projectile");
            for (EffectPlayer effectPlayer : projectileEffects) {
                effectPlayer.start(firework.getLocation(), firework, null, null);
            }

            ActionHandler.setActions(firework, actions, context, parameters, "indirect_player_message");
            ActionHandler.setEffects(firework, context, "hit");

            scheduleEntityCheck(context, firework, checkInterval);
            if (setTarget) {
                context.setTargetEntity(firework);
            }
        }

		return SpellResult.CAST;
	}

    protected void scheduleEntityCheck(final CastContext context, final Entity entity, final int interval) {
        if (interval > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(context.getController().getPlugin(), new Runnable() {
                public void run() {
                    checkEntity(context, entity, interval);
                }
            }, interval);
        }
    }

    protected void checkEntity(final CastContext context, final Entity entity, final int interval) {
        if (entity.isDead() || !entity.isValid()) {
            ActionHandler.runActions(entity, entity.getLocation(), entity);
            ActionHandler.runEffects(entity);
        } else {
            scheduleEntityCheck(context, entity, interval);
        }
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
