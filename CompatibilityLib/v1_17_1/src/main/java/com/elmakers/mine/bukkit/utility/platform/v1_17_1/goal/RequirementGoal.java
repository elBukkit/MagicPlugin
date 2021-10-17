package com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal;

import java.util.Collection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;

import net.minecraft.world.entity.ai.goal.Goal;

public class RequirementGoal extends MagicGoal {
    private final Collection<Requirement> requirements;
    private final Mage mage;

    public RequirementGoal(Mage mage, Collection<Goal> goals, boolean interruptable, Collection<Requirement> requirements) {
        super(goals, interruptable);
        this.mage = mage;
        this.requirements = requirements;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && checkRequirements();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && checkRequirements();
    }

    protected boolean checkRequirements() {
        MageController controller = mage.getController();
        return controller.checkRequirements(mage.getContext(), requirements) == null;
    }
}
