package com.elmakers.mine.bukkit.effect;

import com.elmakers.mine.bukkit.api.effect.ParticleType;
import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.EffectManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages EffectLib integration
 */
public class EffectLibManager {
    private static EffectManager effectManager;
    private static final Map<String, String> nameMap = new HashMap<String, String>();

    public EffectLibManager() {
    }

    public static EffectLibManager initialize(Plugin plugin) {
        if (effectManager == null) {
            effectManager = new EffectManager(plugin);
        }

        return new EffectLibManager();
    }

    public Effect[] play(Plugin plugin, ConfigurationSection configuration, EffectPlayer player, Location origin, Location target) {
        Entity sourceEntity = player.getOriginEntity();
        Entity targetEntity = player.getTargetEntity();
        if (sourceEntity != null && sourceEntity instanceof Player) {
            nameMap.put("$name", ((Player)sourceEntity).getName());
        } else {
            nameMap.put("$name", "Unknown");
        }

        return effectManager.start(configuration.getString("class"), configuration, origin, target, sourceEntity, targetEntity, nameMap);
    }

    public void cancel(Effect[] effects) {
        for (Effect effect : effects) {
            try {
                effect.cancel();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
