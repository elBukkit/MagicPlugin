package com.elmakers.mine.bukkit.effect.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.effect.EffectRepeating;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.util.DynamicLocation;

public class EffectTrail extends EffectRepeating {

    protected Double length;

    // State
    protected double size;
    protected Vector direction;

    public EffectTrail() {

    }

    public EffectTrail(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void load(Plugin plugin, ConfigurationSection configuration) {
        // Different default for a trail, more iterations are generally needed.
        iterations = 8;

        super.load(plugin, configuration);
        length = ConfigurationUtils.getDouble(configuration, "length", length);
    }

    @Override
    public void play() {
        Location target = getTarget();
        Location origin = getOrigin();
        if (length != null) {
            size = length;
        } else if (target != null && origin != null) {
            size = origin.distance(target);
        } else {
            size = 0;
        }

        // Don't bother playing if it's right in front of us.
        if (size < 1) {
            stop();
            return;
        }

        direction = getDirection();
        super.play();
    }

    @Override
    public void iterate() {
        Location origin = getOrigin();
        Location target = getTarget();
        if (origin == null) return;
        Vector delta = direction.clone();
        Location source = origin.clone();
        if (playAtOrigin) {
            source.add(delta.multiply(scale(size) + 1));
        }
        if (target != null && playAtTarget) {
            target = target.clone();
            target.add(delta.multiply(-scale(size) + 1));
        }
        playEffect(new DynamicLocation(source, getOriginEntity()), new DynamicLocation(target, getTargetEntity()));
    }

    @Override
    protected void checkLocations() {
        Location target = getTarget();
        Location origin = getOrigin();
        if (target == null && origin != null) {
            Vector delta = origin.getDirection().clone().normalize();
            target = origin.clone();
            target.add(delta.multiply(size + 1));
            setTarget(target);
        }
        super.checkLocations();
    }
}
