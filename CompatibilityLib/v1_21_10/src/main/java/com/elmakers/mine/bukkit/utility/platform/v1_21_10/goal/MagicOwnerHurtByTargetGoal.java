package com.elmakers.mine.bukkit.utility.platform.v1_21_10.goal;

import java.lang.reflect.Method;

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
        try {
            this.mob.setTarget(this.target, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
        } catch (Error ignore) {
            // Must be paper?
            try {
                Method setTargetMethod = this.mob.getClass().getMethod("setTarget", LivingEntity.class, EntityTargetEvent.TargetReason.class);
                setTargetMethod.invoke(this.target, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET);
            } catch (Exception e) {
                // Fall back to API
                ((org.bukkit.entity.Mob)(this.mob.getBukkitEntity())).setTarget((org.bukkit.entity.LivingEntity)this.target.getBukkitEntity());
            }
        }        LivingEntity owner = this.tamed.getOwner();
        if (owner != null) {
            this.lastTargetUpdate = owner.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
