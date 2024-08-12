package com.elmakers.mine.bukkit.utility.platform.v1_21_1.goal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

public class MagicFollowMobGoal extends Goal {
    private final Mob mob;
    private final double speedModifier;
    private final PathNavigation navigation;
    private final int interval;
    private final double radius;
    private final double stopDistanceSquared;
    private final Predicate<LivingEntity> followPredicate;

    // State
    private int ticksRemaining = 0;
    private LivingEntity followingMob;

    public MagicFollowMobGoal(Mob mob, double speedModifier, double radius, float stopDistance, int interval, Class<? extends LivingEntity> followType) {
        this.mob = mob;
        this.radius = radius;
        this.interval = interval;
        this.speedModifier = speedModifier;
        this.navigation = mob.getNavigation();
        this.stopDistanceSquared = stopDistance * stopDistance;
        this.followPredicate = (checkMob) -> checkMob != null && checkMob != mob && followType.isAssignableFrom(checkMob.getClass());
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // I could not figure out how to use followType directly here
        List<? extends LivingEntity> nearby = this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(radius), this.followPredicate);
        for (LivingEntity mob : nearby) {
            if (!mob.isInvisible()) {
                this.followingMob = mob;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (followingMob == null) {
            return false;
        }
        if (navigation.isDone()) {
            return false;
        }
        return mob.distanceToSqr(followingMob) > stopDistanceSquared;
    }

    @Override
    public void start() {
        this.ticksRemaining = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.navigation.stop();
    }

    @Override
    public void tick() {
        if (followingMob != null && !mob.isLeashed()) {
            this.mob.getLookControl().setLookAt(this.followingMob, 10.0F, this.mob.getMaxHeadXRot());
            if (--this.ticksRemaining <= 0) {
                this.ticksRemaining = interval;
                navigation.moveTo(this.followingMob, this.speedModifier);
            }
        }
    }
}
