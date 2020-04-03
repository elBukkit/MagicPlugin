package com.elmakers.mine.bukkit.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.effect.EffectContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.util.DynamicLocation;

public abstract class EffectPlayer implements com.elmakers.mine.bukkit.api.effect.EffectPlayer {
    private static final String EFFECT_BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.effect.builtin";
    public static int PARTICLE_RANGE = 32;

    public static boolean initialize(Plugin plugin) {
        effectLib = EffectLibManager.initialize(plugin);
        return effectLib != null;
    }

    public static void debugEffects(boolean debug) {
        if (effectLib != null) {
            effectLib.enableDebug(debug);
        }
    }

    public static void setParticleRange(int range) {
        PARTICLE_RANGE = range;
        if (effectLib != null) {
            effectLib.setParticleRange(range);
        }
    }

    private static Map<String, Class<?>> effectClasses = new HashMap<>();
    private static EffectLibManager effectLib = null;
    private ConfigurationSection effectLibConfig = null;
    private Collection<EffectPlay> currentEffects = null;

    public static boolean SOUNDS_ENABLED = true;

    protected Plugin plugin;

    private DynamicLocation origin;
    private DynamicLocation target;
    private Vector originOffset;
    private Vector targetOffset;
    private Vector originRelativeOffset;
    private Vector targetRelativeOffset;
    private Visibility visibility = Visibility.ALL;

    // These are ignored by the Trail type, need multi-inheritance :\
    protected boolean playAtOrigin = true;
    protected boolean playAtTarget = false;
    protected boolean playAtAllTargets = false;

    protected Color color = null;
    protected MaterialAndData material;

    protected int delayTicks = 0;

    protected MaterialAndData material1;
    protected Color color1 = null;
    protected Color color2 = null;
    protected boolean useColor = true;

    protected EntityEffect entityEffect = null;

    protected Effect effect = null;
    protected Integer effectData = null;

    protected SoundEffect sound = null;
    protected boolean broadcastSound = true;

    protected boolean hasFirework = false;
    protected FireworkEffect.Type fireworkType;
    protected int fireworkPower = 1;
    protected Boolean fireworkFlicker;
    protected boolean fireworkSilent;

    protected FireworkEffect fireworkEffect;

    protected Particle particleType = null;
    protected Particle particleOverride = null;
    protected String useParticleOverride = null;
    protected String useColorOverride = null;
    protected float particleData = 0f;
    protected float particleXOffset = 0.3f;
    protected float particleYOffset = 0.3f;
    protected float particleZOffset = 0.3f;
    protected int particleCount = 1;
    protected float particleSize = 1;

    protected boolean requireEntity = false;
    protected boolean requireTargetEntity = false;
    protected boolean sampleTarget = false;
    protected SourceLocation sourceLocation = null;
    protected SourceLocation targetLocation = null;

    protected float scale = 1.0f;

    protected ConfigurationSection parameterMap = null;

    public EffectPlayer() {
    }

    public EffectPlayer(Plugin plugin) {
        this.plugin = plugin;
    }

    private void warn(String warning) {
        if (plugin != null) {
            plugin.getLogger().warning(warning);
        }
    }

