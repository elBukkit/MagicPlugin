package com.elmakers.mine.bukkit.requirements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.magic.MageContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProcessor;

public class RequirementsController implements RequirementsProcessor {
    private final MageController controller;

    public RequirementsController(MageController controller) {
        this.controller = controller;
    }

    @Override
    public boolean checkRequirement(@Nonnull MageContext context, @Nonnull Requirement requirement) {
        MagicRequirement check = new MagicRequirement(controller, requirement);
        return check.checkRequirement(context);
    }

    @Nullable
    @Override
    public String getRequirementDescription(@Nonnull MageContext context, @Nonnull Requirement requirement) {
        MagicRequirement check = new MagicRequirement(controller, requirement);
        return check.getRequirementDescription(context);
    }
}
