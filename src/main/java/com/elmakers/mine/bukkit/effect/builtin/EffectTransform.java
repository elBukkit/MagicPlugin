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

    public EffectTransform() {
    }

    public void play() {
        playTime = 0;
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
    }

    public void iterate() {
        if (positionTransform == null) {
            playEffect();
            return;
        }
        Location origin = getOrigin();
        Location target = getTarget();
        if (origin == null) return;
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
