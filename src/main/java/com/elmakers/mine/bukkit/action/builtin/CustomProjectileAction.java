package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CustomProjectileAction extends CompoundAction
{
    private int interval;
    private int lifetime;
    private double speed;
    private int startDistance;
    private String projectileEffectKey;
    private String hitEffectKey;
    private String tickEffectKey;
    private boolean targetEntities;
    private double radius;
    private double gravity;
    private double drag;
    private double tickSize;
    private boolean reorient;
    private boolean useWandLocation;
    private boolean useEyeLocation;

    private boolean hasTickEffects;
    private long lastUpdate;
    private long nextUpdate;
    private long deadline;
    private boolean hit = false;
    private Vector velocity = null;
    private DynamicLocation effectLocation = null;
    private Collection<EffectPlay> activeProjectileEffects;

    private class CandidateEntity {
        public final Entity entity;
        public final BoundingBox bounds;

        public CandidateEntity(Entity entity) {
            this.entity = entity;
            this.bounds = CompatibilityUtils.getHitbox(entity).expand(radius);
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        interval = parameters.getInt("interval", 1);
        lifetime = parameters.getInt("lifetime", 5000);
        speed = parameters.getDouble("speed", 0.1);
        startDistance = parameters.getInt("start", 0);
        projectileEffectKey = parameters.getString("projectile_effects", "projectile");
        hitEffectKey = parameters.getString("hit_effects", "hit");
        tickEffectKey = parameters.getString("tick_effects", "tick");
        targetEntities = parameters.getBoolean("target_entities", true);
        radius = parameters.getDouble("size", 1) / 2;
        gravity = parameters.getDouble("gravity", 0);
        drag = parameters.getDouble("drag", 0);
        tickSize = parameters.getDouble("tick_size", 0.5);
        hasTickEffects = context.getEffects(tickEffectKey).size() > 0;
        reorient = parameters.getBoolean("reorient", false);
        useWandLocation = parameters.getBoolean("use_wand_location", true);
        useEyeLocation = parameters.getBoolean("use_eye_location", true);
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        long now = System.currentTimeMillis();
        nextUpdate = 0;
        lastUpdate = now;
        deadline =  now + lifetime;
        hit = false;
        effectLocation = null;
        velocity = null;
        activeProjectileEffects = null;
    }

	@Override
	public SpellResult perform(CastContext context) {
        long now = System.currentTimeMillis();
        if (now < nextUpdate)
        {
            return SpellResult.PENDING;
        }
        if (hit)
        {
            return super.perform(context);
        }
        if (now > deadline)
        {
            return hit(context);
        }
        nextUpdate = now + interval;

        // Check for initialization required
        Location targetLocation = context.getTargetLocation();
        if (targetLocation == null) {
            if (useWandLocation) {
                targetLocation = context.getWandLocation().clone();
            } else if (useEyeLocation) {
                targetLocation = context.getEyeLocation().clone();
            } else {
                targetLocation = context.getLocation().clone();
            }
            context.setTargetLocation(targetLocation);
        }
        if (velocity == null)
        {
            velocity = context.getDirection().clone().normalize();
            if (startDistance != 0) {
                targetLocation.add(velocity.clone().multiply(startDistance));
            }

            // Start up projectile FX
            Collection<EffectPlayer> projectileEffects = context.getEffects(projectileEffectKey);
            for (EffectPlayer apiEffectPlayer : projectileEffects)
            {
                if (effectLocation == null) {
                    effectLocation = new DynamicLocation(targetLocation);
                    effectLocation.setDirection(velocity);
                }
                if (activeProjectileEffects == null) {
                    activeProjectileEffects = new ArrayList<EffectPlay>();
                }
                // Hrm- this is ugly, but I don't want the API to depend on EffectLib.
                if (apiEffectPlayer instanceof com.elmakers.mine.bukkit.effect.EffectPlayer)
                {
                    com.elmakers.mine.bukkit.effect.EffectPlayer effectPlayer = (com.elmakers.mine.bukkit.effect.EffectPlayer)apiEffectPlayer;
                    effectPlayer.setEffectPlayList(activeProjectileEffects);
                    effectPlayer.startEffects(effectLocation, null);
                }
            }
        }
        else if (effectLocation != null)
        {
            effectLocation.updateFrom(targetLocation);
            effectLocation.setDirection(velocity);
        }

        // Advance position, checking for collisions
        long delta = now - lastUpdate;
        lastUpdate = now;

        // Apply gravity and drag
        if (reorient)
        {
            velocity = context.getDirection().clone().normalize();
        }
        else
        {
            if (gravity > 0) {
                velocity.setY(velocity.getY() - gravity * delta / 50);
            }
            if (drag > 0) {
                double size = velocity.length();
                size = size - drag * delta / 50;
                if (size <= 0) {
                    return hit(context);
                }
                velocity.normalize().multiply(size);
            }
        }

        // Compute incremental speed movement
        double remainingSpeed = speed * delta / 50;
        List<CandidateEntity> candidates = null;
        if (radius >= 0 && targetEntities) {
            Entity sourceEntity = context.getEntity();
            candidates = new ArrayList<CandidateEntity>();
            double boundSize = Math.ceil(remainingSpeed) * radius + 2;
            List<Entity> nearbyEntities = CompatibilityUtils.getNearbyEntities(targetLocation, boundSize, boundSize, boundSize);
            for (Entity entity : nearbyEntities)
            {
                if ((context.getTargetsCaster() || entity != sourceEntity) && context.canTarget(entity))
                {
                    candidates.add(new CandidateEntity(entity));
                }
            }

            if (candidates.isEmpty())
            {
                candidates = null;
            }
        }

        // Put a sane limit on the number of iterations here
        for (int i = 0; i < 256; i++) {
            // Play tick FX
            if (hasTickEffects) {
                context.setTargetLocation(targetLocation);
                context.playEffects(tickEffectKey);
            }

            // Check for entity collisions first
            Vector targetVector = targetLocation.toVector();
            if (candidates != null) {
                for (CandidateEntity candidate : candidates) {
                    if (candidate.bounds.contains(targetVector)) {
                        context.setTargetEntity(candidate.entity);
                        return hit(context);
                    }
                }
            }

            int y = targetLocation.getBlockY();
            if (y >= targetLocation.getWorld().getMaxHeight() || y <= 0) {
                return hit(context);
            }
            Block block = targetLocation.getBlock();
            if (!block.getChunk().isLoaded()) {
                return hit(context);
            }
            if (!context.isTransparent(block.getType())) {
                return hit(context);
            }

            double partialSpeed = Math.min(tickSize, remainingSpeed);
            Vector speedVector = velocity.clone().multiply(partialSpeed);
            remainingSpeed -= tickSize;
            Vector newLocation = targetLocation.toVector().add(speedVector);

            // Skip over same blocks, we increment by 0.5 (by default) to try and catch diagonals
            if (newLocation.getBlockX() == targetLocation.getBlockX()
                    && newLocation.getBlockY() == targetLocation.getBlockY()
                    && newLocation.getBlockZ() == targetLocation.getBlockZ()) {
                remainingSpeed -= tickSize;
                newLocation = newLocation.add(speedVector);
                targetLocation.setX(newLocation.getX());
                targetLocation.setY(newLocation.getY());
                targetLocation.setZ(newLocation.getZ());

                if (hasTickEffects) {
                    context.setTargetLocation(targetLocation);
                    context.playEffects(tickEffectKey);
                }
            } else {
                targetLocation.setX(newLocation.getX());
                targetLocation.setY(newLocation.getY());
                targetLocation.setZ(newLocation.getZ());
            }

            if (remainingSpeed <= 0) break;
        }

		return SpellResult.PENDING;
	}

    protected SpellResult hit(CastContext context) {
        hit = true;
        if (activeProjectileEffects != null) {
            for (EffectPlay play : activeProjectileEffects) {
                play.cancel();
            }
        }
        context.playEffects(hitEffectKey);
        return super.perform(context);
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
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);

        if (parameterKey.equals("speed") || parameterKey.equals("lifetime") ||
            parameterKey.equals("interval") || parameterKey.equals("start") || parameterKey.equals("size") ||
            parameterKey.equals("gravity") || parameterKey.equals("drag") || parameterKey.equals("tick_size")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("target_entities")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        }
    }
}
