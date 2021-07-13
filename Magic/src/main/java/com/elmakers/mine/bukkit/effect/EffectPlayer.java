package com.elmakers.mine.bukkit.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
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
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.effect.EffectContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.configuration.TranslatingConfigurationSection;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.tasks.PlayEffectTask;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.util.DynamicLocation;
import de.slikey.effectlib.util.ParticleOptions;

public abstract class EffectPlayer implements com.elmakers.mine.bukkit.api.effect.EffectPlayer {
    private static final String EFFECT_BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.effect.builtin";
    public static boolean ENABLE_VANILLA_SOUNDS = true;
    public static boolean ENABLE_CUSTOM_SOUNDS = true;
    public static int PARTICLE_RANGE = 32;

    public static boolean initialize(Plugin plugin, Logger logger) {
        effectLib = EffectLibManager.initialize(plugin, logger);
        return effectLib != null;
    }

    public static void ignorePlayer(Player player, boolean ignore) {
        if (effectLib != null) {
            effectLib.ignorePlayer(player, ignore);
        }
    }

    public static void debugEffects(boolean debug) {
        if (effectLib != null) {
            effectLib.enableDebug(debug);
        }
    }

    public static void showStackTraces(boolean debug) {
        if (effectLib != null) {
            effectLib.enableStackTraces(debug);
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
    protected Logger logger;
    protected String logContext;

    private DynamicLocation origin;
    private DynamicLocation target;
    private DynamicLocation selection;
    private Vector originOffset;
    private Vector targetOffset;
    private Vector originRelativeOffset;
    private Vector targetRelativeOffset;
    private Visibility visibility = Visibility.ALL;

    // These are ignored by the Trail type, need multi-inheritance :\
    protected boolean playAtOrigin = true;
    protected boolean playAtTarget = false;
    protected boolean targetIsSelection = false;
    protected boolean originIsSelection = false;
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
    protected String radiusOverride = null;
    protected float particleData = 0f;
    protected float particleXOffset = 0.3f;
    protected float particleYOffset = 0.3f;
    protected float particleZOffset = 0.3f;
    protected int particleCount = 1;
    protected float particleSize = 1;
    protected int arrivalTime;

    protected boolean requireEntity = false;
    protected boolean requireTargetEntity = false;
    protected boolean noTargetEntity = false;
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

    @Nullable
    private Logger getLogger() {
        if (this.logger == null && this.plugin != null) {
            this.logger = this.plugin.getLogger();
        }
        return this.logger;
    }

    private void warn(String warning) {
        Logger logger = getLogger();
        if (logger != null) {
            logger.warning(warning);
        }
    }

    public static Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> loadEffects(Plugin plugin, ConfigurationSection root, String key) {
        return loadEffects(plugin, root, key, null, null, null);
    }

    public static Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> loadEffects(Plugin plugin, ConfigurationSection root, String key, Logger logger, String logContext, ConfigurationSection parameterMap) {
        List<com.elmakers.mine.bukkit.api.effect.EffectPlayer> players = new ArrayList<>();
        Collection<ConfigurationSection> effectNodes = ConfigurationUtils.getNodeList(root, key);
        if (effectNodes != null)
        {
            for (ConfigurationSection effectValues : effectNodes)
            {
                String effectClass = effectValues.getString("class", "EffectSingle");
                try {
                    if (!effectClass.contains(".")) {
                        if (!effectClass.startsWith("Effect")) {
                            effectClass = "Effect" + effectClass;
                        }
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
                    player.load(plugin, effectValues, logger, logContext, parameterMap);
                    players.add(player);
                } catch (ClassNotFoundException unknown) {
                    if (logger != null) {
                        logger.warning("Unknown effect class in " + logContext + ": " + effectClass);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (logger != null) {
                        logger.warning("Error creating effect class in " + logContext + ": " + effectClass + " " + ex.getMessage());
                    }
                }
            }
        }

        return players;
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

        Logger logger = getLogger();
        broadcastSound = configuration.getBoolean("sound_broadcast", true);
        useParticleOverride = configuration.getString("particle_override", null);
        useColorOverride = configuration.getString("color_override", null);
        radiusOverride = configuration.getString("radius_override", null);
        originOffset = ConfigurationUtils.getVector(configuration, "origin_offset",  ConfigurationUtils.getVector(configuration, "offset", null, logger, logContext), logger, logContext);
        targetOffset = ConfigurationUtils.getVector(configuration, "target_offset", null, logger, logContext);
        originRelativeOffset = ConfigurationUtils.getVector(configuration, "relative_offset", null, logger, logContext);
        targetRelativeOffset = ConfigurationUtils.getVector(configuration, "relative_target_offset", null, logger, logContext);
        delayTicks = configuration.getInt("delay", delayTicks) * 20 / 1000;
        material1 = ConfigurationUtils.getMaterialAndData(configuration, "material");
        if (configuration.isBoolean("color") && !configuration.getBoolean("color")) {
            useColor = false;
        } else {
            color1 = ConfigurationUtils.getColor(configuration, "color", null);
            color2 = ConfigurationUtils.getColor(configuration, "color2", null);
            if (color2 == null) {
                color2 = ConfigurationUtils.getColor(configuration, "to_color", null);
            }
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
        boolean enableVanillaSounds = configuration.getBoolean("enable_vanilla_sounds", ENABLE_VANILLA_SOUNDS);
        if (!enableVanillaSounds && sound != null && !sound.isCustom()) {
            sound = null;
        }
        boolean enableCustomSounds = configuration.getBoolean("enable_custom_sounds", ENABLE_CUSTOM_SOUNDS);
        if (!enableCustomSounds && sound != null && sound.isCustom()) {
            sound = null;
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
            typeName = CompatibilityLib.getCompatibilityUtils().convertParticle(typeName);
            try {
                particleType = Particle.valueOf(typeName.toUpperCase());
            } catch (Exception ignored) {
            }
            if (particleType == null) {
                warn("Unknown particle type " + typeName);
            } else {
                particleData = (float)configuration.getDouble("particle_data", particleData);
                particleData = (float)configuration.getDouble("particle_speed", particleData);
                particleXOffset = (float)configuration.getDouble("particle_offset", particleXOffset);
                particleYOffset = (float)configuration.getDouble("particle_offset", particleYOffset);
                particleZOffset = (float)configuration.getDouble("particle_offset", particleZOffset);
                particleXOffset = (float)configuration.getDouble("particle_offset_x", particleXOffset);
                particleYOffset = (float)configuration.getDouble("particle_offset_y", particleYOffset);
                particleZOffset = (float)configuration.getDouble("particle_offset_z", particleZOffset);
                particleCount = configuration.getInt("particle_count", particleCount);
                arrivalTime = configuration.getInt("particle_arrival_time", arrivalTime);
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
        noTargetEntity = configuration.getBoolean("requires_no_entity_target", false);
        sourceLocation = new SourceLocation(configuration);
        targetLocation = new SourceLocation(configuration, "target_location", false);
        sampleTarget = configuration.getString("sample", "").equalsIgnoreCase("target");
    }

    public void load(Plugin plugin, ConfigurationSection configuration, Logger logger, String logContext, ConfigurationSection parameterMap) {
        this.logContext = logContext;
        this.logger = logger;
        this.parameterMap = parameterMap;
        if (!(configuration instanceof TranslatingConfigurationSection)) {
            configuration = TranslatingConfigurationSection.getWrapped(configuration);
        }
        ((TranslatingConfigurationSection)configuration).setParameterMap(parameterMap);
        load(plugin, configuration);
        ((TranslatingConfigurationSection)configuration).setParameterMap(null);
    }

    public void setLocationType(String locationType) {
        originIsSelection = false;
        playAtAllTargets = false;
        playAtOrigin = false;
        playAtTarget = false;
        if (locationType.equals("target")) {
            playAtTarget = true;
        } else if (locationType.equals("origin") || locationType.equals("source")) {
            playAtOrigin = true;
        } else if (locationType.equals("both")) {
            playAtTarget = true;
            playAtOrigin = true;
        } else if (locationType.equals("targets")) {
            playAtTarget = true;
            playAtAllTargets = true;
        } else if (locationType.equals("selection") || locationType.equals("selection_to_origin")) {
            playAtTarget = true;
            targetIsSelection = true;
        } else if (locationType.equals("origin_to_selection") || locationType.equals("source_to_selection")) {
            playAtOrigin = true;
            targetIsSelection = true;
        } else if (locationType.equals("target_to_selection")) {
            playAtTarget = true;
            originIsSelection = true;
        } else if (locationType.equals("selection_to_target")) {
            playAtOrigin = true;
            originIsSelection = true;
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
        if (target != null && !CompatibilityLib.getCompatibilityUtils().isChunkLoaded(target)) {
            target = null;
        }
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
        playEffect(getDynamicOrigin(), getDynamicTarget());
    }

    protected void playEffect(DynamicLocation origin, DynamicLocation target) {
        if (requireTargetEntity && (target == null || target.getEntity() == null)) {
            return;
        }
        if (noTargetEntity && (target != null && target.getEntity() != null)) {
            return;
        }
        if (playAtOrigin && origin != null) {
            performEffects(origin, target);
        }
        if (playAtTarget && target != null) {
            performEffects(target, origin);
        }
    }

    @Override
    public void validate() {
        if (effectLib != null && effectLibConfig != null) {
            ((TranslatingConfigurationSection)effectLibConfig).setParameterMap(parameterMap);
            effectLib.validate(effectLibConfig, this, null, null, parameterMap, logContext);
            ((TranslatingConfigurationSection)effectLibConfig).setParameterMap(null);
        }
    }

    public static void displayParticle(Particle particle, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, float size, Color color, Material material, byte materialData, double range) {
        effectLib.displayParticle(particle, center, offsetX, offsetY, offsetZ, speed, amount, size, color, material, materialData, range);
    }

    public void displayParticle(Particle particle, ParticleOptions options, Location center, double range) {
        Player targetPlayer = null;
        switch (getVisibility()) {
            case TARGET:
                if (target != null && target.getEntity() instanceof Player) {
                    targetPlayer = (Player)target.getEntity();
                }
                if (targetPlayer == null) {
                    return;
                }
                break;
            case ORIGIN:
                if (origin != null && origin.getEntity() instanceof Player) {
                    targetPlayer = (Player)origin.getEntity();
                }
                if (targetPlayer == null) {
                    return;
                }
                break;
            default:
                break;
        }
        List<Player> targetPlayers = null;
        if (targetPlayer != null) {
            targetPlayers = new ArrayList<>();
            targetPlayers.add(targetPlayer);
        }
        effectLib.displayParticle(particle, options, center, range, targetPlayers);
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

    public String getRadiusOverrideName() {
        return radiusOverride;
    }

    public double getDistance(Location location1, Location location2) {
        // This makes cross-world trails (e.g. Repair, Backup) work
        if (location1 == null || location2 == null) return 0;
        if (!location1.getWorld().equals(location2.getWorld())) {
            location1 = location1.clone();
            location1.setWorld(location2.getWorld());
        }
        return location1.distance(location2);
    }

    public double getRadius() {
        if (targetIsSelection || originIsSelection) {
            if (selection == null) return 0;
            if (originIsSelection) {
                if (target == null) return 0;
                return getDistance(selection.getLocation(), target.getLocation());
            }
            if (origin == null) return 0;
            return getDistance(selection.getLocation(), origin.getLocation());
        }
        if (origin == null || target == null) return 0;
        return getDistance(origin.getLocation(), target.getLocation());
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
        this.origin = origin == null || origin.getLocation() == null ? null : origin;
        this.target = target == null || target.getLocation() == null ? null : target;

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
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new PlayEffectTask(this), delayTicks);
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

    public void startPlay() {
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
        DynamicLocation origin = getDynamicOrigin();
        return origin == null ? null : origin.getLocation();
    }

    @Nullable
    public Location getTarget() {
        DynamicLocation target = getDynamicTarget();
        return target == null ? null : target.getLocation();
    }

    public DynamicLocation getDynamicTarget() {
        if (targetIsSelection) {
            return selection;
        }
        return target;
    }

    public DynamicLocation getDynamicOrigin() {
        if (originIsSelection) {
            return selection;
        }
        return origin;
    }

    @Nullable
    public Location getSelection() {
        return selection == null ? null : selection.getLocation();
    }

    public void setOrigin(Location location) {
        if (location == null) {
            origin = null;
            return;
        }
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
        if (location == null) {
            target = null;
            return;
        }
        if (target == null) {
            target = new DynamicLocation(location);
        } else {
            Location targetLocation = target.getLocation();
            targetLocation.setX(location.getX());
            targetLocation.setY(location.getY());
            targetLocation.setZ(location.getZ());
        }
    }

    @Override
    public void setSelection(Location location) {
        if (location == null) {
            selection = null;
            return;
        }
        selection = new DynamicLocation(location);
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
            Iterator<EffectPlay> iterator = currentEffects.iterator();
            while (iterator.hasNext()) {
                EffectPlay play = iterator.next();
                if (play.isPlayer(this)) {
                    play.cancel();
                    iterator.remove();
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void performEffects(DynamicLocation source, DynamicLocation target) {
        Location sourceLocation = source == null ? null : source.getLocation();
        if (sourceLocation == null) return;
        Entity sourceEntity = source == null ? null : source.getEntity();
        if (requireEntity && sourceEntity == null) return;

        if (effectLib != null && effectLibConfig != null) {
            EffectLibPlay play = effectLib.play(effectLibConfig, this, source, target, parameterMap, logContext);
            if (currentEffects != null && play != null)
            {
                play.setPlayer(this);
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
                sound.play(plugin, getLogger(), sourceLocation);
            } else if (sourceEntity != null) {
                sound.play(plugin, getLogger(), sourceEntity);
            }
        }
        if (fireworkEffect != null) {
            EffectUtils.spawnFireworkEffect(plugin.getServer(), sourceLocation, fireworkEffect, fireworkPower, fireworkSilent);
        }

        if (particleType != null) {
            Particle useEffect = overrideParticle(particleType);
            Material material = getWorkingMaterial().getMaterial();
            Short data = getWorkingMaterial().getData();
            ParticleOptions options = new ParticleOptions(particleXOffset, particleYOffset, particleZOffset, particleData, particleCount, particleSize, getColor1(), getColor2(), arrivalTime, material, data == null ? 0 : (byte)(short)data);
            options.target = target;
            displayParticle(useEffect, options, sourceLocation, PARTICLE_RANGE);
        }
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

    @Override
    public boolean originIsSelection() {
        return originIsSelection;
    }

    @Override
    public boolean targetIsSelection() {
        return targetIsSelection;
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
