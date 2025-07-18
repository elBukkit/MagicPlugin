package com.elmakers.mine.bukkit.utility.platform.v1_21_8.goal;

import java.util.EnumSet;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class MagicOwnerTargetGoal extends TargetGoal {
    protected final MagicTamed tamed;
    protected final Mob mob;

    // State
    protected LivingEntity target;
    protected int lastTargetUpdate;

    public MagicOwnerTargetGoal(Platform platform, Mob mob, Entity entity, boolean see, boolean reach) {
        super(mob, see, reach);
        this.mob = mob;
        this.tamed = new MagicTamed(platform, mob);
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    protected boolean canAttack(LivingEntity newTarget, int newTimestamp) {
        this.target = newTarget;
        if (lastTargetUpdate == newTimestamp) {
            return false;
        }
        if (mob instanceof TamableAnimal) {
            TamableAnimal tamable = (TamableAnimal)mob;
            if (!tamable.wantsToAttack(target, tamed.getOwner())) {
                return false;
            }
        }
        return this.canAttack(this.target, TargetingConditions.DEFAULT);
    }

    @Override
    public boolean canUse() {
        return tamed.canUse();
    }

    @Override
    public void stop() {
        tamed.stop();
    }
}