    public void load(Plugin plugin, ConfigurationSection configuration) {
        this.plugin = plugin;

        if (effectLib != null && configuration.contains("effectlib")) {
            effectLibConfig = ConfigurationUtils.getConfigurationSection(configuration, "effectlib");
            if (effectLibConfig == null) {
                Object rawConfig = configuration.get("effectlib");
                warn("Could not load effectlib node of type " + rawConfig.getClass());
            }
        } else {
            effectLibConfig = null;
        }

        broadcastSound = configuration.getBoolean("sound_broadcast", true);
        useParticleOverride = configuration.getString("particle_override", null);
        useColorOverride = configuration.getString("color_override", null);
        originOffset = ConfigurationUtils.getVector(configuration, "origin_offset",  ConfigurationUtils.getVector(configuration, "offset"));
        targetOffset = ConfigurationUtils.getVector(configuration, "target_offset");
        originRelativeOffset = ConfigurationUtils.getVector(configuration, "relative_offset");
        targetRelativeOffset = ConfigurationUtils.getVector(configuration, "relative_target_offset");
        delayTicks = configuration.getInt("delay", delayTicks) * 20 / 1000;
        material1 = ConfigurationUtils.getMaterialAndData(configuration, "material");
        if (configuration.isBoolean("color") && !configuration.getBoolean("color")) {
            useColor = false;
        } else {
            color1 = ConfigurationUtils.getColor(configuration, "color", null);
            color2 = ConfigurationUtils.getColor(configuration, "color2", null);
        }

        if (configuration.contains("effect")) {
            String effectName = configuration.getString("effect");
            try {
                effect = Effect.valueOf(effectName.toUpperCase());
            } catch (Exception ignored) {
            }
            if (effect == null) {
                warn("Unknown effect type " + effectName);
            } else {
                effectData = ConfigurationUtils.getInteger(configuration, "effect_data", effectData);
            }
        }

        if (configuration.contains("entity_effect")) {
            String effectName = configuration.getString("entity_effect");
            try {
                entityEffect = EntityEffect.valueOf(effectName.toUpperCase());
            } catch (Exception ignored) {
            }
            if (entityEffect == null) {
                warn("Unknown entity effect type " + effectName);
            }
        }

        if (configuration.contains("sound")) {
            sound = new SoundEffect(configuration.getString("sound"));
        } else if (configuration.contains("custom_sound")) {
            sound = new SoundEffect(configuration.getString("custom_sound"));
        }

        if (sound != null) {
            sound.setVolume((float)configuration.getDouble("sound_volume", sound.getVolume()));
            sound.setPitch((float)configuration.getDouble("sound_pitch", sound.getPitch()));
            sound.setRange(configuration.getInt("sound_range", sound.getRange()));
        }

        if (configuration.contains("firework") || configuration.contains("firework_power")) {
            hasFirework = true;
            fireworkType = null;
            if (configuration.contains("firework")) {
                String typeName = configuration.getString("firework");
                try {
                    fireworkType = FireworkEffect.Type.valueOf(typeName.toUpperCase());
                } catch (Exception ignored) {
                }
                if (fireworkType == null) {
                    warn("Unknown firework type " + typeName);
                }
            }

            fireworkPower = configuration.getInt("firework_power", fireworkPower);
            fireworkFlicker = ConfigurationUtils.getBoolean(configuration, "firework_flicker", fireworkFlicker);
            fireworkSilent = configuration.getBoolean("firework_silent", true);
        }
        if (configuration.contains("particle")) {
            String typeName = configuration.getString("particle");
            try {
                particleType = Particle.valueOf(typeName.toUpperCase());
            } catch (Exception ignored) {
            }
            if (particleType == null) {
                warn("Unknown particle type " + typeName);
            } else {
                particleData = (float)configuration.getDouble("particle_data", particleData);
                particleData = (float)configuration.getDouble("particle_speed", particleData);
                particleXOffset = (float)configuration.getDouble("particle_offset_x", particleXOffset);
                particleYOffset = (float)configuration.getDouble("particle_offset_y", particleYOffset);
                particleZOffset = (float)configuration.getDouble("particle_offset_z", particleZOffset);
                particleCount = configuration.getInt("particle_count", particleCount);
                particleSize = (float)configuration.getDouble("particle_size", particleSize);
            }
        }

        String visibilityType = configuration.getString("visibility");
        if (visibilityType != null) {
            try {
                visibility = Visibility.valueOf(visibilityType.toUpperCase());
            } catch (Exception ex) {
                warn("Invalid visibility type: " + visibilityType);
            }
        }

        setLocationType(configuration.getString("location", "origin"));
        requireEntity = configuration.getBoolean("requires_entity", false);
        requireTargetEntity = configuration.getBoolean("requires_entity_target", false);
        sourceLocation = new SourceLocation(configuration);
        targetLocation = new SourceLocation(configuration, "target_location", false);
        sampleTarget = configuration.getString("sample", "").equalsIgnoreCase("target");
    }

