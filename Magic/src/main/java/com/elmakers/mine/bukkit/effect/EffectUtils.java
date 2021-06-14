package com.elmakers.mine.bukkit.effect;

import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class EffectUtils {
    public static void spawnFireworkEffect(Server server, Location location, FireworkEffect effect, int power) {
        spawnFireworkEffect(server, location, effect, power, null, 2, 1);
    }

    public static void spawnFireworkEffect(Server server, Location location, FireworkEffect effect, int power, boolean silent) {
        spawnFireworkEffect(server, location, effect, power, null, 2, 1, silent);
    }

    @Nullable
    public static Entity spawnFireworkEffect(Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown) {
        return spawnFireworkEffect(server, location, effect, power, direction, expectedLifespan, ticksFlown, false);
    }

    @Nullable
    public static Entity spawnFireworkEffect(Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown, boolean silent) {
        return CompatibilityUtils.spawnFireworkEffect(DefaultMaterials.getFirework(), server, location, effect, power, direction, expectedLifespan, ticksFlown, silent);
    }

    public static FireworkEffect getFireworkEffect(CastContext context, Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType, Boolean flicker, Boolean trail) {
        return getFireworkEffect(context, color1, color2, fireworkType, flicker, trail, true);
    }

    public static FireworkEffect getFireworkEffect(CastContext context, Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType, Boolean flicker, Boolean trail, boolean useWandColor) {
        Mage mage = context.getMage();
        Random random = context.getRandom();
        Color wandColor = mage.getEffectColor();
        if (wandColor != null && useWandColor) {
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

}
