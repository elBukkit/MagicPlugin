package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.DoorActionType;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class DoorAction extends BaseSpellAction {
    private DoorActionType actionType;

    @Override
    public SpellResult perform(CastContext context)
    {
        Block targetBlock = context.getTargetBlock();
        CompatibilityUtils compatibilityUtils = CompatibilityLib.getCompatibilityUtils();
        Block[] doorBlocks = compatibilityUtils.getDoorBlocks(targetBlock);
        if (doorBlocks == null) {
            return SpellResult.NO_TARGET;
        }
        for (Block doorBlock : doorBlocks) {
            if (!context.hasBuildPermission(doorBlock)) {
                return SpellResult.INSUFFICIENT_PERMISSION;
            }
            if (!context.isDestructible(doorBlock)) {
                return SpellResult.NO_TARGET;
            }
        }
        if (!compatibilityUtils.checkDoorAction(doorBlocks, actionType)) {
            return SpellResult.NO_TARGET;
        }
        for (Block doorBlock : doorBlocks) {
            context.registerForUndo(doorBlock);
        }
        if (!CompatibilityLib.getCompatibilityUtils().performDoorAction(doorBlocks, actionType)) {
            return SpellResult.NO_TARGET;
        }

        context.setTargetLocation(doorBlocks[0].getLocation());
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        actionType = DoorActionType.TOGGLE;
        String type = parameters.getString("type", "open");
        if (type.equalsIgnoreCase("open")) {
            actionType = DoorActionType.OPEN;
        } else if (type.equalsIgnoreCase("close")) {
            actionType = DoorActionType.CLOSE;
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("type");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("type")) {
            examples.add("open");
            examples.add("close");
            examples.add("toggle");
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
