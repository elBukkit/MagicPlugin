package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;

@Deprecated
public class CushionSpell extends BlockSpell
{
    private static final int DEFAULT_CUSHION_WIDTH = 3;
    private static final int DEFAULT_CUSHION_HEIGHT = 4;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        World world = getWorld();
        Block targetFace = getTargetBlock();
        if (targetFace == null)
        {
            return SpellResult.NO_TARGET;
        }
        if (!hasBuildPermission(targetFace)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        int cushionWidth = parameters.getInt("width", DEFAULT_CUSHION_WIDTH);
        int cushionHeight = parameters.getInt("height", DEFAULT_CUSHION_HEIGHT);

        int bubbleStart = -cushionWidth  / 2;
        int bubbleEnd = cushionWidth  / 2;

        for (int dx = bubbleStart; dx < bubbleEnd; dx++)
        {
            for (int dz = bubbleStart; dz < bubbleEnd; dz++)
            {
                for (int dy = 0; dy < cushionHeight; dy++)
                {
                    int x = targetFace.getX() + dx;
                    int y = targetFace.getY() + dy;
                    int z = targetFace.getZ() + dz;
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR || block.getType() == Material.FIRE)
                    {
                        registerForUndo(block);
                        block.setType(Material.WATER);
                    }
                }
            }
        }

        registerForUndo();
        return SpellResult.CAST;
    }
}
