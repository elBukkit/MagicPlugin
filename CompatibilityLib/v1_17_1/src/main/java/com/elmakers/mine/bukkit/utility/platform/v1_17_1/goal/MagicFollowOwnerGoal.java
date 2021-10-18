package com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal;

import java.util.EnumSet;

import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class MagicFollowOwnerGoal extends Goal {
    private final Platform platform;
    private final Mob mob;
    private final Entity entity;
    private final LivingEntity owner;
    private final Level level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private final int interval;
    private final double stopDistanceSquared;
    private final double startDistanceSquared;
    private final double teleportDistanceSquared;

    // State
    private int ticksRemaining = 0;

    public MagicFollowOwnerGoal(Platform platform, Mob tamed, Entity entity, LivingEntity owner, double speedModifier, float startDistance, float stopDistance, int interval, ConfigurationSection config) {
        this.platform = platform;
        this.mob = tamed;
        this.entity = entity;
        this.owner = owner;
        this.level = tamed.level;
        this.speedModifier = speedModifier;
        this.navigation = tamed.getNavigation();
        this.startDistanceSquared = startDistance * startDistance;
        this.stopDistanceSquared = stopDistance * stopDistance;
        float teleportDistance = (float)config.getDouble("teleport_distance", 12);
        this.teleportDistanceSquared = teleportDistance * teleportDistance;
        this.interval = interval;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (owner == null || (owner instanceof Player && ((Player)owner).getGameMode() == GameMode.SPECTATOR)) {
            return false;
        }
        if (platform.getEnityMetadataUtils().getBoolean(entity, MagicMetaKeys.STAY)) {
            return false;
        }
        if (mob.distanceToSqr(owner) < startDistanceSquared) {
            return false;
        }
        return true;
    }

    public boolean canContinueToUse() {
        if (navigation.isDone()) {
            return false;
        }
        if (platform.getEnityMetadataUtils().getBoolean(entity, MagicMetaKeys.STAY)) {
            return false;
        }
        return mob.distanceToSqr(owner) > stopDistanceSquared;
    }

    public void start() {
        this.ticksRemaining = 0;
    }

    public void stop() {
        this.navigation.stop();
    }

    public void tick() {
        this.mob.getLookControl().setLookAt(owner, 10.0F, (float)this.mob.getMaxHeadXRot());
        if (ticksRemaining-- <= 0) {
            ticksRemaining = this.interval;
            if (!this.mob.isLeashed() && !this.mob.isPassenger()) {
                if (this.mob.distanceToSqr(this.owner) >= teleportDistanceSquared) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }
    }

    private void teleportToOwner() {
        BlockPos blockPosition = this.owner.blockPosition();
        for (int i = 0; i < 10; ++i) {
            int dx = RandomUtils.getRandomIntInclusive(-3, 3);
            int dy = RandomUtils.getRandomIntInclusive(-1, 1);
            int dz = RandomUtils.getRandomIntInclusive(-3, 3);
            if (this.maybeTeleportTo(blockPosition.getX() + dx, blockPosition.getY() + dy, blockPosition.getZ() + dz)) {
                break;
            }
        }
    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        if (Math.abs((double)x - this.owner.getX()) < 2.0D && Math.abs((double)z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.mob.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, this.mob.getYRot(), this.mob.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos blockPosition) {
        BlockPathTypes pathType = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, blockPosition.mutable());
        if (pathType != BlockPathTypes.WALKABLE) {
            return false;
        }

        blockPosition = blockPosition.e(this.mob.blockPosition());
        return this.level.noCollision(this.mob, this.mob.getBoundingBox().move(blockPosition));
    }
}
