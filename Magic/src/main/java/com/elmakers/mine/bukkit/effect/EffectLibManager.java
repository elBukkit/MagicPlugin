package com.elmakers.mine.bukkit.effect;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.util.DynamicLocation;
import de.slikey.effectlib.util.ParticleOptions;

/**
 * Manages EffectLib integration
 */
public class EffectLibManager {
    private static EffectManager effectManager;

    private final Plugin plugin;

    public EffectLibManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public static EffectLibManager initialize(Plugin plugin, Logger logger) {
        if (effectManager == null) {
            effectManager = new EffectManager(plugin, logger);
            effectManager.setImageCacheFolder(new File(plugin.getDataFolder(), "data/imagemapcache"));
        }

        return new EffectLibManager(plugin);
    }

    public void enableDebug(boolean debug) {
        if (effectManager != null) {
            effectManager.enableDebug(debug);
        }
    }

    public void enableStackTraces(boolean debug) {
        if (effectManager != null) {
            effectManager.enableStackTraces(debug);
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
    public EffectLibPlay play(ConfigurationSection configuration, EffectPlayer player, DynamicLocation origin, DynamicLocation target, ConfigurationSection parameterMap, String logContext) {
        return loadEffect(true, configuration, player, origin, target, parameterMap, logContext);
    }

    @Nullable
    public EffectLibPlay validate(ConfigurationSection configuration, EffectPlayer player, DynamicLocation origin, DynamicLocation target, ConfigurationSection parameterMap, String logContext) {
        return loadEffect(false, configuration, player, origin, target, parameterMap, logContext);
    }

    @Nullable
    public EffectLibPlay loadEffect(boolean play, ConfigurationSection configuration, EffectPlayer player, DynamicLocation origin, DynamicLocation target, ConfigurationSection parameterMap, String logContext) {
        // Check visibility type
        Player targetPlayer = null;
        switch (player.getVisibility()) {
            case TARGET:
                if (target != null && target.getEntity() instanceof Player) {
                    targetPlayer = (Player)target.getEntity();
                }
                if (targetPlayer == null) {
                    return null;
                }
                break;
            case ORIGIN:
                if (origin != null && origin.getEntity() instanceof Player) {
                    targetPlayer = (Player)origin.getEntity();
                }
                if (targetPlayer == null) {
                    return null;
                }
                break;
            default:
                break;
        }

        if (parameterMap == null) {
            parameterMap = ConfigurationUtils.newConfigurationSection();
        }
        Entity originEntity = origin == null ? null : origin.getEntity();
        if (originEntity != null && originEntity instanceof Player) {
            parameterMap.set("$name", originEntity.getName());
        } else if (originEntity != null) {
            parameterMap.set("$name", originEntity.getCustomName());
        } else {
            parameterMap.set("$name", "Unknown");
        }
        Entity targetEntity = target == null ? null : target.getEntity();
        if (targetEntity != null && targetEntity instanceof Player) {
            parameterMap.set("$target", targetEntity.getName());
        } else if (targetEntity != null) {
            parameterMap.set("$target", targetEntity.getCustomName());
        } else {
            parameterMap.set("$target", "Unknown");
        }

        Effect effect = null;
        String effectClass = configuration.getString("class");
        if (effectClass == null) {
            plugin.getLogger().warning("An effectlib effect is defined without a class property");
            return null;
        }
        Particle particleEffect = player.overrideParticle(null);
        String effectOverride = player.getParticleOverrideName();
        if (effectOverride != null && effectOverride.isEmpty()) effectOverride = null;
        String colorOverrideName = player.getColorOverrideName();
        if (colorOverrideName != null && colorOverrideName.isEmpty()) colorOverrideName = null;
        String radiusOverrideName = player.getRadiusOverrideName();
        if (radiusOverrideName != null && radiusOverrideName.isEmpty()) radiusOverrideName = null;
        ConfigurationSection parameters = configuration;
        Color colorOverride = player.getColor1();
        if ((colorOverrideName != null && colorOverride != null)
        || (effectOverride != null && particleEffect != null)
        || radiusOverrideName != null)
        {
            parameters = ConfigurationUtils.newConfigurationSection();
            Collection<String> keys = configuration.getKeys(false);
            for (String key : keys) {
                parameters.set(key, configuration.get(key));
            }
            if (effectOverride != null && particleEffect != null)
            {
                parameters.set(effectOverride, particleEffect.name());
            }
            if (radiusOverrideName != null)
            {
                parameters.set(radiusOverrideName, player.getRadius());
            }
            if (colorOverride != null && colorOverrideName != null)
            {
                String hexColor = Integer.toHexString(colorOverride.asRGB());
                parameters.set(colorOverrideName, hexColor);
            }
        }

        try {
            effect = effectManager.getEffect(effectClass, parameters, origin, target, parameterMap, targetPlayer, logContext);
            if (effect != null) {
                if (!parameters.contains("material")) {
                    MaterialAndData mat = player.getWorkingMaterial();
                    if (mat != null) {
                        Byte data = mat.getBlockData();
                        effect.material = mat.getMaterial();
                        effect.materialData = data == null ? 0 : data;
                    }
                }
                if (play) {
                    effect.start();
                }
            }
        } catch (Throwable ex) {
            plugin.getLogger().log(Level.WARNING, "Error playing effects of class: " + effectClass, ex);
        }
        return effect == null ? null : new EffectLibPlay(effect);
    }

    public void cancel(Collection<Effect> effects) {
        for (Effect effect : effects) {
            try {
                effect.cancel();
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.WARNING, "Error cancelling effects", ex);
            }
        }
    }

    public void displayParticle(Particle particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, float size, Color color, Material material, byte materialData, double range) {
        effectManager.display(particle, center, offsetX, offsetY, offsetZ, speed, amount, size, color, material, materialData, range, null);
    }

    public void displayParticle(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        effectManager.display(particle, options, center, range, targetPlayers);
    }

    public void ignorePlayer(Player player, boolean ignore) {
        effectManager.ignorePlayer(player, ignore);
    }
}
