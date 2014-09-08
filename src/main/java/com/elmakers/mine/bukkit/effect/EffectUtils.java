package com.elmakers.mine.bukkit.effect;

import java.lang.reflect.Method;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import com.elmakers.mine.bukkit.utility.NMSUtils;

public class EffectUtils extends NMSUtils {

    public static void spawnFireworkEffect(Location location, FireworkEffect effect, int power) {
        World world = location.getWorld();

        try {
            // Initialize the Firework object
            Firework firework = world.spawn(location, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            firework.setFireworkMeta(meta);
            firework.detonate();

            // Remove the firework, we only needed it for the effect
            firework.remove();
        } catch (Exception ex) {

        }
    }
}