    public void setLocationType(String locationType) {
        if (locationType.equals("target")) {
            playAtOrigin = false;
            playAtTarget = true;
            playAtAllTargets = false;
        } else if (locationType.equals("origin")) {
            playAtTarget = false;
            playAtOrigin = true;
            playAtAllTargets = false;
        } else if (locationType.equals("both")) {
            playAtTarget = true;
            playAtOrigin = true;
            playAtAllTargets = false;
        } else if (locationType.equals("targets")) {
            playAtTarget = true;
            playAtOrigin = false;
            playAtAllTargets = true;
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

    @Override
    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    public void setEntityEffect(EntityEffect entityEffect) {
        this.entityEffect = entityEffect;
    }

    public void setParticleType(Particle particleType) {
        this.particleType = particleType;
    }

    @Override
    public void setParticleOverride(String particleType) {
        if (particleType == null || particleType.isEmpty()) {
            this.particleOverride = null;
            return;
        }
        try {
            this.particleOverride = Particle.valueOf(particleType.toUpperCase());
        } catch (Exception ex) {
            warn("Error setting particle override: " + ex.getMessage());
            this.particleOverride = null;
        }
    }
    @Override
    public void setEffectData(int data) {
        this.effectData = data;
    }

    @SuppressWarnings("deprecation")
    protected MaterialAndData getWorkingMaterial() {
        Location target = getTarget();
        if (sampleTarget && target != null) {
            return new MaterialAndData(target.getBlock().getType(), target.getBlock().getData());
        }
        if (material1 != null) return material1;
        MaterialAndData result = material;
        Location origin = getOrigin();
        if (result == null && target != null) {
            result = new MaterialAndData(target.getBlock().getType(), target.getBlock().getData());
        } else if (result == null && origin != null) {
            result = new MaterialAndData(origin.getBlock().getType(), origin.getBlock().getData());
        } else if (result == null) {
            result = new MaterialAndData(Material.AIR);
        }

        return result;
    }

    protected void playEffect() {
        playEffect(origin, target);
    }

    protected void playEffect(DynamicLocation origin, DynamicLocation target) {
        if (requireTargetEntity && (target == null || target.getEntity() == null)) {
            return;
        }
        if (playAtOrigin && origin != null) {
            performEffects(origin, target);
        }
        if (playAtTarget && target != null) {
            performEffects(target, origin);
        }
    }

    @SuppressWarnings("deprecation")
    protected void performEffects(DynamicLocation source, DynamicLocation target) {
        Location sourceLocation = source == null ? null : source.getLocation();
        if (sourceLocation == null) return;
        Entity sourceEntity = source == null ? null : source.getEntity();
        if (requireEntity && sourceEntity == null) return;

        if (effectLib != null && effectLibConfig != null) {
            EffectLibPlay play = effectLib.play(effectLibConfig, this, source, target, parameterMap);
            if (currentEffects != null && play != null)
            {
                currentEffects.add(play);
            }
        }
        if (effect != null) {
            int data = effectData == null ? 0 : effectData;
            if ((effect == Effect.STEP_SOUND) && effectData == null) {
                Material material = getWorkingMaterial().getMaterial();

                // Check for potential bad materials, this can get really hairy (client crashes)
                if (!material.isSolid()) {
                    return;
                }
                sourceLocation.getWorld().playEffect(sourceLocation, effect, material);
            } else {
                sourceLocation.getWorld().playEffect(sourceLocation, effect, data);
            }
        }
        if (entityEffect != null && sourceEntity != null) {
            sourceEntity.playEffect(entityEffect);
        }
        if (sound != null) {
            if (broadcastSound) {
                sound.play(plugin, sourceLocation);
            } else if (sourceEntity != null) {
                sound.play(plugin, sourceEntity);
            }
        }
        if (fireworkEffect != null) {
            EffectUtils.spawnFireworkEffect(plugin.getServer(), sourceLocation, fireworkEffect, fireworkPower, fireworkSilent);
        }

        if (particleType != null) {
            Particle useEffect = overrideParticle(particleType);
            Material material = getWorkingMaterial().getMaterial();
            Short data = getWorkingMaterial().getData();
            displayParticle(useEffect, sourceLocation, particleXOffset, particleYOffset, particleZOffset, particleData, particleCount, particleSize, getColor1(), material, data == null ? 0 : (byte)(short)data, PARTICLE_RANGE);
        }
    }

    public static void displayParticle(Particle particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, float size, Color color, Material material, byte materialData, double range) {
        effectLib.displayParticle(particle, center, offsetX, offsetY, offsetZ, speed, amount, size, color, material, materialData, range);
    }

    public Particle overrideParticle(Particle particle) {
        return useParticleOverride != null && !useParticleOverride.isEmpty() && particleOverride != null ? particleOverride : particle;
    }

    public String getParticleOverrideName() {
        return useParticleOverride;
    }

    public String getColorOverrideName() {
        return useColorOverride;
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

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void setSound(Sound sound) {
        this.sound = new SoundEffect(sound);
    }

    @Override
    public void setSound(Sound sound, float volume, float pitch) {
        this.sound = new SoundEffect(sound);
        this.sound.setVolume(volume);
        this.sound.setPitch(pitch);
    }

    @Override
    public void setDelayTicks(int ticks) {
        delayTicks = ticks;
    }

    @Override
    public void start(Entity originEntity, Entity targetEntity) {
        startEffects(new DynamicLocation(originEntity), new DynamicLocation(targetEntity));
    }

    @Override
    public void start(Location origin, Entity originEntity, Location target, Entity targetEntity) {
        startEffects(new DynamicLocation(origin, originEntity), new DynamicLocation(target, targetEntity));
    }

    @Override
    public void start(Location origin, Entity originEntity, Location target, Entity targetEntity, Collection<Entity> targets) {
        if (playsAtAllTargets()) {
            start(origin, originEntity, targets);
        } else {
            start(origin, originEntity, target, targetEntity);
        }
    }

    public void start(Location origin, Entity originEntity, Collection<Entity> targets) {
        if (targets == null || targets.size() == 0) return;

        DynamicLocation source = new DynamicLocation(origin, originEntity);
        for (Entity targetEntity : targets)
        {
            if (targetEntity == null) continue;
            // This is not really going to work out well for any Effect type but Single!
            // TODO : Perhaps re-visit?
            DynamicLocation target = new DynamicLocation(targetEntity);
            startEffects(source, target);
        }
    }

    @Override
    public void start(Location origin, Location target) {
        startEffects(new DynamicLocation(origin), new DynamicLocation(target));
    }

    @Override
    public void start(EffectContext context) {
        context.trackEffects(this);
        Location source = getSourceLocation(context);
        Location target = getSourceLocation(context);
        setColor(context.getEffectColor());
        setParticleOverride(context.getEffectParticle());
        start(source, context.getEntity(), target, context.getTargetEntity());
    }

    public void startEffects(DynamicLocation origin, DynamicLocation target) {
        // Kinda hacky, but makes cross-world trails (e.g. Repair, Backup) work
        Location targetLocation = target == null ? null : target.getLocation();
        Location originLocation = origin == null ? null : origin.getLocation();
        if (targetLocation != null && originLocation != null && !originLocation.getWorld().equals(targetLocation.getWorld())) {
            targetLocation.setWorld(originLocation.getWorld());
        }
        this.origin = origin;
        this.target = target;

        if (originRelativeOffset != null && this.origin != null) {
            this.origin.addRelativeOffset(originRelativeOffset);
        }
        if (targetRelativeOffset != null && this.target != null) {
            this.target.addRelativeOffset(targetRelativeOffset);
        }

        if (hasFirework) {
            fireworkEffect = getFireworkEffect(getColor1(), getColor2(), fireworkType, fireworkFlicker, false);
        } else {
            fireworkEffect = null;
        }

        // Should I let EffectLib handle delay?
        if (delayTicks > 0 && plugin != null) {
            final EffectPlayer player = this;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.startPlay();
                }
            }, delayTicks);
        } else {
            startPlay();
        }
    }

