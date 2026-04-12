package com.elmakers.mine.bukkit.utility.platform.v1_21_9.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import org.bukkit.craftbukkit.v1_21_R6.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R6.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;

// Based on net.minecraft.world.entity.ai.goal.TemptGoal, fixed to not throw an error for mobs missing the tempt_range attribute
public class MagicTemptGoal extends Goal {
    private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight();
    private final TargetingConditions targetingConditions;
    protected final PathfinderMob mob;
    private final double speedModifier;
    private double px;
    private double py;
    private double pz;
    private double pRotX;
    private double pRotY;
    private double temptRange;
    @Nullable
    protected LivingEntity b;
    private int calmDown;
    private boolean isRunning;
    private final Predicate<ItemStack> items;
    private final boolean canScare;

    public MagicTemptGoal(PathfinderMob entitycreature, double d0, Predicate<ItemStack> predicate, boolean flag, double temptRange) {
        this.mob = entitycreature;
        this.speedModifier = d0;
        this.items = predicate;
        this.canScare = flag;
        this.temptRange = temptRange;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.targetingConditions = TEMPT_TARGETING.copy().selector((entityliving, worldserver) -> {
            return this.shouldFollow(entityliving);
        });
    }

    public boolean canUse() {
        if (this.calmDown > 0) {
            --this.calmDown;
            return false;
        } else {
            double mobTempRange = mob.getAttributes().hasAttribute(Attributes.TEMPT_RANGE) ? this.mob.getAttributeValue(Attributes.TEMPT_RANGE) : this.temptRange;
            this.b = getServerLevel(this.mob).getNearestPlayer(this.targetingConditions.range(mobTempRange), this.mob);
            if (this.b != null) {
                EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.mob, this.b, TargetReason.TEMPT);
                if (event.isCancelled()) {
                    return false;
                }

                this.b = event.getTarget() == null ? null : ((CraftLivingEntity)event.getTarget()).getHandle();
            }

            return this.b != null;
        }
    }

    private boolean shouldFollow(LivingEntity entityliving) {
        return this.items.test(entityliving.getMainHandItem()) || this.items.test(entityliving.getOffhandItem());
    }

    public boolean canContinueToUse() {
        if (this.canScare()) {
            if (this.mob.distanceToSqr(this.b) < 36.0) {
                if (this.b.distanceToSqr(this.px, this.py, this.pz) > 0.010000000000000002) {
                    return false;
                }

                if (Math.abs((double)this.b.getXRot() - this.pRotX) > 5.0 || Math.abs((double)this.b.getYRot() - this.pRotY) > 5.0) {
                    return false;
                }
            } else {
                this.px = this.b.getX();
                this.py = this.b.getY();
                this.pz = this.b.getZ();
            }

            this.pRotX = (double)this.b.getXRot();
            this.pRotY = (double)this.b.getYRot();
        }

        return this.canUse();
    }

    protected boolean canScare() {
        return this.canScare;
    }

    public void start() {
        this.px = this.b.getX();
        this.py = this.b.getY();
        this.pz = this.b.getZ();
        this.isRunning = true;
    }

    public void stop() {
        this.b = null;
        this.mob.getNavigation().stop();
        this.calmDown = reducedTickDelay(100);
        this.isRunning = false;
    }

    public void tick() {
        this.mob.getLookControl().setLookAt(this.b, (float)(this.mob.getMaxHeadYRot() + 20), (float)this.mob.getMaxHeadXRot());
        if (this.mob.distanceToSqr(this.b) < 6.25) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.b, this.speedModifier);
        }

    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
