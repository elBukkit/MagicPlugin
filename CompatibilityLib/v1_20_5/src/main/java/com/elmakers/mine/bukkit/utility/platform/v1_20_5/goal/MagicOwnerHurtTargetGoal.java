package com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;

import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class MagicOwnerHurtTargetGoal extends MagicOwnerTargetGoal {

    public MagicOwnerHurtTargetGoal(Platform platform, Mob mob, Entity entity, boolean see, boolean reach) {
        super(platform, mob, entity, see, reach);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        return canAttack(tamed.getOwner().getLastHurtMob(), tamed.getOwner().getLastHurtMobTimestamp());
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
        LivingEntity owner = this.tamed.getOwner();
        if (owner != null) {
            this.lastTargetUpdate = owner.getLastHurtMobTimestamp();
        }

        super.start();
    }
}
