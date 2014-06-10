package com.elmakers.mine.bukkit.effect;

import com.elmakers.mine.bukkit.api.effect.ParticleType;
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

/**
 * Manages EffectLib integration
 */
public class EffectLibManager {
    private static EffectManager effectManager;
    private static Class<?> particleEffectClass;
    private static Method particleEffectLookupMethod;

    public EffectLibManager() {
    }

    public static EffectLibManager initialize(Plugin plugin) {
        if (effectManager == null) {
            effectManager = new EffectManager(plugin);
        }

        // TODO: Remove the reflection here, kind of unnecessary now
        // that EffectLib is Mavenized
        // use its API without breaking my build.
        try {
            if (particleEffectClass == null) {
                particleEffectClass = Class.forName("com.elmakers.mine.bukkit.slikey.effectlib.util.ParticleEffect");
            }

            if (particleEffectLookupMethod == null) {
                particleEffectLookupMethod = particleEffectClass.getMethod("fromName", String.class);
            }

            if (particleEffectClass == null || particleEffectLookupMethod == null) {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return new EffectLibManager();
    }

    protected Object[] tryPointConstructor(Class<?> effectLibClass, EffectPlayer effectPlayer) {
        Location target = effectPlayer.playAtTarget ? effectPlayer.target : null;
        Location origin = effectPlayer.playAtOrigin ? effectPlayer.origin : null;
        if (origin == null && target == null) return null;

        Object[] players = null;
        try {
            Constructor constructor = effectLibClass.getConstructor(effectManager.getClass(), Location.class);
            if (target != null && origin != null) {
                players = new Object[2];
                players[0] = constructor.newInstance(effectManager, target);
                players[1] = constructor.newInstance(effectManager, origin);
            } else if (target != null) {
                players = new Object[1];
                players[0] = constructor.newInstance(effectManager, target);
            } else if (origin != null) {
                players = new Object[1];
                players[0] = constructor.newInstance(effectManager, origin);
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
            Constructor constructor = effectLibClass.getConstructor(effectManager.getClass(), Entity.class);
            if (target != null && origin != null) {
                players = new Object[2];
                players[0] = constructor.newInstance(effectManager, target);
                players[1] = constructor.newInstance(effectManager, origin);
            } else if (target != null) {
                players = new Object[1];
                players[0] = constructor.newInstance(effectManager, target);
            } else if (origin != null) {
                players = new Object[1];
                players[0] = constructor.newInstance(effectManager, origin);
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
            Constructor constructor = effectLibClass.getConstructor(effectManager.getClass(), Location.class, Location.class);
            player = constructor.newInstance(effectManager, effectPlayer.origin, effectPlayer.target);
        } catch (Exception ex) {
            player = null;
        }
        return player;
    }

    public Object play(Plugin plugin, ConfigurationSection configuration, EffectPlayer player) {
        Class<?> effectLibClass = null;
        String className = configuration.getString("class");
        try {
            effectLibClass = Class.forName("com.elmakers.mine.bukkit.slikey.effectlib.effect." + className);
        } catch (Throwable ex) {
            plugin.getLogger().info("Error loading EffectLib class: " + className);
            ex.printStackTrace();
            return null;
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
            return null;
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

        return effects;
    }

    public void cancel(Object token) {
        Object[] effects = (Object[])token;
        for (Object effect : effects) {
            try {
                Method cancelMethod = effect.getClass().getMethod("cancel", Boolean.TYPE);
                cancelMethod.invoke(effect, true);
            } catch (Throwable ex) {
                ex.printStackTrace();
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
