package com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal;

import java.util.Collection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;

import net.minecraft.world.entity.ai.goal.Goal;

public class MagicRequirementGoal extends Goal {
    private final Collection<Requirement> requirements;
    private final Mage mage;
    private final Collection<Goal> goals;
    private final boolean interruptable;
    private Goal currentGoal;

    public MagicRequirementGoal(Mage mage, Collection<Goal> goals, boolean interruptable, Collection<Requirement> requirements) {
        this.mage = mage;
        this.goals = goals;
        this.interruptable = interruptable;
        this.requirements = requirements;
    }

    @Override
    public boolean canUse() {
        // Note that goals will perform important logic in their canUse and other
        // methods that look like they would just be queries
        // So we need to call them even if we don't care about the result.
        for (Goal goal : goals) {
            if (goal.canUse() && currentGoal == null) {
                currentGoal = goal;
            }
        }
        return currentGoal != null && checkRequirements();
    }

    @Override
    public boolean canContinueToUse() {
        boolean interrupt = false;
        boolean continuing = false;
        for (Goal goal : goals) {
            if (goal == currentGoal) {
                boolean canContinue = goal.canContinueToUse();
                if (canContinue) {
                    continuing = true;
                } else {
                    interrupt = true;
                }
            } else {
                // A higher-priority goal can interrupt
                boolean canUse = goal.canUse();
                if (canUse && !continuing && (currentGoal == null || currentGoal.isInterruptable())) {
                    interrupt = true;
                }
            }
            if (interrupt) {
                return false;
            }
        }
        return checkRequirements();
    }

    protected boolean checkRequirements() {
        MageController controller = mage.getController();
        return controller.checkRequirements(mage.getContext(), requirements) == null;
    }

    @Override
    public boolean isInterruptable() {
        for (Goal goal : goals) {
            if (!goal.isInterruptable() && goal == currentGoal) {
                return false;
            }
        }
        return interruptable;
    }

    @Override
    public void start() {
        if (currentGoal != null) {
            currentGoal.start();
        }
    }

    @Override
    public void stop() {
        if (currentGoal != null) {
            currentGoal.stop();
            currentGoal = null;
        }
    }

    @Override
    public void tick() {
        if (currentGoal != null) {
            currentGoal.tick();
        }
    }
}
