package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.Collection;

public class ModifyReflectiveAction extends BaseSpellAction {
    private double backfireChance = 1;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        backfireChance = parameters.getDouble("reflect_chance", 1);
    }

    @SuppressWarnings("deprecation")
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
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("reflect_chance");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("reflect_chance")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_PERCENTAGES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
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