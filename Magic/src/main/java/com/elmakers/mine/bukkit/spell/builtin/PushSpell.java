package com.elmakers.mine.bukkit.spell.builtin;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

@Deprecated
public class PushSpell extends UndoableSpell
{
    private static final int DEFAULT_ITEM_MAGNITUDE = 1;
    private static final int DEFAULT_ENTITY_MAGNITUDE = 3;
    private static final int DEFAULT_MAX_ALL_DISTANCE = 20;

    public void forceAll(Entity sourceEntity, double mutliplier, boolean pull, int entityMagnitude, int itemMagnitude, int maxAllDistance, double damage, int fallProtection)
    {
        float maxDistance = maxAllDistance * mage.getRangeMultiplier();
        float maxDistanceSquared = maxDistance * maxDistance;

        List<Entity> entities = getWorld().getEntities();
        for (Entity target : entities)
        {
            if (target == sourceEntity) continue;
            if (!canTarget(target)) continue;

            Mage mage = controller.isMage(target) ? controller.getMage(target) : null;
            if (mage != null && mage.isSuperProtected()) {
                continue;
            }

            if (mage != null && fallProtection > 0) {
                mage.enableFallProtection(fallProtection);
            }

            Location playerLocation = getLocation();
            Location targetLocation = target.getLocation();

            if (playerLocation.distanceSquared(targetLocation) > maxDistanceSquared) continue;

            int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;
            forceEntity(target, mutliplier, playerLocation, targetLocation, magnitude, damage, pull);
            getCurrentTarget().setEntity(target);
        }
    }

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        boolean push = false;
        boolean pull = false;
        Entity sourceEntity = mage.getEntity();

        String typeString = parameters.getString("type", "");
        push = typeString.equals("push");
        pull = typeString.equals("pull");

        double multiplier = parameters.getDouble("size", 1);
        if (push) {
            multiplier *= mage.getDamageMultiplier();
        }
        int count = parameters.getInt("count", 0);

        boolean allowAll = mage.isSuperPowered() || parameters.getBoolean("allow_area", true);
        boolean forceArea = parameters.getBoolean("area", false);
        int itemMagnitude = parameters.getInt("item_force", DEFAULT_ITEM_MAGNITUDE);
        int entityMagnitude = parameters.getInt("entity_force", DEFAULT_ENTITY_MAGNITUDE);
        int maxAllDistance = parameters.getInt("area_range", DEFAULT_MAX_ALL_DISTANCE);
        int fallProtection = parameters.getInt("fall_protection", 0);
        double damage = parameters.getDouble("damage", 0) * mage.getDamageMultiplier();

        if (mage.isSuperPowered()) {
            allowAll = true;
        }

        if
        (
            allowAll
            &&  (forceArea || isLookingDown() || isLookingUp())
        )
        {
            forceAll(sourceEntity, multiplier, pull, entityMagnitude, itemMagnitude, maxAllDistance, damage, fallProtection);
            return SpellResult.ALTERNATE;
        }

        Target directTarget = getTarget();
        List<Target> targets = getAllTargetEntities();

        if (directTarget.hasEntity() && targets.size() == 0)
        {
            targets.add(directTarget);
        }

        if (targets.size() == 0)
        {
            return SpellResult.NO_TARGET;
        }

        int pushed = 0;
        for (Target target : targets) {
            Entity targetEntity = target.getEntity();
            Mage mage = targetEntity != null && controller.isMage(targetEntity)
                    ? controller.getMage(targetEntity)
                    : null;
            if (mage != null && mage.isSuperProtected()) {
                continue;
            }
            if (mage != null && fallProtection > 0) {
                mage.enableFallProtection(fallProtection);
            }
            int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;

            getCurrentTarget().setEntity(targetEntity);
            forceEntity(targetEntity, multiplier, getLocation(), target.getLocation(), magnitude, damage, pull);
            pushed++;
            if (count > 0 && pushed >= count) break;
        }
        return SpellResult.CAST;
    }

    protected void forceEntity(Entity target, double multiplier, Location sourceLocation, Location targetLocation, int magnitude, double damage, boolean pull)
    {
        // Check for protected players (admins, generally...)
        if (isSuperProtected(target)) {
            return;
        }

        if (target instanceof Hanging) {
            return;
        }

        if (target instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)target;
            registerModified(li);
            if (damage > 0) {
                CompatibilityUtils.magicDamage(li, damage, mage.getEntity());
            }
        }

        Location to = pull ? targetLocation : sourceLocation;
        Location from = pull ? sourceLocation : targetLocation;

        registerVelocity(target);
        magnitude = (int)(magnitude * multiplier);
        Vector toVector = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
        Vector fromVector = new Vector(from.getBlockX(), from.getBlockY(), from.getBlockZ());
        Vector forceVector = fromVector;
        forceVector.subtract(toVector);
        if (forceVector.lengthSquared() < 1) {
            forceVector = sourceLocation.getDirection();
            if (pull) {
                forceVector.multiply(-1);
            }
        }
        forceVector.normalize();
        forceVector.multiply(magnitude);
        SafetyUtils.setVelocity(target, forceVector);
    }
}
