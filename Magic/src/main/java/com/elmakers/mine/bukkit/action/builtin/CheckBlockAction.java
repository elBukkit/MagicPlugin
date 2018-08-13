package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.Spell;

public class CheckBlockAction extends CheckAction {
    private MaterialSet allowed;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);

        allowed = spell.getController().getMaterialSetManager()
                .fromConfig(parameters.getString("allowed"));
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        MaterialBrush brush = context.getBrush();
        Block block = context.getTargetBlock();
        if (block == null) {
            return false;
        }
        if (allowed != null) {
            if (!allowed.testBlock(block)) return false;
        } else {
            if (brush != null && brush.isErase()) {
                if (!context.hasBreakPermission(block)) {
                    return false;
                }
            } else {
                if (!context.hasBuildPermission(block)) {
                    return false;
                }
            }
            if (!context.isDestructible(block)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}