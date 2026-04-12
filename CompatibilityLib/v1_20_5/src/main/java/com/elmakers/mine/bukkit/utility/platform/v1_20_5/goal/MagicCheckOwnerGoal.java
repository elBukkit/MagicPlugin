package com.elmakers.mine.bukkit.utility.platform.v1_20_5.goal;

import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class MagicCheckOwnerGoal extends MagicOwnerGoal {
    public MagicCheckOwnerGoal(Platform platform, Mob tamed) {
        super(platform, tamed);
    }

    @Override
    public boolean canUse() {
        tamed.checkOwner();
        LivingEntity owner = tamed.getOwner();
        if (owner == null || owner.isDeadOrDying()) {
            tamed.setOwner(null);
            tamed.stop();
        }
        // Note that this goal never becomes active, it is just here to clear the owner
        return false;
    }
}
