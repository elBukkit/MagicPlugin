package com.elmakers.mine.bukkit.effect;

import com.elmakers.mine.bukkit.api.effect.ParticleType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Manages EffectLib integration
 */
public class EffectLibManager {
    private final Object manager;
    private Class<?> particleEffectClass;
    private Method particleEffectLookupMethod;

    public EffectLibManager(Object manager) {
        this.manager = manager;
    }

    public boolean initialize() {
        if (manager == null) {
            return false;
        }

        try {
            particleEffectClass = Class.forName("de.slikey.effectlib.util.ParticleEffect");
            particleEffectLookupMethod = particleEffectClass.getMethod("fromName", String.class);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return particleEffectClass != null && particleEffectLookupMethod != null;
    }

    protected Object[] tryPointConstructor(Class<?> effectLibClass, EffectPlayer effectPlayer) {
        Location target = effectPlayer.playAtTarget ? effectPlayer.target : null;
        Location origin = effectPlayer.playAtOrigin ? effectPlayer.origin : null;
        if (origin == null && target == null) return null;

        Object[] players = null;
        try {
            Constructor constructor = effectLibClass.getConstructor(manager.getClass(), Location.class);
            if (target != null && origin != null) {
                players = new Object[2];
                players[0] = constructor.newInstance(manager, target);
                players[1] = constructor.newInstance(manager, origin);
            } else if (target != null) {
                players = new Object[1];
                players[0] = constructor.newInstance(manager, target);
            } else if (origin != null) {
                players = new Object[1];
                players[0] = constructor.newInstance(manager, origin);
            }
        } catch (Exception ex) {
            players = null;
        }
        return players;
    }

    protected Object[] tryEntityConstructor(Class<?> effectLibClass, EffectPlayer effectPlayer) {
        Entity target = effectPlayer.targetEntity != null && effectPlayer.playAtTarget ? effectPlayer.targetEntity.get() : null;
        Entity origin = effectPlayer.originEntity != null && effectPlayer.playAtOrigin  ? effectPlayer.originEntity.get() : null;
        if (target == null && origin == null) return null;

        Object[] players = null;
        try {
            Constructor constructor = effectLibClass.getConstructor(manager.getClass(), Entity.class);
            if (target != null && origin != null) {
                players = new Object[2];
                players[0] = constructor.newInstance(manager, target);
                players[1] = constructor.newInstance(manager, origin);
            } else if (target != null) {
                players = new Object[1];
                players[0] = constructor.newInstance(manager, target);
            } else if (origin != null) {
                players = new Object[1];
                players[0] = constructor.newInstance(manager, origin);
            }
        } catch (Exception ex) {
            players = null;
        }
        return players;
    }

    protected Object tryLineConstructor(Class<?> effectLibClass, EffectPlayer effectPlayer) {
        if (effectPlayer.origin == null || effectPlayer.target == null) return null;

        Object player = null;
        try {
            Constructor constructor = effectLibClass.getConstructor(manager.getClass(), Location.class, Location.class);
            player = constructor.newInstance(manager, effectPlayer.origin, effectPlayer.target);
        } catch (Exception ex) {
            player = null;
        }
        return player;
    }

    public void play(Plugin plugin, ConfigurationSection configuration, EffectPlayer player) {
        Class<?> effectLibClass = null;
        String className = configuration.getString("class");
        try {
            effectLibClass = Class.forName("de.slikey.effectlib.effect." + className);
        } catch (Throwable ex) {
            plugin.getLogger().info("Error loading EffectLib class: " + className);
            ex.printStackTrace();
            return;
        }

        Object[] effects = tryPointConstructor(effectLibClass, player);
        if (effects == null) {
            effects = tryEntityConstructor(effectLibClass, player);
            if (effects == null) {
                Object lineEffect = tryLineConstructor(effectLibClass, player);
                if (lineEffect != null) {
                    effects = new Object[1];
                    effects[0] = lineEffect;
                }
            }
        }

        if (effects == null) {
            plugin.getLogger().info("Failed to construct EffectLib class: " + effectLibClass.getName());
            return;
        }

        ParticleType particleType = player.particleType;
        for (Object effect : effects) {
            if (particleType != null) {
                Object converted = convertParticleEffect(particleType);
                if (converted != null) {
                    try {
                        Field particleField = effectLibClass.getField("particle");
                        particleField.set(effect, converted);
                    } catch (Throwable ex) {

                    }
                }
            }

            if (player instanceof EffectRepeating) {
                EffectRepeating repeating = (EffectRepeating)player;
                try {
                    Field periodField = effectLibClass.getField("period");
                    periodField.set(effect, repeating.period);
                    // This does not seem to work out well!
                    //Field iterationsField = effectLibClass.getField("iterations");
                    //iterationsField.set(effect, repeating.iterations);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }

            Collection<String> keys = configuration.getKeys(false);
            for (String key : keys) {
                if (key.equals("class")) continue;

                if (!setField(effect, key, configuration, player)) {
                    plugin.getLogger().warning("Unable to assign EffectLib property " + key + " of class " + className);
                }
            }

            try {
                Method startMethod = effectLibClass.getMethod("start");
                startMethod.invoke(effect);
            } catch (Throwable ex) {

            }
        }
    }

    protected boolean setField(Object effect, String key, ConfigurationSection section, EffectPlayer player) {
        try {
            Field field = effect.getClass().getField(key);
            if (field.getType().equals(Integer.TYPE)) {
                field.set(effect, section.getInt(key));
            } else if (field.getType().equals(Float.TYPE)) {
                field.set(effect, (float)section.getDouble(key));
            } else if (field.getType().equals(Double.TYPE)) {
                field.set(effect, section.getDouble(key));
            } else if (field.getType().equals(Boolean.TYPE)) {
                field.set(effect, section.getBoolean(key));
            } else if (field.getType().equals(Long.TYPE)) {
                field.set(effect, section.getLong(key));
            } else if (field.getType().isAssignableFrom(String.class)) {
                String value = section.getString(key);
                Entity sourceEntity = player.getOriginEntity();
                if (sourceEntity instanceof Player) {
                    value = value.replace("$name", ((Player)sourceEntity).getName());
                }
                field.set(effect, value);
            } else if (field.getType().isAssignableFrom(particleEffectClass)) {
                field.set(effect, convertParticleEffect(ParticleType.valueOf(section.getString(key).toUpperCase())));
            } else {
                return false;
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public Object convertParticleEffect(ParticleType particleType) {
        Object converted = null;
        try {
            converted = particleEffectLookupMethod.invoke(null, particleType.getParticleName());
        } catch (Throwable ex) {
            converted = null;
            ex.printStackTrace();;
        }
        return converted;
    }
}
