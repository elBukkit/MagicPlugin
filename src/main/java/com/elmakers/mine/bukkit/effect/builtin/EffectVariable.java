package com.elmakers.mine.bukkit.effect.builtin;

import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class EffectVariable extends EffectPlayer {

    private Map<Double, Collection<EffectPlayer>> brightnessMap = new TreeMap<Double, Collection<EffectPlayer>>();
    private Collection<EffectPlayer> playing = new ArrayList<EffectPlayer>();

    public EffectVariable() {

    }

    @Override
    public void cancel() {
        super.cancel();
        for (EffectPlayer player : playing) {
            player.cancel();
        }
    }

    @Override
    public void load(Plugin plugin, ConfigurationSection configuration) {
        super.load(plugin, configuration);

        ConfigurationSection brightness = ConfigurationUtils.getConfigurationSection(configuration, "brightness");
        Collection<String> keys = brightness.getKeys(false);
        for (String key : keys) {
            try {
                double level = Double.parseDouble(key);
                brightnessMap.put(level, EffectPlayer.loadEffects(plugin, brightness, key));
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void play() {
        playing.clear();
        if (brightnessMap.size() > 0) {
            double brightness = 0;
            Color color = getColor1();
            if (color != null) {
                brightness = color.getRed() / 255.0 + color.getBlue() / 255.0 + color.getGreen() / 255.0;
            }
            for (Map.Entry<Double, Collection<EffectPlayer>> entry : brightnessMap.entrySet()) {
                if (brightness < entry.getKey()) {
                    for (EffectPlayer player : entry.getValue()) {
                        // Set scale
                        player.setScale(scale);

                        // Set material and color
                        player.setMaterial(getWorkingMaterial());
                        player.setColor(getColor1());
                        player.setParticleOverride(particleOverride == null ? null : particleOverride.name());
                        player.start(getOrigin(), getOriginEntity(), getTarget(), getTargetEntity());

                        playing.add(player);
                    }
                    break;
                }
            }
        }
    }
}
