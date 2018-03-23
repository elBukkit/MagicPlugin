package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CheckBlockAction extends CompoundAction {
    private MaterialSet allowed;
    
    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);

        allowed = spell.getController().getMaterialSetManager()
                .fromConfig(parameters.getString("allowed"));
    }
    
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
    public SpellResult step(CastContext context) {
        boolean allowed = isAllowed(context);
        ActionHandler actions = getHandler("actions");
        if (actions == null || actions.size() == 0) {
            return allowed ? SpellResult.CAST : SpellResult.STOP;
        }
        
        if (!allowed) {
            return SpellResult.NO_TARGET;
        }
        return startActions();
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}