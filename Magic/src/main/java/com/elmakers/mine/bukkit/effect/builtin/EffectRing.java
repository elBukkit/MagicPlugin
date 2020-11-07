package com.elmakers.mine.bukkit.effect.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.effect.EffectRepeating;

import de.slikey.effectlib.util.DynamicLocation;

public class EffectRing extends EffectRepeating {

    protected int size = 8;
    protected float radius = 1;

    private float useRadius;

    public EffectRing() {

    }

    public EffectRing(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void load(Plugin plugin, ConfigurationSection configuration) {
        super.load(plugin, configuration);

        radius = (float)configuration.getDouble("radius", radius);
        size = configuration.getInt("size", size);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public void run() {
        if (radius == 0) {
            useRadius = (float)getRadius();
        } else {
            useRadius = radius;
        }
        super.run();
    }

    @Override
    public void iterate() {
        Location origin = getOrigin();
        if (origin == null) return;
        float currentRadius = scale(useRadius * useRadius);

        // Randomization
        double startRadians = Math.random() * Math.PI * 2;

        for (int i = 0; i < size; i++) {
            double radians = (double)i / size * Math.PI * 2 + startRadians;
            Vector direction = new Vector(Math.cos(radians) * currentRadius, 0, Math.sin(radians) * currentRadius);
            Location source = origin;
            Location target = getTarget();
            if (playAtOrigin) {
                source = source.clone();
                source.add(direction);
            }
            if (target != null && playAtTarget) {
                target = target.clone();
                target.add(direction);
            }
            playEffect(new DynamicLocation(source, getOriginEntity()), new DynamicLocation(target, getTargetEntity()));
        }
    }
}
