package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;

public class FrostSpell extends Spell
{
    private int defaultRadius          = 2;
    private int defaultSearchDistance  = 32;
    private int maxRadius              = 32;
    private int verticalSearchDistance = 8;

    public int checkPosition(int x, int z, int R)
    {
        return x * x + z * z - R * R;
    }

    public void frostBlock(int dx, int dy, int dz, Block centerPoint, int radius, BlockList frostedBlocks)
    {
        int x = centerPoint.getX() + dx - radius;
        int y = centerPoint.getY() + dy - radius;
        int z = centerPoint.getZ() + dz - radius;
        Block block = player.getWorld().getBlockAt(x, y, z);
        int depth = 0;

        if (block.getType() == Material.AIR)
        {
            while (depth < verticalSearchDistance && block.getType() == Material.AIR)
            {
                depth++;
                block = block.getFace(BlockFace.DOWN);
            }
        }
        else
        {
            while (depth < verticalSearchDistance && block.getType() != Material.AIR)
            {
                depth++;
                block = block.getFace(BlockFace.UP);
            }
            block = block.getFace(BlockFace.DOWN);
        }

        if (block.getType() == Material.AIR || block.getType() == Material.SNOW)
        {
            return;
        }
        Material material = Material.SNOW;
        Block target = block;
        if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
        {
            material = Material.ICE;
        }
        else if (block.getType() == Material.LAVA)
        {
            material = Material.COBBLESTONE;
        }
        else if (block.getType() == Material.STATIONARY_LAVA)
        {
            material = Material.OBSIDIAN;
        }
        else if (block.getType() == Material.FIRE)
        {
            material = Material.AIR;
        }
        else
        {
            target = target.getFace(BlockFace.UP);
        }
        frostedBlocks.add(target);
        target.setType(material);
    }

    @Override
    public String getCategory()
    {
        return "nature";
    }

    @Override
    public String getDescription()
    {
        return "Freeze water and create snow";
    }

    @Override
    public Material getMaterial()
    {
        return Material.SNOW_BALL;
    }

    @Override
    public String getName()
    {
        return "frost";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        Block target = getTargetBlock();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        if (defaultSearchDistance > 0 && getDistance(player, target) > defaultSearchDistance)
        {
            castMessage(player, "Can't frost that far away");
            return false;
        }

        int radius = defaultRadius;
        if (parameters.length > 0)
        {
            try
            {
                radius = Integer.parseInt(parameters[0]);
                if (radius > maxRadius && maxRadius > 0)
                {
                    radius = maxRadius;
                }
            }
            catch (NumberFormatException ex)
            {
                radius = defaultRadius;
            }
        }

        BlockList frostedBlocks = new BlockList();
        int diameter = radius * 2;
        int midX = (diameter - 1) / 2;
        int midY = (diameter - 1) / 2;
        int midZ = (diameter - 1) / 2;
        int diameterOffset = diameter - 1;

        for (int x = 0; x < radius; ++x)
        {
            for (int z = 0; z < radius; ++z)
            {
                if (checkPosition(x - midX, z - midZ, radius) <= 0)
                {
                    int y = midY;
                    frostBlock(x, y, z, target, radius, frostedBlocks);
                    frostBlock(diameterOffset - x, y, z, target, radius, frostedBlocks);
                    frostBlock(x, diameterOffset - y, z, target, radius, frostedBlocks);
                    frostBlock(x, y, diameterOffset - z, target, radius, frostedBlocks);
                    frostBlock(diameterOffset - x, diameterOffset - y, z, target, radius, frostedBlocks);
                    frostBlock(x, diameterOffset - y, diameterOffset - z, target, radius, frostedBlocks);
                    frostBlock(diameterOffset - x, y, diameterOffset - z, target, radius, frostedBlocks);
                    frostBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, frostedBlocks);
                }

            }
        }

        spells.addToUndoQueue(player, frostedBlocks);
        castMessage(player, "Frosted " + frostedBlocks.size() + " blocks");

        return true;
    }

    @Override
    public void onLoad(PluginProperties properties)
    {
        defaultRadius = properties.getInteger("spells-frost-radius", defaultRadius);
        maxRadius = properties.getInteger("spells-frost-max-radius", maxRadius);
        defaultSearchDistance = properties.getInteger("spells-frost-search-distance", defaultSearchDistance);
        verticalSearchDistance = properties.getInteger("spells-frost-vertical-search-distance", verticalSearchDistance);
    }

}
