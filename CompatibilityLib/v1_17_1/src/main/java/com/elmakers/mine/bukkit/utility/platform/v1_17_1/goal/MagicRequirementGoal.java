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
    private final boolean force;
    private final boolean interruptable;

    public MagicRequirementGoal(Mage mage, Collection<Goal> goals, boolean force, boolean interruptable, Collection<Requirement> requirements) {
        this.mage = mage;
        this.goals = goals;
        this.force = force;
        this.interruptable = interruptable;
        this.requirements = requirements;
    }

    @Override
    public boolean canUse() {
        // Note that goals will perform important logic in their canUse and other
        // methods that look like they would just be queries
        // So we need to call them even if we don't care about the result.
        for (Goal goal : goals) {
            if (!goal.canUse() && !force) {
                return false;
            }
        }
        return checkRequirements();
    }

    @Override
    public boolean canContinueToUse() {
        for (Goal goal : goals) {
            if (!goal.canContinueToUse() && !force) {
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
            if (!goal.isInterruptable() && !force) {
                return false;
            }
        }
        return interruptable;
    }

    @Override
    public void start() {
        for (Goal goal : goals) {
            goal.start();
        }
    }

    @Override
    public void stop() {
        for (Goal goal : goals) {
            goal.stop();
        }
    }

    @Override
    public void tick() {
        for (Goal goal : goals) {
            goal.tick();
        }
    }
}
