package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BlockSpell;

@Deprecated
public class RaiseSpell extends BlockSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Block targetBlock = getTargetBlock();
        if (targetBlock == null)
        {
            return SpellResult.NO_TARGET;
        }

        if (!isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        if (!hasBuildPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        BlockFace direction = BlockFace.UP;
        int distance = parameters.getInt("distance", 5);
        Block highestBlock = targetBlock;
        while (distance > 0) {
            highestBlock = highestBlock.getRelative(direction);
            if (highestBlock.getType() != Material.AIR) {
                return SpellResult.NO_TARGET;
            }
            distance--;
        }
        if (!hasBuildPermission(highestBlock)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        MaterialAndData blockState = new MaterialAndData(targetBlock);
        registerForUndo(targetBlock);
        registerForUndo(highestBlock);

        targetBlock.setType(Material.AIR);
        blockState.modify(highestBlock);
        currentCast.registerBreakable(highestBlock, 1);

        registerForUndo();

        return SpellResult.CAST;
    }
}
