package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;

public class CushionSpell extends Spell
{
    private int airBubble     = 3;
    private int cushionHeight = 4;
    private int cushionWidth  = 3;

    @Override
    public String getCategory()
    {
        return "help";
    }

    @Override
    public String getDescription()
    {
        return "Create a safety bubble";
    }

    @Override
    public Material getMaterial()
    {
        return Material.SOUL_SAND;
    }

    @Override
    public String getName()
    {
        return "cushion";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        World world = player.getWorld();
        CraftWorld craftWorld = (CraftWorld) world;
        Block targetFace = getTargetBlock();
        if (targetFace == null)
        {
            castMessage(player, "No target");
            return false;
        }

        castMessage(player, "Happy landings");

        BlockList cushionBlocks = new BlockList();
        cushionBlocks.setTimeToLive(10000);

        BlockList airBlocks = new BlockList();
        airBlocks.setTimeToLive(500);
        airBlocks.setRepetitions(30);

        int bubbleStart = -cushionWidth / 2;
        int bubbleEnd = cushionWidth / 2;

        for (int dx = bubbleStart - airBubble; dx < bubbleEnd + airBubble; dx++)
        {
            for (int dz = bubbleStart - airBubble; dz < bubbleEnd + airBubble; dz++)
            {
                for (int dy = -airBubble; dy < cushionHeight + airBubble; dy++)
                {
                    int x = targetFace.getX() + dx;
                    int y = targetFace.getY() + dy;
                    int z = targetFace.getZ() + dz;
                    Block block = craftWorld.getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR)
                    {
                        if (dx <= bubbleStart || dx >= bubbleEnd || dz <= bubbleStart || dz >= bubbleEnd || dy <= 0)
                        {
                            airBlocks.add(block);
                        }
                        else
                        {
                            cushionBlocks.add(block);
                            block.setType(Material.STATIONARY_WATER);
                        }
                    }
                }
            }
        }

        spells.scheduleCleanup(cushionBlocks);
        spells.scheduleCleanup(airBlocks);

        // Schedule an additional later cleanup, to cleanup water spillage
        BlockList delayedCleanup = new BlockList(cushionBlocks);
        delayedCleanup.setTimeToLive(15000);

        spells.scheduleCleanup(delayedCleanup);

        return true;
    }

    @Override
    public void onLoad(PluginProperties properties)
    {
        cushionWidth = properties.getInteger("spells-cushion-width", cushionWidth);
        cushionHeight = properties.getInteger("spells-cushion-height", cushionHeight);
        airBubble = properties.getInteger("spells-air-bubble-thickness", airBubble);
    }
}
