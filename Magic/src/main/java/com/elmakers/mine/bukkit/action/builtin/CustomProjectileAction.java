package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.tasks.CancelEffectsTask;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.HitboxUtils;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.Targeting;
import com.elmakers.mine.bukkit.utility.TextUtils;

import de.slikey.effectlib.math.VectorTransform;
import de.slikey.effectlib.util.DynamicLocation;
import de.slikey.effectlib.util.VectorUtils;

public class CustomProjectileAction extends CompoundAction
{
    private int interval;
    private int lifetime;
    private int attachDuration;
    private int range;
    private double minRange;
    private double minEntityRange;
    private double minBlockRange;
    private double speed;
    private VectorTransform velocityTransform;
    private double spread;
    private double movementSpread;
    private double maxMovementSpread;
    private int startDistance;
    private String projectileEffectKey;
    private boolean projectileEffectsUseTarget;
    private String hitEffectKey;
    private String missEffectKey;
    private String headshotEffectKey;
    private String tickEffectKey;
    private double gravity;
    private double drag;
    private double tickSize;
    private boolean reorient;
    protected SourceLocation sourceLocation;
    private boolean trackEntity;
    private double trackCursorRange;
    private double trackSpeed;
    private int targetSelfTimeout;
    private Boolean targetSelf;
    private boolean breaksBlocks;
    private double targetBreakables;
    private int targetBreakableSize;
    private boolean bypassBackfire;
    private boolean reverseDirection;
    private boolean hitOnMiss;
    private int blockHitLimit;
    private int entityHitLimit;
    private int reflectLimit;
    private int pitchMin;
    private int pitchMax;
    private boolean ignoreHitEntities;
    private boolean ignoreTargeting;
    private boolean reflectReorient;
    private boolean reflectResetDistanceTraveled;
    private boolean reflectTargetCaster;
    private boolean reflectTrackEntity;
    private boolean hitRequiresEntity;
    private double reflectTrackCursorRange;

    protected Targeting targeting;
    protected Location launchLocation;
    protected long flightTime;
    protected double distanceTravelled;
    protected double distanceTravelledThisTick;
    protected Vector velocity = null;

    private double effectDistanceTravelled;
    private boolean hasTickActions;
    private boolean hasTickEffects;
    private boolean hasStepEffects;
    private boolean hasBlockMissEffects;
    private boolean hasPreHitEffects;
    private Vector attachedOffset;
    private long attachedDeadline;
    private int entityHitCount;
    private int blockHitCount;
    private int reflectCount;
    private boolean missed;
    private long lastUpdate;
    private long nextUpdate;
    private long deadline;
    private long targetSelfDeadline;
    private DynamicLocation effectLocation = null;
    private Collection<EffectPlay> activeProjectileEffects;
    private Queue<PlanStep> plan;
    private Collection<ConfigurationSection> planConfiguration;

    private boolean returnToCaster;
    private Vector returnOffset;
    private Vector returnRelativeOffset;
    private boolean resetTimeOnPathChange;
    private Double returnDistanceAway = null;
    private boolean updateLaunchLocation;
    private Vector previousLocation;
    private boolean projectileFollowPlayer;

    private static class PlanStep {
        public double distance;
        public long time;
        public double returnBuffer;
        public ConfigurationSection parameters;
        public String effectsKey;

