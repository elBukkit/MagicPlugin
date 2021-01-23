package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class ModifyBreakableAction extends BaseSpellAction {
    private double breakable = 1;
    private boolean targetAir = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        breakable = parameters.getDouble("breakable", 1);
        targetAir = parameters.getBoolean("target_air", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Block block = context.getTargetBlock();
        if (breakable <= 0) {
            return SpellResult.FAIL;
        }
        if (!targetAir && DefaultMaterials.isAir(block.getType())) {
            return SpellResult.NO_TARGET;
        }
        if (!context.hasBreakPermission(block)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        if (!context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }

        context.registerForUndo(block);
        context.registerBreakable(block, breakable);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("breakable");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("breakable")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}
