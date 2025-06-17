package com.elmakers.mine.bukkit.utility.platform.v1_21_6.goal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.utility.StringUtils;

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
        return "Requirements" + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + StringUtils.join(requirementDescriptions, " ") + ChatColor.GRAY + ")";
    }
}
