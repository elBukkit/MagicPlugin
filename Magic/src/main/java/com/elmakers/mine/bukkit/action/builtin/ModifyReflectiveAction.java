package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class ModifyReflectiveAction extends BaseSpellAction {
    private double backfireChance = 1;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        backfireChance = parameters.getDouble("reflect_chance", 1);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Block block = context.getTargetBlock();
        if (!context.hasBuildPermission(block)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        if (!context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }

        context.registerForUndo(block);
        context.registerReflective(block, backfireChance);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("reflect_chance");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("reflect_chance")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
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
