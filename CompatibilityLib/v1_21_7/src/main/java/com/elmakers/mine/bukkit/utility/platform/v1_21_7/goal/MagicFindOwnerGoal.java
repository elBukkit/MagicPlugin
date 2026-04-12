package com.elmakers.mine.bukkit.utility.platform.v1_21_7.goal;

import java.util.List;
import java.util.function.Predicate;

import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class MagicFindOwnerGoal extends MagicOwnerGoal {
    private final double radius;
    private final Predicate<LivingEntity> followPredicate;

    public MagicFindOwnerGoal(Platform platform, Mob mob, double radius, Class<? extends LivingEntity> followType) {
        super(platform, mob);
        this.radius = radius;
        this.followPredicate = (checkMob) -> checkMob != null && checkMob != mob && followType.isAssignableFrom(checkMob.getClass());
    }

    @Override
    public boolean canUse() {
        // We have to clear the owner cache each time to check and see if we still have an owner.
        tamed.stop();
        tamed.checkOwner();
        if (tamed.getOwner() != null) {
            return false;
        }

        double closestSquared = 0;
        LivingEntity closest = null;
        List<? extends LivingEntity> nearby = this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(radius), this.followPredicate);
        for (LivingEntity mob : nearby) {
            double distance = mob.distanceToSqr(this.mob);
            if (closest == null || distance < closestSquared) {
                closest = mob;
                closestSquared = distance;
            }
        }
        if (closest != null) {
            tamed.setOwner(closest);
        }

        // This goal never activates, it's just here to set an owner
        return false;
    }
}
