package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class CustomProjectileAction extends CompoundAction
{
    private int interval;
    private int lifetime;
    private double speed;
    private int startDistance;
    private String projectileEffectKey;
    private String hitEffectKey;

    private long lastUpdate;
    private long nextUpdate;
    private long deadline;
    private boolean hit = false;
    private Vector velocity = null;
    private DynamicLocation effectLocation = null;
    private Collection<EffectPlay> activeProjectileEffects;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        interval = parameters.getInt("interval", 200);
        lifetime = parameters.getInt("lifetime", 5000);
        speed = parameters.getDouble("speed", 0.1);
        startDistance = parameters.getInt("start", 0);
        projectileEffectKey = parameters.getString("projectile_effects", "projectile");
        hitEffectKey = parameters.getString("hit_effects", "hit");
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
        if (now > deadline)
        {
            hit(context);
        }
        if (hit)
        {
            return super.perform(context);
        }
        nextUpdate = now + interval;

        // Check for initialization required
        Location targetLocation = context.getTargetLocation();
        if (targetLocation == null) {
            targetLocation = context.getEyeLocation().clone();
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
                }
                if (activeProjectileEffects == null) {
                    activeProjectileEffects = new ArrayList<EffectPlay>();
                }
                // Hrm- this is ugly, but I don't want the API to depend on EffectLib.
                if (apiEffectPlayer instanceof com.elmakers.mine.bukkit.effect.EffectPlayer)
                {
                    com.elmakers.mine.bukkit.effect.EffectPlayer effectPlayer = (com.elmakers.mine.bukkit.effect.EffectPlayer)apiEffectPlayer;
                    effectPlayer.setEffectPlayList(activeProjectileEffects);
                    effectPlayer.startEffects(effectLocation, new DynamicLocation(context.getTargetLocation(), context.getTargetEntity()));
                }
            }
        }
        else if (effectLocation != null)
        {
            effectLocation.updateFrom(targetLocation);
        }

        // Advance position, checking for collisions
        long delta = now - lastUpdate;
        lastUpdate = now;
        if (delta > 0) {
            double remainingSpeed = speed * delta / 50;
            while (remainingSpeed > 0) {
                Block block = targetLocation.getBlock();
                if (!block.getChunk().isLoaded()) {
                    hit(context);
                    return SpellResult.PENDING;
                }
                if (!context.isTransparent(block.getType())) {
                    hit(context);
                    return SpellResult.PENDING;
                }

                double partialSpeed = Math.min(0.5, remainingSpeed);
                Vector speedVector = velocity.clone().multiply(partialSpeed);
                remainingSpeed -= 0.5;
                Vector newLocation = targetLocation.toVector().add(speedVector);

                // Skip over same blocks, we increment by 0.5 to try and catch diagonals
                if (newLocation.getBlockX() == targetLocation.getBlockX()
                        && newLocation.getBlockY() == targetLocation.getBlockY()
                        && newLocation.getBlockZ() == targetLocation.getBlockZ()) {
                    remainingSpeed -= 0.5;
                    newLocation = newLocation.add(speedVector);
                    targetLocation.setX(newLocation.getX());
                    targetLocation.setY(newLocation.getY());
                    targetLocation.setZ(newLocation.getZ());
                } else {
                    targetLocation.setX(newLocation.getX());
                    targetLocation.setY(newLocation.getY());
                    targetLocation.setZ(newLocation.getZ());
                }
            }
        }

		return SpellResult.PENDING;
	}

    protected void hit(CastContext context) {
        hit = true;
        if (activeProjectileEffects != null) {
            for (EffectPlay play : activeProjectileEffects) {
                play.cancel();
            }
        }
        context.playEffects(hitEffectKey);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("interval");
        parameters.add("lifetime");
        parameters.add("speed");
        parameters.add("start");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);

        if (parameterKey.equals("speed") || parameterKey.equals("lifetime") ||
            parameterKey.equals("interval") || parameterKey.equals("start")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }
}
