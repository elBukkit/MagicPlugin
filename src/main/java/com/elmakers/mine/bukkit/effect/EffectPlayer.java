package com.elmakers.mine.bukkit.effect;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.effect.ParticleType;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class EffectPlayer implements com.elmakers.mine.bukkit.api.effect.EffectPlayer {

    public static boolean setEffectManager(Object manager) {
        effectLib = new EffectLibManager(manager);
        if (!effectLib.initialize()) {
            effectLib = null;
            return false;
        }
        return true;
    }

    private static EffectLibManager effectLib = null;
    private ConfigurationSection effectLibConfig = null;

	public static boolean SOUNDS_ENABLED = true;
	
	protected Plugin plugin;
	
	protected Location origin;
	protected Location target;
    protected Vector originOffset;
    protected Vector targetOffset;
    protected WeakReference<Entity> originEntity;
    protected WeakReference<Entity> targetEntity;

	// These are ignored by the Trail type, need multi-inheritance :\
	protected boolean playAtOrigin = true;
	protected boolean playAtTarget = false;
	
	protected Color color;
	protected MaterialAndData material;
	
	protected int delayTicks = 0;

	protected MaterialAndData material1;
	protected Color color1 = Color.PURPLE;
	protected Color color2 = Color.TEAL;

    protected EntityEffect entityEffect = null;

	protected Effect effect = null;
	protected Integer effectData = null;
	
	protected Sound sound = null;
	protected float soundVolume = 0.7f;
	protected float soundPitch = 1.5f;
	
	protected boolean hasFirework = false;
	protected FireworkEffect.Type fireworkType;
	protected int fireworkPower = 1;
	protected Boolean fireworkFlicker;
	
	protected FireworkEffect fireworkEffect;
	
	protected ParticleType particleType = null;
	protected String particleSubType = "";
	protected float particleData = 0f;
	protected float particleXOffset = 0.3f;
	protected float particleYOffset = 0.3f;
	protected float particleZOffset = 0.3f;
	protected int particleCount = 1;
	
	protected Vector offset = new Vector(0, 0, 0);
	
	public EffectPlayer() {
	}

	public EffectPlayer(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public void load(Plugin plugin, ConfigurationSection configuration) {
		this.plugin = plugin;

        if (effectLib != null && configuration.contains("effectlib")) {
            effectLibConfig = configuration.getConfigurationSection("effectlib");
            // I feel like ConfigurationSection should be smart enough for the above
            // to work, but it is not.
            // I suspect this is due to having Maps in Lists. Oh well.
            if (effectLibConfig == null) {
                Object rawConfig = configuration.get("effectlib");
                if (rawConfig instanceof Map) {
                    effectLibConfig = new MemoryConfiguration();
                    Map<String, Object> map = (Map<String, Object>)rawConfig;
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        effectLibConfig.set(entry.getKey(), entry.getValue());
                    }
                } else {
                    plugin.getLogger().warning("Could not load effectlib node of type " + rawConfig.getClass());
                }
            }
        } else {
            effectLibConfig = null;
        }

        originOffset = ConfigurationUtils.getVector(configuration, "origin_offset");
        targetOffset = ConfigurationUtils.getVector(configuration, "target_offset");
		delayTicks = configuration.getInt("delay", delayTicks) * 20 / 1000;
		material1 = ConfigurationUtils.getMaterialAndData(configuration, "material");
		color1 = ConfigurationUtils.getColor(configuration, "color", Color.PURPLE);
		color2 = ConfigurationUtils.getColor(configuration, "color2", Color.TEAL);
		
		if (configuration.contains("effect")) {
			String effectName = configuration.getString("effect");
			effect = Effect.valueOf(effectName.toUpperCase());
			if (effect == null) {
				plugin.getLogger().warning("Unknown effect type " + effectName);
			} else {
				effectData = ConfigurationUtils.getInteger(configuration, "effect_data", effectData);
			}
		}

        if (configuration.contains("entity_effect")) {
            String effectName = configuration.getString("entity_effect");
            entityEffect = EntityEffect.valueOf(effectName.toUpperCase());
            if (entityEffect == null) {
                plugin.getLogger().warning("Unknown entity effect type " + effectName);
            }
        }

		if (configuration.contains("sound")) {
			String soundName = configuration.getString("sound");
			sound = Sound.valueOf(soundName.toUpperCase());
			if (sound == null) {
				plugin.getLogger().warning("Unknown sound type " + soundName);
			} else {
				soundVolume = (float)configuration.getDouble("sound_volume", soundVolume);
				soundPitch = (float)configuration.getDouble("sound_pitch", soundPitch);
			}
		}

		if (configuration.contains("firework") || configuration.contains("firework_power")) {
			hasFirework = true;
			fireworkType = null;
			if (configuration.contains("firework")) {
				String typeName = configuration.getString("firework");
				fireworkType = FireworkEffect.Type.valueOf(typeName.toUpperCase());
				if (fireworkType == null) {
					plugin.getLogger().warning("Unknown firework type " + typeName);
				}				
			}

			fireworkPower = configuration.getInt("firework_power", fireworkPower);
			fireworkFlicker = ConfigurationUtils.getBoolean(configuration, "firework_flicker", fireworkFlicker);
		}
		if (configuration.contains("particle")) {
			String typeName = configuration.getString("particle");
			particleType = ParticleType.valueOf(typeName.toUpperCase());
			if (particleType == null) {
				plugin.getLogger().warning("Unknown particle type " + typeName);
			} else {
				particleSubType = configuration.getString("particle_sub_type", particleSubType);
				particleData = (float)configuration.getDouble("particle_data", particleData);
				particleXOffset = (float)configuration.getDouble("particle_offset_x", particleXOffset);
				particleYOffset = (float)configuration.getDouble("particle_offset_y", particleYOffset);
				particleZOffset = (float)configuration.getDouble("particle_offset_z", particleZOffset);
				particleCount = configuration.getInt("particle_count", particleCount);
			}			
		}
		
		setLocationType(configuration.getString("location", "origin"));
	}
	
	public void setLocationType(String locationType) {
		if (locationType.equals("target")) {
			playAtOrigin = false;
			playAtTarget = true;
		} else if (locationType.equals("origin")) {
			playAtTarget = false;
			playAtOrigin = true;
		} else if (locationType.equals("both")) {
			playAtTarget = true;
			playAtOrigin = true;
		}
	}
	
	public FireworkEffect getFireworkEffect(Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType) {
			return getFireworkEffect(color1, color2, fireworkType, null, null);
	}

	public FireworkEffect getFireworkEffect(Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType, Boolean flicker, Boolean trail) {
		Random rand = new Random();
		if (color1 == null) {
			color1 = Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
		}
		if (color2 == null) {
			color2 = Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
		}
		if (fireworkType == null) {
			fireworkType = org.bukkit.FireworkEffect.Type.values()[rand.nextInt(org.bukkit.FireworkEffect.Type.values().length)];
		}
		if (flicker == null) {
			flicker = rand.nextBoolean();
		}
		if (trail == null) {
			trail = rand.nextBoolean();
		}
		
		return FireworkEffect.builder().flicker(flicker).withColor(color1).withFade(color2).with(fireworkType).trail(trail).build();
	}
	
	public void setEffect(Effect effect) {
		this.effect = effect;
	}

    public void setEntityEffect(EntityEffect entityEffect) {
        this.entityEffect = entityEffect;
    }

	public void setParticleType(ParticleType particleType) {
		this.particleType = particleType;
	}
	
	public void setParticleSubType(String particleSubType) {
		this.particleSubType = particleSubType;
	}
	
	public void setEffectData(int data) {
		this.effectData = data;
	}
	
	@SuppressWarnings("deprecation")
	protected MaterialAndData getWorkingMaterial() {
		if (material1 != null) return material1;
		MaterialAndData result = material;
		if (result == null && target != null) {
			result = new MaterialAndData(target.getBlock().getType(), target.getBlock().getData());
		} else if (result == null && origin != null) {
			result = new MaterialAndData(origin.getBlock().getType(), target.getBlock().getData());
		} else if (result == null) {
			result = new MaterialAndData(Material.AIR);
		}
		
		return result;
	}
	
	@SuppressWarnings("deprecation")
	protected void playEffect(Location targetLocation) {
		Location location = targetLocation.clone();
		location.add(offset);
		if (effect != null) {
			int data = effectData == null ? 0 : effectData;
			if ((effect == Effect.STEP_SOUND) && effectData == null) {
				Material material = getWorkingMaterial().getMaterial();
				
				// Check for potential bad materials, this can get really hairy (client crashes)
				if (!material.isSolid()) {
					return;
				}
				data = material.getId();
			}
			location.getWorld().playEffect(location, effect, data);
		}
        if (entityEffect != null && originEntity != null && playAtOrigin) {
            Entity entity = originEntity.get();
            if (entity != null) {
                entity.playEffect(entityEffect);
            }
        }
        if (entityEffect != null && targetEntity != null && playAtTarget) {
            Entity entity = targetEntity.get();
            if (entity != null) {
                entity.playEffect(entityEffect);
            }
        }
		if (sound != null) {
			location.getWorld().playSound(location, sound, soundVolume, soundPitch);
		}
		if (fireworkEffect != null) {
			EffectUtils.spawnFireworkEffect(location, fireworkEffect, fireworkPower);
		}
		if (particleType != null) {
			String subType = particleSubType;
			if ((particleType == ParticleType.BLOCK_BREAKING || particleType == ParticleType.TOOL_BREAKING) && particleSubType.length() == 0) {
				Material material = getWorkingMaterial().getMaterial();
				
				// Check for potential bad materials, this can get really hairy (client crashes)
				if (particleType == ParticleType.BLOCK_BREAKING && !material.isSolid()) {
					return;
				}
				
				// TODO: Check for tools... ?
				if (particleType == ParticleType.TOOL_BREAKING) {
					material = Material.DIAMOND_AXE;
				}
				subType = "" + material.getId();
			}
			
			EffectUtils.playEffect(location, particleType, subType, particleXOffset, particleYOffset, particleZOffset, particleData, particleCount);
		}
	}

	public void setParticleData(float effectData) {
		this.particleData = effectData;
	}

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}
	
	public void setParticleOffset(float xOffset, float yOffset, float zOffset) {
		this.particleXOffset = xOffset;
		this.particleYOffset = yOffset;
		this.particleZOffset = zOffset;
	}
	
	public void setSound(Sound sound) {
		this.sound = sound;
	}
	
	public void setSound(Sound sound, float volume, float pitch) {
		this.sound = sound;
		this.soundVolume = volume;
		this.soundPitch = pitch;
	}
	
	public void setDelayTicks(int ticks) {
		delayTicks = ticks;
	}

    @Override
    public void start(Entity originEntity, Entity targetEntity) {
        start(originEntity == null ? null : originEntity.getLocation(), originEntity, targetEntity == null ? null : targetEntity.getLocation(), targetEntity);
    }

    @Override
    public void start(Location origin, Entity originEntity, Location target, Entity targetEntity) {
        this.originEntity = new WeakReference<Entity>(originEntity);
        this.targetEntity = new WeakReference<Entity>(targetEntity);
        start(origin, target);
    }

    @Override
	public void start(Location origin, Location target) {
		if (origin == null) {
			throw new InvalidParameterException("Origin cannot be null");
		}
		
		// Kinda hacky, but makes cross-world trails (e.g. Repair, Backup) work
		if (target != null && !origin.getWorld().equals(target.getWorld())) {
			target.setWorld(origin.getWorld());
		}
		this.origin = origin;
		this.target = target;
		
		if (hasFirework) {
			fireworkEffect = getFireworkEffect(getColor1(), getColor2(), fireworkType, fireworkFlicker, false);
		} else {
			fireworkEffect = null;
		}

        // Should I let EffectLib handle delay?
		if (delayTicks > 0 && plugin != null) {
			final EffectPlayer player = this;
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					player.startPlay();
				}
			}, delayTicks);			
		} else {
            startPlay();
		}
	}

    protected void checkLocations() {
        if (origin != null && originOffset != null) {
            origin = origin.add(originOffset);
        }
        if (target != null && targetOffset != null) {
            target = target.add(targetOffset);
        }
    }

    protected void startPlay() {
        if (effectLib != null && effectLibConfig != null) {
            // Generate a target location for compatibility if none exists.
            checkLocations();
            effectLib.play(plugin, effectLibConfig, this);
        } else {
            play();
        }
    }
	
	protected Vector getDirection() {
		Vector direction = target == null ? origin.getDirection() : target.toVector().subtract(origin.toVector());
		return direction.normalize();
	}
	
	public void setMaterial(com.elmakers.mine.bukkit.api.block.MaterialAndData material) {
		this.material = new MaterialAndData(material);
	}
	
	public void setMaterial(Block block) {
		this.material = new MaterialAndData(block);
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor1() {
		return color != null ? color : color1;
	}
	
	public Color getColor2() {
		return color != null ? color1 : color2;
	}
	
	public void setOffset(float x, float y, float z) {
		offset.setX(x);
		offset.setY(y);
		offset.setZ(z);
	}
	
	public abstract void play();
}
