package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public class CheckBlockAction extends BaseSpellAction {
    private Set<Material> allowed;
    
    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        allowed = spell.getController().getMaterialSet(parameters.getString("allowed"));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(CastContext context) {
        MaterialBrush brush = context.getBrush();
        Block block = context.getTargetBlock();
        if (block == null) {
            return SpellResult.STOP;
        }
        if (allowed != null) {
            if (!allowed.contains(block.getType())) return SpellResult.STOP;
        } else {
            if (brush != null && brush.isErase()) {
                if (!context.hasBreakPermission(block)) {
                    return SpellResult.STOP;
                }
            } else {
                if (!context.hasBuildPermission(block)) {
                    return SpellResult.STOP;
                }
            }
            if (!context.isDestructible(block)) {
                return SpellResult.STOP;
            }
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }
}