    protected void checkLocations() {
        if (origin != null) {
            if (originOffset != null) {
                origin.addOffset(originOffset);
            }
        }
        if (target != null) {
            if (targetOffset != null) {
                target.addOffset(targetOffset);
            }
        }
    }

    protected void startPlay() {
        // Generate a target location for compatibility if none exists.
        checkLocations();
        play();
    }

    protected Vector getDirection() {
        Location origin = this.getOrigin();
        Location target = this.getTarget();
        if (origin == null) return new Vector(0, 1, 0);
        Vector direction = target == null ? origin.getDirection() : target.toVector().subtract(origin.toVector());
        return direction.normalize();
    }

    @Override
    public void setMaterial(com.elmakers.mine.bukkit.api.block.MaterialAndData material) {
        this.material = material == null ? null : new MaterialAndData(material);
    }

    @Override
    public void setMaterial(Block block) {
        if (block == null) {
            this.material =  null;
        } else {
            this.material = new MaterialAndData(block);
        }
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Nullable
    public Color getColor1() {
        return useColor ? (color1 != null ? color1 : color) : null;
    }

    @Nullable
    public Color getColor2() {
        return useColor ? (color2 != null ? color2 : color) : null;
    }

    public abstract void play();

    @Nullable
    public Location getOrigin() {
        return origin == null ? null : origin.getLocation();
    }

    @Nullable
    public Location getTarget() {
        return target == null ? null : target.getLocation();
    }

    public void setOrigin(Location location) {
        if (origin == null) {
            origin = new DynamicLocation(location);
        } else {
            Location originLocation = origin.getLocation();
            originLocation.setX(location.getX());
            originLocation.setY(location.getY());
            originLocation.setZ(location.getZ());
        }
    }

    public void setTarget(Location location) {
        if (target == null) {
            target = new DynamicLocation(location);
        } else {
            Location targetLocation = target.getLocation();
            targetLocation.setX(location.getX());
            targetLocation.setY(location.getY());
            targetLocation.setZ(location.getZ());
        }
    }

    @Nullable
    public Entity getOriginEntity() {
        return origin == null ? null : origin.getEntity();
    }

    @Nullable
    public Entity getTargetEntity() {
        return target == null ? null : target.getEntity();
    }

    @Override
    public void cancel() {
        if (currentEffects != null) {
            for (EffectPlay effect : currentEffects) {
                effect.cancel();
            }
            currentEffects.clear();
        }
    }

    public static Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> loadEffects(Plugin plugin, ConfigurationSection root, String key) {
        List<com.elmakers.mine.bukkit.api.effect.EffectPlayer> players = new ArrayList<>();
        Collection<ConfigurationSection> effectNodes = ConfigurationUtils.getNodeList(root, key);
        if (effectNodes != null)
        {
            for (ConfigurationSection effectValues : effectNodes)
            {
                String effectClass = effectValues.getString("class", "EffectSingle");
                try {
                    if (!effectClass.contains(".")) {
                        effectClass = EFFECT_BUILTIN_CLASSPATH + "." + effectClass;
                    }
                    Class<?> genericClass = effectClasses.get(effectClass);
                    if (genericClass == null) {
                        genericClass = Class.forName(effectClass);
                        effectClasses.put(effectClass, genericClass);
                    }
                    if (!EffectPlayer.class.isAssignableFrom(genericClass)) {
                        throw new Exception("Must extend EffectPlayer");
                    }

                    @SuppressWarnings("unchecked")
                    Class<? extends EffectPlayer> playerClass = (Class<? extends EffectPlayer>)genericClass;
                    EffectPlayer player = playerClass.getDeclaredConstructor().newInstance();
                    player.load(plugin, effectValues);
                    players.add(player);
                } catch (ClassNotFoundException unknown) {
                    if (plugin != null) {
                        plugin.getLogger().warning("Unknown effect class: " + effectClass);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (plugin != null) {
                        plugin.getLogger().warning("Error creating effect class: " + effectClass + " " + ex.getMessage());
                    }
                }
            }
        }

        return players;
    }

    @Override
    public void setEffectPlayList(Collection<EffectPlay> plays) {
        this.currentEffects = plays;
    }

    @Nullable
    @Override
    public Location getSourceLocation(@Nonnull EffectContext context)  {
        return sourceLocation.getLocation(context);
    }

    @Nullable
    @Override
    public Location getTargetLocation(@Nonnull EffectContext context)  {
        return targetLocation.getLocation(context);
    }

    @Override
    @Deprecated
    public void setParameterMap(Map<String, String> map) {
        this.parameterMap = ConfigurationUtils.toStringConfiguration(map);
    }

    @Override
    public void setParameterMap(ConfigurationSection map) {
        this.parameterMap = map;
    }

    @Override
    public boolean playsAtOrigin() {
        return playAtOrigin;
    }

    @Override
    public boolean playsAtTarget() {
        return playAtTarget;
    }

    @Override
    public boolean playsAtAllTargets() {
        return playAtAllTargets;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    @Deprecated
    public boolean shouldUseBlockLocation() {
        return targetLocation.shouldUseBlockLocation();
    }

    @Override
    @Deprecated
    public boolean shouldUseHitLocation() {
        return targetLocation.shouldUseHitLocation();
    }

    @Override
    @Deprecated
    public boolean shouldUseWandLocation() {
        return sourceLocation.shouldUseWandLocation();
    }

    @Override
    @Deprecated
    public boolean shouldUseCastLocation() {
        return sourceLocation.shouldUseCastLocation();
    }

    @Override
    @Deprecated
    public boolean shouldUseEyeLocation() {
        return sourceLocation.shouldUseEyeLocation();
    }
}
