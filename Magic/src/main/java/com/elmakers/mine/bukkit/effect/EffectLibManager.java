package com.elmakers.mine.bukkit.effect;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.block.MaterialAndData;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.util.DynamicLocation;
import de.slikey.effectlib.util.ParticleEffect;

/**
 * Manages EffectLib integration
 */
public class EffectLibManager {
    private static EffectManager effectManager;

    private final Plugin plugin;

    public EffectLibManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public static EffectLibManager initialize(Plugin plugin) {
        if (effectManager == null) {
            effectManager = new EffectManager(plugin);
            effectManager.setImageCacheFolder(new File(plugin.getDataFolder(), "data/imagemapcache"));
        }

        return new EffectLibManager(plugin);
    }

    public void enableDebug(boolean debug) {
        if (effectManager != null) {
            effectManager.enableDebug(debug);
        }
    }

    public boolean isDebugEnabled() {
        return effectManager != null ? effectManager.isDebugEnabled() : false;
    }

    public void setParticleRange(int range) {
        if (effectManager != null) {
            effectManager.setParticleRange(range);
        }
    }

    @Nullable
    public Effect play(ConfigurationSection configuration, EffectPlayer player, DynamicLocation origin, DynamicLocation target, Map<String, String> parameterMap) {
        if (parameterMap == null) {
            parameterMap = new HashMap<>();
        }
        Entity originEntity = origin == null ? null : origin.getEntity();
        if (originEntity != null && originEntity instanceof Player) {
            parameterMap.put("$name", ((Player)originEntity).getName());
        } else if (originEntity != null && originEntity instanceof LivingEntity) {
            parameterMap.put("$name", ((LivingEntity)originEntity).getCustomName());
        } else {
            parameterMap.put("$name", "Unknown");
        }
        Entity targetEntity = target == null ? null : target.getEntity();
        if (targetEntity != null && targetEntity instanceof Player) {
            parameterMap.put("$target", ((Player)targetEntity).getName());
        } else if (originEntity != null && targetEntity instanceof LivingEntity) {
            parameterMap.put("$target", ((LivingEntity)targetEntity).getCustomName());
        } else {
            parameterMap.put("$target", "Unknown");
        }

        Effect effect = null;
        String effectClass = configuration.getString("class");
        ParticleEffect particleEffect = player.overrideParticle(null);
        String effectOverride = player.getParticleOverrideName();
        if (effectOverride != null && effectOverride.isEmpty()) effectOverride = null;
        String colorOverrideName = player.getColorOverrideName();
        if (colorOverrideName != null && colorOverrideName.isEmpty()) colorOverrideName = null;
        ConfigurationSection parameters = configuration;
        Color colorOverride = player.getColor1();
        if ((colorOverrideName != null && colorOverride != null) || (effectOverride != null && particleEffect != null))
        {
            parameters = new MemoryConfiguration();
            Collection<String> keys = configuration.getKeys(false);
            for (String key : keys) {
                parameters.set(key, configuration.get(key));
            }
            if (effectOverride != null && particleEffect != null)
            {
                parameters.set(effectOverride, particleEffect.name());
            }
            if (colorOverride != null && colorOverrideName != null)
            {
                String hexColor = Integer.toHexString(colorOverride.asRGB());
                parameters.set(colorOverrideName, hexColor);
            }
        }

        try {
            effect = effectManager.start(effectClass, parameters, origin, target, parameterMap);
            if (!parameters.contains("material"))
            {
                MaterialAndData mat = player.getWorkingMaterial();
                if (mat != null) {
                    effect.material = mat.getMaterial();
                    effect.materialData = mat.getBlockData();
                }
            }
        } catch (Throwable ex) {
            plugin.getLogger().warning("Error playing effects of class: " + effectClass);
            ex.printStackTrace();
        }
        return effect;
    }

    public void cancel(Collection<Effect> effects) {
        for (Effect effect : effects) {
            try {
                effect.cancel();
            } catch (Throwable ex) {
                plugin.getLogger().warning("Error cancelling effects");
                ex.printStackTrace();
            }
        }
    }
}
