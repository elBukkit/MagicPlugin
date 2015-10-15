package com.elmakers.mine.bukkit.effect.builtin;

import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.effect.EffectRepeating;

public class EffectRing extends EffectRepeating {

    protected int size = 8;
    protected float radius = 1;

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

    public void iterate() {
        Location origin = getOrigin();
        if (origin == null) return;
        float currentRadius = scale(radius * scale);

        // Randomization
        double startRadians = Math.random() * Math.PI * 2;

        for (int i = 0; i < size; i++) {
            double radians = (double)i / size * Math.PI * 2 + startRadians;
            Vector direction = new Vector(Math.cos(radians) * currentRadius, 0, Math.sin(radians) * currentRadius);
            Location source = origin.clone();
            Location target = getTarget();
            source.add(direction);
            if (target != null) {
                target = target.clone();
                target.add(direction);
            }
            playEffect(new DynamicLocation(source), new DynamicLocation(target));
        }
    }
}
