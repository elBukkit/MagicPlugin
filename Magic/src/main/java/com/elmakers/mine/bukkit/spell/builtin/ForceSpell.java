package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class ForceSpell extends TargetingSpell
{
    LivingEntity targetEntity = null;
    private Color effectColor = null;

    private static final int DEFAULT_MAGNITUDE = 3;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        effectColor = mage.getEffectColor();
        if (effectColor == null) {
            effectColor = Color.fromRGB(Integer.parseInt(parameters.getString("effect_color", "FF0000"), 16));
        }

        if (targetEntity != null)
        {
            Location location = getLocation();
            World targetWorld = targetEntity.getWorld();
            if (!targetEntity.isValid() || targetEntity.isDead())
            {
                releaseTarget();
            }
            else if (targetWorld == null || location == null || !targetWorld.getName().equals(location.getWorld().getName()))
            {
                releaseTarget();
            }
            else if (location.distanceSquared(targetEntity.getLocation()) > getMaxRangeSquared())
            {
                releaseTarget();
            }

            // Check for protected Mages
            if (targetEntity != null && controller.isMage(targetEntity)) {
                Mage targetMage = controller.getMage(targetEntity);
                // Check for protected players (admins, generally...)
                if (isSuperProtected(targetMage)) {
                    releaseTarget();
                }
            }
        }

        if (targetEntity == null) {
            Target target = getTarget();

            if (!target.hasEntity() || !(target.getEntity() instanceof LivingEntity))
            {
                return SpellResult.NO_TARGET;
            }

            releaseTarget();
            LivingEntity checkTarget = (LivingEntity)target.getEntity();

            // Check for protected Mages
            if (checkTarget != null && controller.isMage(checkTarget)) {
                Mage targetMage = controller.getMage(checkTarget);

                // Check for protected players
                if (isSuperProtected(targetMage)) {
                    return SpellResult.NO_TARGET;
                }
            }

            selectTarget(checkTarget);
            activate();
            return SpellResult.TARGET_SELECTED;
        }

        double multiplier = parameters.getDouble("size", 1);

        int magnitude = parameters.getInt("entity_force", DEFAULT_MAGNITUDE);
        forceEntity(targetEntity, multiplier, magnitude);
        return SpellResult.CAST;
    }

    protected void forceEntity(Entity target, double multiplier, int magnitude)
    {
        magnitude = (int)(magnitude * multiplier);
        Vector forceVector = mage.getLocation().getDirection();
        forceVector.normalize();
        forceVector.multiply(magnitude);
        SafetyUtils.setVelocity(target, forceVector);
    }

    protected void selectTarget(LivingEntity entity) {
        releaseTarget();
        targetEntity = entity;
        getCurrentTarget().setEntity(entity);
    }

    protected void releaseTarget() {
        targetEntity = null;
    }

    @Override
    public void onDeactivate() {
        releaseTarget();
    }

    @Override
    public boolean onCancelSelection()
    {
        return targetEntity != null;
    }
}
