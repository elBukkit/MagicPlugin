package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public class CheckBlockAction extends CompoundAction {
    private Set<Material> allowed;
    private boolean returnCosts;
    
    private SpellResult failSpellResult;
    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        allowed = spell.getController().getMaterialSet(parameters.getString("allowed"));
        returnCosts = parameters.getBoolean("return_costs", false);
		
        failSpellResult = returnCosts ? SpellResult.CANCELLED : SpellResult.NO_TARGET;
    }
    
    protected boolean isAllowed(CastContext context) {
        MaterialBrush brush = context.getBrush();
        Block block = context.getTargetBlock();
        if (block == null) {
            return false;
        }
        if (allowed != null) {
            if (!allowed.contains(block.getType())) return false;
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
            return failSpellResult;
        }
        return startActions();
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }
}