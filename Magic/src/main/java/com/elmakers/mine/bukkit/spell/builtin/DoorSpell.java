package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.Door;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class DoorSpell extends BlockSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();

        if (!target.hasTarget())
        {
            return SpellResult.NO_TARGET;
        }

        Block targetBlock = target.getBlock();
        BlockState blockState = targetBlock.getState();
        Object data = blockState.getData();
        if (!(data instanceof Door)) {
            return SpellResult.NO_TARGET;
        }
        Door doorData = (Door)data;
        if (doorData.isTopHalf()) {
            targetBlock = targetBlock.getRelative(BlockFace.DOWN);
            blockState = targetBlock.getState();
            data = blockState.getData();
            if (!(data instanceof Door)) {
                return SpellResult.NO_TARGET;
            }
            doorData = (Door)data;
        }

        if (!hasBuildPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        registerForUndo(targetBlock);
        String type = parameters.getString("type", "open");
        if (type.equalsIgnoreCase("open")) {
            doorData.setOpen(true);
        } else if (type.equalsIgnoreCase("close")) {
            doorData.setOpen(false);
        } else if (type.equalsIgnoreCase("toggle")) {
            doorData.setOpen(!doorData.isOpen());
        } else {
            return SpellResult.FAIL;
        }
        blockState.setData(doorData);
        blockState.update();

        registerForUndo();
        return SpellResult.CAST;
    }
}
