package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

public class DoorAction extends BaseSpellAction
{
    private enum DoorActionType {
        OPEN,
        CLOSE,
        TOGGLE
    }

    private DoorActionType actionType;

    @Override
    public SpellResult perform(CastContext context)
    {
		Block targetBlock = context.getTargetBlock();
        byte data = DeprecatedUtils.getData(targetBlock);
        if ((data & 0x8) != 0) {
            targetBlock = targetBlock.getRelative(BlockFace.DOWN);
            data = DeprecatedUtils.getData(targetBlock);
        }

        if (!context.hasBuildPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        context.registerForUndo(targetBlock);
        switch (actionType)
        {
            case OPEN:
                DeprecatedUtils.setData(targetBlock, (byte)(data | 0x4));
                break;
            case CLOSE:
                DeprecatedUtils.setData(targetBlock, (byte)(data & ~0x4));
                break;
            case TOGGLE:
                DeprecatedUtils.setData(targetBlock, (byte)(data ^ 0x4));
                break;
        }

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
