package com.elmakers.mine.bukkit.utility.platform.v1_21_0.goal;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;

import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class MagicOwnerHurtByTargetGoal extends MagicOwnerTargetGoal {

    public MagicOwnerHurtByTargetGoal(Platform platform, Mob mob, Entity entity, boolean see, boolean reach) {
        super(platform, mob, entity, see, reach);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        return canAttack(tamed.getOwner().getLastHurtByMob(), tamed.getOwner().getLastHurtByMobTimestamp());
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
        LivingEntity owner = this.tamed.getOwner();
        if (owner != null) {
            this.lastTargetUpdate = owner.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
