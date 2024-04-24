package com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal;

import java.util.EnumSet;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

public class MagicFollowOwnerGoal extends MagicOwnerGoal {
    private final double speedModifier;
    private final PathNavigation navigation;
    private final int interval;
    private final double stopDistanceSquared;
    private final double startDistanceSquared;
    private final double teleportDistanceSquared;

    // State
    private int ticksRemaining = 0;

    public MagicFollowOwnerGoal(Platform platform, Mob tamed, double speedModifier, float startDistance, float stopDistance, int interval, ConfigurationSection config) {
        super(platform, tamed);
        this.speedModifier = speedModifier;
        this.navigation = tamed.getNavigation();
        this.startDistanceSquared = startDistance * startDistance;
        this.stopDistanceSquared = stopDistance * stopDistance;
        float teleportDistance = (float)config.getDouble("teleport_distance", 12);
        if (!config.getBoolean("teleport", true)) {
            teleportDistance = 0;
        }
        this.teleportDistanceSquared = teleportDistance * teleportDistance;
        this.interval = interval;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (mob.distanceToSqr(tamed.getOwner()) < startDistanceSquared) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (navigation.isDone()) {
            return false;
        }
        if (tamed.isStay()) {
            return false;
        }
        return mob.distanceToSqr(tamed.getOwner()) > stopDistanceSquared;
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
        this.mob.getLookControl().setLookAt(tamed.getOwner(), 10.0F, this.mob.getMaxHeadXRot());
        if (ticksRemaining-- <= 0) {
            ticksRemaining = this.interval;
            if (!this.mob.isLeashed() && !this.mob.isPassenger()) {
                if (teleportDistanceSquared > 0 && this.mob.distanceToSqr(this.tamed.getOwner()) >= teleportDistanceSquared) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.tamed.getOwner(), this.speedModifier);
                }
            }
        }
    }

    private void teleportToOwner() {
        BlockPos blockPosition = this.tamed.getOwner().blockPosition();
        for (int i = 0; i < 10; ++i) {
            int dx = RandomUtils.getRandomIntInclusive(-3, 3);
            int dy = RandomUtils.getRandomIntInclusive(-1, 1);
            int dz = RandomUtils.getRandomIntInclusive(-3, 3);
            if (this.tryTeleportTo(blockPosition.getX() + dx, blockPosition.getY() + dy, blockPosition.getZ() + dz)) {
                break;
            }
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        LivingEntity owner = tamed.getOwner();
        if (Math.abs(x - owner.getX()) < 2.0D && Math.abs(z - owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.mob.moveTo(x + 0.5D, y, z + 0.5D, this.mob.getYRot(), this.mob.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos blockPosition) {
        // TODO: The PathType enum doesn't seem accessible
        /**
        PathType pathType = WalkNodeEvaluator.getPathTypeStatic(mob, blockPosition.mutable());
        if (pathType != PathType.WALKABLE) {
            return false;
        }
        */

        blockPosition = blockPosition.subtract(this.mob.blockPosition());
        return mob.level().noCollision(this.mob, this.mob.getBoundingBox().move(blockPosition));
    }
}