        public PlanStep(ConfigurationSection planConfig) {
            distance = planConfig.getDouble("distance");
            time = planConfig.getLong("time");
            effectsKey = planConfig.getString("effects");
            returnBuffer = planConfig.getDouble("return_buffer");
            parameters = planConfig;
        }
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);
        targeting = new Targeting();
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        finishEffects();
    }

    @Override
    protected void addHandlers(Spell spell, ConfigurationSection parameters) {
        super.addHandlers(spell, parameters);
        addHandler(spell, "headshot");
        addHandler(spell, "miss");
        addHandler(spell, "tick");
    }

    public void modifyParameters(ConfigurationSection parameters) {
        gravity = parameters.getDouble("gravity", gravity);
        drag = parameters.getDouble("drag", drag);
        reorient = parameters.getBoolean("reorient", reorient);
        trackEntity = parameters.getBoolean("track_target", trackEntity);
        trackCursorRange = parameters.getDouble("track_range", trackCursorRange);
        trackSpeed = parameters.getDouble("track_speed", trackSpeed);

        if (parameters.contains("velocity_transform")) {
            ConfigurationSection transformParameters = ConfigurationUtils.getConfigurationSection(parameters, "velocity_transform");
            if (transformParameters != null) {
                velocityTransform = new VectorTransform(transformParameters);
            } else if (parameters.contains("velocity_transform")) {
                velocityTransform = null;
            }
        }

        speed = parameters.getDouble("speed", speed);
        speed = parameters.getDouble("velocity", speed * 20);

        // Mainly for legacy purposes, gravity is in terms of original speed.
        gravity *= speed;

        tickSize = parameters.getDouble("tick_size", tickSize);
        ignoreTargeting = parameters.getBoolean("ignore_targeting", ignoreTargeting);
        returnToCaster = parameters.getBoolean("return_to_caster", returnToCaster);
        resetTimeOnPathChange = parameters.getBoolean("reset_time_on_path_change", resetTimeOnPathChange);
        updateLaunchLocation = parameters.getBoolean("update_launch_location", updateLaunchLocation);
        projectileFollowPlayer = parameters.getBoolean("projectile_follow_player", projectileFollowPlayer);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        // Parameters that can be modified by a flight plan need
        // to be reset here.
        gravity = 0;
        drag = 0;
        reorient = false;
        trackEntity = false;
        trackCursorRange = 0;
        trackSpeed = 0;
        velocityTransform = null;
        speed = 1;
        tickSize = 0.5;
        ignoreTargeting = false;
        returnToCaster = false;
        resetTimeOnPathChange = false;
        updateLaunchLocation = false;
        projectileFollowPlayer = false;
        modifyParameters(parameters);

        // These parameters can't be changed mid-flight
        targeting.processParameters(parameters);
        interval = parameters.getInt("interval", 30);
        lifetime = parameters.getInt("lifetime", 8000);
        attachDuration = parameters.getInt("attach_duration", 0);
        reverseDirection = parameters.getBoolean("reverse", false);
        startDistance = parameters.getInt("start", 0);
        range = parameters.getInt("range", 0);
        minEntityRange = parameters.getDouble("min_entity_range",0);
        minBlockRange = parameters.getDouble("min_block_range",0);
        minRange = parameters.getDouble("min_entity_range", Math.max(minEntityRange, minBlockRange));

        if (minRange < Math.max(minEntityRange, minBlockRange)) {
            minRange = Math.max(minEntityRange, minBlockRange);
        }

        projectileEffectKey = parameters.getString("projectile_effects", "projectile");
        projectileEffectsUseTarget = parameters.getBoolean("projectile_effects_use_target", false);
        headshotEffectKey = parameters.getString("headshot_effects", "headshot");
        hitEffectKey = parameters.getString("hit_effects", "hit");
        missEffectKey = parameters.getString("miss_effects", "miss");
        tickEffectKey = parameters.getString("tick_effects", "tick");
        sourceLocation = new SourceLocation(parameters);
        targetSelfTimeout = parameters.getInt("target_self_timeout", 0);
        targetSelf = parameters.contains("target_self") ? parameters.getBoolean("target_self") : null;
        breaksBlocks = parameters.getBoolean("break_blocks", true);
        hitRequiresEntity = parameters.getBoolean("hit_requires_entity", false);
        targetBreakables = parameters.getDouble("target_breakables", 1);
        targetBreakableSize = parameters.getInt("breakable_size", 1);
        bypassBackfire = parameters.getBoolean("bypass_backfire", false);
        spread = parameters.getDouble("spread", 0);
        maxMovementSpread = parameters.getDouble("spread_movement_max", 0);
        movementSpread = parameters.getDouble("spread_movement", 0);
        int hitLimit = parameters.getInt("hit_count", 1);
        entityHitLimit = parameters.getInt("entity_hit_count", hitLimit);
        blockHitLimit = parameters.getInt("block_hit_count", hitLimit);
        ignoreHitEntities = parameters.getBoolean("ignore_hit_entities", true);
        reflectLimit = parameters.getInt("reflect_count", -1);
        pitchMin = parameters.getInt("pitch_min", 90);
        pitchMax = parameters.getInt("pitch_max", -90);

        reflectReorient = parameters.getBoolean("reflect_reorient", false);
        reflectResetDistanceTraveled = parameters.getBoolean("reflect_reset_distance_traveled", true);
        reflectTargetCaster = parameters.getBoolean("reflect_target_caster", true);
        reflectTrackEntity = parameters.getBoolean("reflect_track_target", false);
        reflectTrackCursorRange = parameters.getDouble("reflect_track_range", 0D);
        hitOnMiss = parameters.getBoolean("hit_on_miss", false);

        returnOffset = ConfigurationUtils.getVector(parameters, "return_offset");
        returnRelativeOffset = ConfigurationUtils.getVector(parameters, "return_relative_offset");

        range = (int)(range * context.getMage().getRangeMultiplier());

        // Some parameter tweaks to make sure things are sane
        TargetType targetType = targeting.getTargetType();
        if (targetType == TargetType.NONE) {
            targeting.setTargetType(TargetType.OTHER);
        }

        // Flags to optimize FX calls
        hasTickEffects = context.getEffects(tickEffectKey).size() > 0;
        hasBlockMissEffects = context.getEffects("blockmiss").size() > 0;
        hasStepEffects = context.getEffects("step").size() > 0;
        hasPreHitEffects = context.getEffects("prehit").size() > 0;

        ActionHandler handler = getHandler("tick");
        hasTickActions = handler != null && handler.size() > 0;
        planConfiguration = ConfigurationUtils.getNodeList(parameters, "plan");
    }

    @Override
    public boolean next(CastContext context) {
        return !missed && entityHitCount < entityHitLimit && blockHitCount < blockHitLimit;
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);

        targeting.reset();
        long now = System.currentTimeMillis();
        nextUpdate = 0;
        distanceTravelled = 0;
        lastUpdate = 0;
        deadline =  now + lifetime;
        targetSelfDeadline = targetSelfTimeout > 0 ? now + targetSelfTimeout : 0;
        effectLocation = null;
        velocity = null;
        activeProjectileEffects = null;
        entityHitCount = 0;
        blockHitCount = 0;
        reflectCount = 0;
        attachedDeadline = 0;
        attachedOffset = null;
        missed = false;
        returnDistanceAway = null;

        // This has to be done here, so that the plan is not shared across parallel instances
        if (planConfiguration != null && !planConfiguration.isEmpty()) {
            plan = new ArrayDeque<>();
            for (ConfigurationSection planStepConfig : planConfiguration) {
                plan.add(new PlanStep(planStepConfig));
            }
        } else {
            plan = null;
        }
    }

    @Override
    public SpellResult start(CastContext context) {
        if (movementSpread > 0) {
            double entitySpeed = context.getMage().getVelocity().lengthSquared();
            if (entitySpeed > 0) {
                double movementAmount = Math.min(1.0, entitySpeed / (movementSpread * movementSpread));
                spread += (movementAmount * maxMovementSpread);
                if (context.getMage().getDebugLevel() >= 3) {
                    context.getMage().sendDebugMessage(ChatColor.DARK_RED + " Applying spread of " + ChatColor.RED + spread
                            + ChatColor.DARK_RED + " from speed of " + ChatColor.GOLD + context.getMage().getVelocity().length());
                }
            }
        }
        return super.start(context);
    }

    @Override
    public SpellResult step(CastContext context) {
        long now = System.currentTimeMillis();
        if (now < nextUpdate)
        {
            return SpellResult.PENDING;
        }
        if (attachedDeadline > 0)
        {
            Entity targetEntity = actionContext.getTargetEntity();
            if (attachedOffset != null && targetEntity != null)
            {
                Location targetLocation = targetEntity.getLocation();
                targetLocation.add(attachedOffset);
                actionContext.setTargetLocation(targetLocation);
                if (effectLocation != null)
                {
                    effectLocation.updateFrom(targetLocation);
                }
            }
            if (now > attachedDeadline)
            {
                return finishAttach();
            }
            return SpellResult.PENDING;
        }
        if (now > deadline)
        {
            return miss();
        }
        if (targetSelfDeadline > 0 && now > targetSelfDeadline && actionContext != null)
        {
            targetSelfDeadline = 0;
            actionContext.setTargetsCaster(true);
        }
        nextUpdate = now + interval;

        // Check for initialization required
        // TODO: Move this to start()?
        Location projectileLocation = null;
        if (velocity == null)
        {
            projectileLocation = sourceLocation.getLocation(context).clone();

            /* This feels confusing however...
             * Looking straight down in Minecraft gives a pitch of 90
             * While looking straight up is a pitch of -90
             * We don't want to normalize these values as other functions need the non-normalize numbers.
             * So if the projectile pitch value is found to be higher or lower than the min or max, it's set to the min or max respectively
             */
            if (pitchMin < projectileLocation.getPitch())
            {
                projectileLocation.setPitch(pitchMin);
            }
            else if (pitchMax > projectileLocation.getPitch())
            {
                projectileLocation.setPitch(pitchMax);
            }
            launchLocation = projectileLocation.clone();
            velocity = projectileLocation.getDirection().clone().normalize();

            if (spread > 0) {
                Random random = context.getRandom();
                velocity.setX(velocity.getX() + (random.nextDouble() * spread - spread / 2));
                velocity.setY(velocity.getY() + (random.nextDouble() * spread - spread / 2));
                velocity.setZ(velocity.getZ() + (random.nextDouble() * spread - spread / 2));
                velocity.normalize();
            }

            if (startDistance != 0) {
                projectileLocation.add(velocity.clone().multiply(startDistance));
            }

            if (reverseDirection) {
                velocity = velocity.multiply(-1);
            }

            projectileLocation.setDirection(velocity);
            actionContext.setTargetLocation(projectileLocation);
            actionContext.setTargetEntity(null);
            actionContext.setDirection(velocity);

            // Start up projectile FX
            startProjectileEffects(context, projectileEffectKey);

            if (context.getMage().getDebugLevel() >= 7) {
                context.getMage().sendDebugMessage(ChatColor.BLUE + "Projectile launched from "
                    + TextUtils.printLocation(projectileLocation) + ChatColor.BLUE, 7);
            }
        }
        else
        {
            projectileLocation = actionContext.getTargetLocation();
            if (effectLocation != null)
            {
                effectLocation.updateFrom(projectileLocation);
                effectLocation.setDirection(velocity);
            }
        }

        // Check plan
        if (plan != null && !plan.isEmpty()) {
            PlanStep next = plan.peek();
            if ((next.distance > 0 && distanceTravelled > next.distance) || (next.time > 0 && flightTime > next.time) || (next.returnBuffer > 0 && returnDistanceAway != null && returnDistanceAway < next.returnBuffer))
            {
                plan.remove();
                if (next.parameters != null) {
                    modifyParameters(next.parameters);
                }
                if (next.effectsKey != null) {
                    startProjectileEffects(context, next.effectsKey);
                }
                context.getMage().sendDebugMessage("Changing flight plan at distance " + ((int)distanceTravelled) + " and time " + flightTime, 4);
                if (resetTimeOnPathChange)
                {
                    flightTime = 0;
                }
            }
        }
        if (updateLaunchLocation)
        {
            launchLocation = context.getCastLocation().clone();
        }
        // Advance position
        // We default to 50 ms travel time (one tick) for the first iteration.
        long delta = lastUpdate > 0 ? now - lastUpdate : 50;
        lastUpdate = now;
        flightTime += delta;
        // Apply gravity, drag or other velocity modifiers
        Vector targetVelocity = null;
        if (trackEntity)
        {
            Entity targetEntity = context.getTargetEntity();
            if (targetEntity != null && targetEntity.isValid() && context.canTarget(targetEntity))
            {
                Location targetLocation = targetEntity instanceof LivingEntity
                        ? ((LivingEntity)targetEntity).getEyeLocation()
                        : targetEntity.getLocation();
                targetVelocity = targetLocation.toVector().subtract(projectileLocation.toVector()).normalize();
            }
        }
        else if (trackCursorRange > 0)
        {
            /* We need to first find out where the player is looking and multiply it by how far the player wants the whip to extend
             * Finally after all that, we adjust the velocity of the projectile to go towards the cursor point
             */
            Vector playerCursor = context.getMage().getDirection().clone().normalize();
            Vector targetPoint = playerCursor.multiply(trackCursorRange);
            Vector worldPoint = targetPoint.add(context.getMage().getEyeLocation().clone().toVector());
            Vector projectileOffset = worldPoint.subtract(projectileLocation.clone().toVector());
            targetVelocity = projectileOffset.normalize();
        }
        else if (reorient)
        {
            targetVelocity = context.getMage().getDirection().clone().normalize();
        }
        else
        {
            if (gravity > 0) {
                // Reduce / change speed based on gravity
                velocity.normalize().multiply(speed);
                velocity.setY(velocity.getY() - gravity * delta / 50);
                speed = velocity.length();
                velocity.normalize();
            }
            if (drag > 0) {
                speed -= drag * delta / 50;
                if (speed <= 0) {
                    return miss();
                }
            }
        }

        if (targetVelocity != null)
        {
            if (trackSpeed > 0)
            {
                double steerDistanceSquared = trackSpeed * trackSpeed;
                double distanceSquared = targetVelocity.distanceSquared(velocity);
                if (distanceSquared <= steerDistanceSquared) {
                    velocity = targetVelocity;
                } else {
                    Vector targetDirection = targetVelocity.subtract(velocity).normalize().multiply(steerDistanceSquared);
                    velocity.add(targetDirection);
                }
            }
            else
            {
                velocity = targetVelocity;
            }
            launchLocation.setDirection(velocity);
        }
        if (velocityTransform != null)
        {
            targetVelocity = velocityTransform.get(launchLocation, (double)flightTime / 1000);

            // This is expensive, but necessary for variable speed to work properly
            // with targeting and range-checking
            if (targetVelocity != null) {
                speed = targetVelocity.length();
                if (speed > 0) {
                    targetVelocity.normalize();
                } else {
                    targetVelocity.setX(1);
                }
                velocity = targetVelocity;
            }
        }
        if (returnToCaster)
        {
            Vector targetLocation = context.getMage().getEyeLocation().toVector();

            if (returnOffset != null)
            {
                targetLocation.add(returnOffset);
            }
            if (returnRelativeOffset != null)
            {
                Vector relativeOffset = VectorUtils.rotateVector(returnRelativeOffset, context.getMage().getEyeLocation());
                targetLocation.add(relativeOffset);
            }

            Vector projectileOffset = targetLocation.clone().subtract(projectileLocation.toVector());
            returnDistanceAway = projectileOffset.length();
            velocity = projectileOffset.normalize();
        }

        if (projectileFollowPlayer)
        {
            Vector currentLocation = context.getMage().getLocation().toVector();
            if (previousLocation != null)
            {
                Vector offset = currentLocation.clone().subtract(previousLocation);
                previousLocation = currentLocation;
                velocity = velocity.add(offset);
            }
            else
            {
                previousLocation = currentLocation;
            }
        }

        projectileLocation.setDirection(velocity);

        // Advance targeting to find Entity or Block
        distanceTravelledThisTick = speed * delta / 1000;
        if (range > 0) {
            distanceTravelledThisTick = Math.min(distanceTravelledThisTick, range - distanceTravelled);
        }
        context.addWork((int)Math.ceil(distanceTravelledThisTick));
        Location targetLocation;
        Targeting.TargetingResult targetingResult = Targeting.TargetingResult.MISS;
        Target target = null;

        if (!ignoreTargeting) {
            targeting.start(projectileLocation);
            target = targeting.target(actionContext, distanceTravelledThisTick);
            targetingResult = targeting.getResult();
            targetLocation = target.getLocation();

            boolean keepGoing = distanceTravelled < minRange;
            Location tempLocation = projectileLocation.clone();
            int checkIterations = 0;

            while (keepGoing)
            {
                // TODO if all of these distance() calls are necessary, they should be optimized to distanceSquared()
                if (targetingResult == Targeting.TargetingResult.MISS) {
                    keepGoing = false;
                }
                else if (targetingResult != null && targetLocation.distance(projectileLocation) + distanceTravelled >= minRange) {
                    keepGoing = false;
                }
                else if (targetLocation.distance(projectileLocation) + distanceTravelled >= minEntityRange && targetingResult == Targeting.TargetingResult.ENTITY) {
                    keepGoing = false;
                }
                else if (targetLocation.distance(projectileLocation) + distanceTravelled >= minBlockRange && targetingResult == Targeting.TargetingResult.BLOCK) {
                    keepGoing = false;
                }
                else if (targetLocation.distance(projectileLocation) >= distanceTravelledThisTick) {
                    keepGoing = false;
                }
                else if (checkIterations > 1000) {
                    keepGoing = false;
                }
                else {
                    if (tempLocation.distance(projectileLocation) < targetLocation.distance(projectileLocation)) {
                        tempLocation.add(velocity.clone().multiply(targetLocation.distance(projectileLocation) + 0.1));
                    }
                    else {
                        tempLocation.add(velocity.clone().multiply(0.2));
                    }

                    actionContext.setTargetLocation(tempLocation);
                    actionContext.setTargetEntity(null);
                    actionContext.setDirection(velocity);

                    // TODO: This whole procedure, particularly retargeting, is going to be very costly
                    // This is hopefully an easier way
                    targeting.start(tempLocation);
                    target = targeting.target(actionContext, distanceTravelledThisTick - tempLocation.distance(projectileLocation));
                    targetingResult = targeting.getResult();
                    targetLocation = target.getLocation();
                    checkIterations++;
                }
            }
        }
        if (targetingResult.isMiss()) {
            if (hasBlockMissEffects && target != null) {
                actionContext.setTargetLocation(target.getLocation());
                actionContext.playEffects("blockmiss");
            }

            targetLocation = projectileLocation.clone().add(velocity.clone().multiply(distanceTravelledThisTick));
            if (context.getMage().getDebugLevel() >= 14) {
                context.getMage().sendDebugMessage(ChatColor.DARK_BLUE + "Projectile miss: " + ChatColor.DARK_PURPLE
                        + " at " + TextUtils.printBlock(targetLocation.getBlock()) + " from range of " + distanceTravelledThisTick + " over time " + delta, 14);
            }
        } else {
            if (hasPreHitEffects) {
                actionContext.playEffects("prehit");
            }
            targetLocation = target.getLocation();
            // Debugging
            if (targetLocation == null) {
                targetLocation = projectileLocation;
                context.getLogger().warning("Targeting hit, with no target location: " + targetingResult + " with " + targeting.getTargetType() + " from " + context.getSpell().getName());
            }

            if (context.getMage().getDebugLevel() >= 8) {
                context.getMage().sendDebugMessage(ChatColor.BLUE + "Projectile hit: " + ChatColor.LIGHT_PURPLE + targetingResult.name().toLowerCase()
                    + ChatColor.BLUE + " at " + TextUtils.printBlock(targetLocation.getBlock())
                    + ChatColor.BLUE + " from " + TextUtils.printBlock(projectileLocation.getBlock()) + ChatColor.BLUE + " to "
                    + TextUtils.printVector(targetLocation.toVector()) + ChatColor.BLUE
                    + " from range of " + ChatColor.GOLD + distanceTravelledThisTick + ChatColor.BLUE + " over time " + ChatColor.DARK_PURPLE + delta, 8);
            }
            distanceTravelledThisTick = targetLocation.distance(projectileLocation);
        }
        distanceTravelled += distanceTravelledThisTick;
        effectDistanceTravelled += distanceTravelledThisTick;

        // Max Height check
        int y = targetLocation.getBlockY();
        boolean maxHeight = y >= targetLocation.getWorld().getMaxHeight();
        boolean minHeight = y <= 0;

        if (maxHeight) {
            targetLocation.setY(targetLocation.getWorld().getMaxHeight());
        } else if (minHeight) {
            targetLocation.setY(0);
        }

        if (hasTickEffects && effectDistanceTravelled > tickSize) {
            // Sane limit here
            Vector speedVector = velocity.clone().multiply(tickSize);
            for (int i = 0; i < 256; i++) {
                actionContext.setTargetLocation(projectileLocation);
                actionContext.playEffects(tickEffectKey);

                projectileLocation.add(speedVector);

                effectDistanceTravelled -= tickSize;
                if (effectDistanceTravelled < tickSize) break;
            }
        }

        actionContext.setTargetLocation(targetLocation);
        if (target != null) {
            actionContext.setTargetEntity(target.getEntity());
        }

        if (hasStepEffects) {
            actionContext.playEffects("step");
        }

        if (maxHeight || minHeight) {
            return miss();
        }

        if (range > 0 && distanceTravelled >= range) {
            return miss();
        }

        if (!CompatibilityUtils.isChunkLoaded(targetLocation)) {
            return miss();
        }

        Block block = targetLocation.getBlock();
        if (distanceTravelled < minRange) {
            // TODO : Should this be < ?
            if (distanceTravelled >= minBlockRange && targetingResult == Targeting.TargetingResult.BLOCK) {
                return miss();
            }

            if (distanceTravelled >= minEntityRange && targetingResult == Targeting.TargetingResult.ENTITY) {
                return miss();
            }
        }
        else if (targetingResult == Targeting.TargetingResult.BLOCK) {
            return hitBlock(block);
        } else if (targetingResult == Targeting.TargetingResult.ENTITY) {
            return hitEntity(target);
        }

        if (hasTickActions) {
            return startActions("tick");
        }

        return SpellResult.PENDING;
    }

    protected void reflect(Vector normal, double offset) {
        trackEntity = reflectTrackEntity;
        reorient = reflectReorient;
        if (reflectResetDistanceTraveled) distanceTravelled = 0;
        if (reflectTrackCursorRange >= 0) trackCursorRange = reflectTrackCursorRange;
        if (reflectTargetCaster) actionContext.setTargetsCaster(true);

        // Calculate angle of reflection
        if (normal != null) {
            velocity.multiply(-1);
            velocity = velocity.subtract(normal.multiply(2 * velocity.dot(normal))).normalize();
            velocity.multiply(-1);

            actionContext.getMage().sendDebugMessage(ChatColor.AQUA + "Projectile reflected: " + ChatColor.LIGHT_PURPLE
                + " with normal vector of " + TextUtils.printVector(normal), 4);
        } else {
            actionContext.getMage().sendDebugMessage(ChatColor.AQUA + "Projectile reflected");
        }

        // Offset position slightly to avoid hitting again
        actionContext.setTargetLocation(actionContext.getTargetLocation().add(velocity.clone().multiply(offset)));
        // actionContext.setTargetLocation(targetLocation.add(normal.normalize().multiply(2)));

        actionContext.playEffects("reflect");
        reflectCount++;
    }

    protected SpellResult hitBlock(Block block) {
        boolean continueProjectile = false;
        if ((reflectLimit < 0 || reflectCount < reflectLimit) && !bypassBackfire && actionContext.isReflective(block)) {
            double reflective = actionContext.getReflective(block);
            if (reflective >= 1 || actionContext.getRandom().nextDouble() < reflective) {
                Location targetLocation = actionContext.getTargetLocation();
                Vector normal = CompatibilityUtils.getNormal(block, targetLocation);
                reflect(normal, 0.05);
                continueProjectile = true;
            }
        }
        if (targetBreakables > 0 && breaksBlocks && actionContext.isBreakable(block)) {
            targetBreakables -= targeting.breakBlock(actionContext, block, Math.min(targetBreakableSize, targetBreakables));
            if (targetBreakables > 0) {
                continueProjectile = true;
            }
        }

        actionContext.playEffects("hit_block");

        if (!continueProjectile) {
            blockHitCount++;
        }
        return continueProjectile ? SpellResult.PENDING : (hitRequiresEntity ? miss() : hit());
    }

    protected SpellResult hitEntity(Target target) {
        Entity hitEntity = target.getEntity();
        Location targetLocation = actionContext.getTargetLocation();
        if (hitEntity instanceof Player) {
            Player hitPlayer = (Player)hitEntity;
            Mage targetMage = actionContext.getController().getMage(hitPlayer);
            if (hitPlayer.isBlocking()) {
                double angle = velocity.angle(hitPlayer.getEyeLocation().getDirection().multiply(-1));
                if ((reflectLimit < 0 || reflectCount < reflectLimit) && !bypassBackfire && targetMage.isReflected(angle)) {
                    velocity = hitPlayer.getEyeLocation().getDirection().normalize().multiply(velocity.length());
                    reflect(null, 0.5);
                    return SpellResult.PENDING;
                }
                if (targetMage.isBlocked(angle) && !bypassBackfire) {
                    return miss();
                }
            }
        }

        entityHitCount++;
        if (hitEntity != null && entityHitLimit > 1 && ignoreHitEntities) {
            targeting.ignoreEntity(hitEntity);
        }
        if (hitEntity != null) {
            actionContext.playEffects("hit_entity");

            if (hasActions("headshot") && HitboxUtils.isHeadshot(hitEntity, targetLocation)) {
                actionContext.getMage().sendDebugMessage(ChatColor.GOLD + "   Projectile headshot", 3);
                return headshot();
            }
        }
        return hit();
    }

    protected SpellResult finishAttach() {
        attachedDeadline = 0;
        finishEffects();
        return startActions();
    }

    protected void finishEffects() {
        if (activeProjectileEffects != null) {
            Plugin plugin = actionContext != null ? actionContext.getPlugin() : null;
            final Collection<EffectPlay> cancelEffects = activeProjectileEffects;
            activeProjectileEffects = null;
            if (plugin == null || !plugin.isEnabled()) {
                for (EffectPlay play : cancelEffects) {
                    play.cancel();
                }
            } else {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new CancelEffectsTask(cancelEffects), 1L);
            }
        }
    }

    protected SpellResult attach() {
        attachedDeadline = System.currentTimeMillis() + attachDuration;
        Entity targetEntity = actionContext == null ? null : actionContext.getTargetEntity();
        Location targetLocation = actionContext == null ? null : actionContext.getTargetLocation();
        if (targetEntity != null && targetLocation != null)
        {
            attachedOffset = targetLocation.toVector().subtract(targetEntity.getLocation().toVector());
        }
        if (actionContext != null) actionContext.playEffects(hitEffectKey);
        return SpellResult.PENDING;
    }

    protected SpellResult headshot() {
        finishEffects();
        if (actionContext == null) {
            return SpellResult.NO_ACTION;
        }
        actionContext.playEffects(headshotEffectKey);
        return startActions("headshot");
    }

    protected SpellResult hit() {
        if (attachDuration > 0) {
            return attach();
        }
        finishEffects();
        if (actionContext == null) {
            return SpellResult.NO_ACTION;
        }
        actionContext.playEffects(hitEffectKey);
        if (targetSelfTimeout > 0) {
            actionContext.setTargetsCaster(true);
        } else if (targetSelf != null) {
            actionContext.setTargetsCaster(targetSelf);
        }
        return startActions();
    }

    protected SpellResult miss() {
        missed = true;
        if (hitOnMiss) {
            return hit();
        }
        finishEffects();
        if (actionContext == null) {
            return SpellResult.NO_ACTION;
        }
        actionContext.playEffects(missEffectKey);
        return startActions("miss");
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("interval");
        parameters.add("lifetime");
        parameters.add("speed");
        parameters.add("start");
        parameters.add("gravity");
        parameters.add("drag");
        parameters.add("target_entities");
        parameters.add("track_target");
        parameters.add("spread");
        parameters.add("spread_movement");
        parameters.add("spread_movement_max");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);

        if (parameterKey.equals("speed")
                || parameterKey.equals("lifetime")
                || parameterKey.equals("interval")
                || parameterKey.equals("start")
                || parameterKey.equals("size")
                || parameterKey.equals("gravity")
                || parameterKey.equals("drag")
                || parameterKey.equals("tick_size")
                || parameterKey.equals("spread")
                || parameterKey.equals("spread_movement")
                || parameterKey.equals("spread_movement_max")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("target_entities") || parameterKey.equals("track_target")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        }
    }

    protected void startProjectileEffects(CastContext context, String effectKey) {

        Collection<EffectPlayer> projectileEffects = context.getEffects(effectKey);
        for (EffectPlayer apiEffectPlayer : projectileEffects)
        {
            if (effectLocation == null) {
                effectLocation = new DynamicLocation(actionContext.getTargetLocation());
                effectLocation.setDirection(velocity);
            }
            if (activeProjectileEffects == null) {
                activeProjectileEffects = new ArrayList<>();
            }
            // Hrm- this is ugly, but I don't want the API to depend on EffectLib.
            if (apiEffectPlayer instanceof com.elmakers.mine.bukkit.effect.EffectPlayer)
            {
                com.elmakers.mine.bukkit.effect.EffectPlayer effectPlayer = (com.elmakers.mine.bukkit.effect.EffectPlayer)apiEffectPlayer;
                effectPlayer.setEffectPlayList(activeProjectileEffects);
                if (projectileEffectsUseTarget) {
                    Entity sourceEntity = actionContext.getEntity();
                    DynamicLocation sourceLocation = sourceEntity == null ? new DynamicLocation(actionContext.getLocation()) : new DynamicLocation(sourceEntity);
                    effectPlayer.startEffects(sourceLocation, effectLocation);
                } else {
                    effectPlayer.startEffects(effectLocation, null);
                }
            }
        }
    }
}
