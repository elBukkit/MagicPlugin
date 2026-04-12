package com.elmakers.mine.bukkit.utility.platform.v1_21_4.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;

public class MagicPanicGoal extends PanicGoal {
    private final int panicTime;
    private final int calmTime;
    private final boolean interruptable;

    // State
    private long lastPanic;

    public MagicPanicGoal(PathfinderMob mob, double speed, int panicTime, int calmTime, boolean interruptable) {
        super(mob, speed);
        this.panicTime = panicTime;
        this.calmTime = calmTime;
        this.interruptable = interruptable;
    }

    @Override
    public boolean isInterruptable() {
        return interruptable;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getLastHurtByMob() == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        long endPanic = lastPanic + panicTime;
        if (now > endPanic && now < endPanic + calmTime) {
            return false;
        }
        if (this.mob.isOnFire()) {
            return super.canUse();
        }

        return this.findRandomPosition();
    }

    @Override
    public void start() {
        super.start();
        this.lastPanic = System.currentTimeMillis();
    }

    @Override
    public boolean canContinueToUse() {
        if (System.currentTimeMillis() > lastPanic + panicTime) {
            return false;
        }
        return super.canContinueToUse();
    }
}
