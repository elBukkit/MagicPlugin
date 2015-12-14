package com.elmakers.mine.bukkit.effect.builtin;

import com.elmakers.mine.bukkit.effect.EffectRepeating;
import com.elmakers.mine.bukkit.math.VectorTransform;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

public class EffectTransform extends EffectRepeating {
    private VectorTransform positionTransform;
    private long playTime;
    private long lastIteration;
    private long totalSteps;
    private long steps;
    private long maxStep;

    public EffectTransform() {
    }

    public void play() {
        playTime = 0;
        totalSteps = 0;
        lastIteration = System.currentTimeMillis();
        super.play();
    }

    @Override
    public void load(Plugin plugin, ConfigurationSection parameters) {
        super.load(plugin, parameters);
        ConfigurationSection transform = ConfigurationUtils.getConfigurationSection(parameters, "position_transform");
        if (transform != null) {
            positionTransform = new VectorTransform(transform);
        } else {
            positionTransform = null;
        }
        steps = parameters.getInt("steps", 0);
        maxStep = parameters.getInt("max_steps", 0);
    }

    public void iterateSteps(Location originalOrigin, Location originalTarget) {
        for (int i = 0; i < steps; i++) {
            Location source = originalOrigin;
            Location target = originalTarget;
            if (playAtOrigin) {
                source = source.clone();
                source.add(positionTransform.get(source, totalSteps));
            }
            if (target != null && playAtTarget) {
                target = target.clone();
                target.add(positionTransform.get(target, totalSteps));
            }
            playEffect(new DynamicLocation(source, getOriginEntity()), new DynamicLocation(target, getTargetEntity()));
            totalSteps++;
            if (maxStep > 0 && totalSteps >= maxStep) {
                totalSteps = 0;
            }
        }
    }

    public void iterate() {
        if (positionTransform == null) {
            playEffect();
            return;
        }
        Location origin = getOrigin();
        Location target = getTarget();
        if (origin == null) return;
        if (steps > 0) {
            iterateSteps(origin, target);
            return;
        }

        Location source = origin;
        double t = (double)playTime / 1000;
        if (playAtOrigin) {
            source = source.clone();
            source.add(positionTransform.get(source, t));
        }
        if (target != null && playAtTarget) {
            target = target.clone();
            target.add(positionTransform.get(target, t));
        }
        long now = System.currentTimeMillis();
        playTime += (now - lastIteration);
        lastIteration = now;
        playEffect(new DynamicLocation(source, getOriginEntity()), new DynamicLocation(target, getTargetEntity()));
    }
}
