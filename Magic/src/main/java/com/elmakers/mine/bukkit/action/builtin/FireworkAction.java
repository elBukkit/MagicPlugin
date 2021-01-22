package com.elmakers.mine.bukkit.action.builtin;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseProjectileAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.EffectUtils;
import com.elmakers.mine.bukkit.utility.ColorHD;

public class FireworkAction extends BaseProjectileAction
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
    private boolean silent;
    private boolean useWandColor;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
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
        silent = parameters.getBoolean("silent", false);
        startDistance = parameters.getInt("start", 0);
        speed = parameters.getDouble("speed", 0.1);
        dyOffset = parameters.getDouble("dy_offset", 0);
        useWandColor = parameters.getBoolean("use_wand_color", true);
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
    public SpellResult start(CastContext context) {
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
            if (location == null) {
                return SpellResult.NO_TARGET;
            }
        }

        FireworkEffect effect = EffectUtils.getFireworkEffect(context, color1, color2, fireworkType, flicker, trail, useWandColor);
        Entity firework = EffectUtils.spawnFireworkEffect(context.getPlugin().getServer(), location, effect, power, direction, expectedLifespan, ticksFlown, silent);
        Entity sourceEntity = context.getEntity();
        if (firework instanceof Projectile && sourceEntity instanceof ProjectileSource) {
            ((Projectile)firework).setShooter((ProjectileSource)sourceEntity);
        }
        if (firework == null) {
            if (direction != null) {
                org.bukkit.Bukkit.getLogger().warning("Failed to spawn firework entity");
                return SpellResult.FAIL;
            }
            return SpellResult.CAST;
        }

        track(context, firework);
        return checkTracking(context);
    }

    protected Color getColor(String name) {
        ColorHD color = new ColorHD(name);
        return color.getColor();
    }

    protected Type getType(String name) {
        for (Type t : Type.values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }

        return Type.BALL;
    }

    @Override
    public boolean requiresTarget() {
        return !launch;
    }

    @Override
    public boolean isUndoable() {
        return false;
    }
}
