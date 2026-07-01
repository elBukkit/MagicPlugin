package com.elmakers.mine.bukkit.utility.platform.v1_21_9.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class SpinGoal extends Goal {
    private final Mob mob;
    private final float degrees;

    public SpinGoal(Mob mob, float degrees) {
        this.mob = mob;
        this.degrees = degrees;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        mob.snapTo(mob.blockPosition(), mob.getYRot() + degrees, mob.getXRot());
    }
}
