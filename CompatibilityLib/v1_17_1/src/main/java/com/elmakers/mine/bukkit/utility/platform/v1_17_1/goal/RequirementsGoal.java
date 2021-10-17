package com.elmakers.mine.bukkit.utility.platform.v1_17_1.goal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;

import net.minecraft.world.entity.ai.goal.Goal;

public class RequirementsGoal extends MagicGoal {
    private final Collection<Requirement> requirements;
    private final Mage mage;

    public RequirementsGoal(Mage mage, Collection<Goal> goals, boolean interruptable, Collection<Requirement> requirements) {
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

    @Override
    protected String getDescription() {
        List<String> requirementDescriptions = new ArrayList<>();
        for (Requirement requirement : requirements) {
            requirementDescriptions.add(requirement.toString());
        }
        return "Requirements(" + StringUtils.join(requirementDescriptions, " ") + "]";
    }
}
