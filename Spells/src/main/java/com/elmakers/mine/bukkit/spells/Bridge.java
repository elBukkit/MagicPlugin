package com.elmakers.mine.bukkit.spells;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Bridge extends Spell
{
    int MAX_SEARCH_DISTANCE = 16;

    @Override
    public String getDescription()
    {
        return "Extends the ground underneath you";
    }

    @Override
    public String getName()
    {
        return "bridge";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Block playerBlock = targeting.getPlayerBlock();
        if (playerBlock == null)
        {
            // no spot found to bridge
            player.sendMessage("You need to be standing on something");
            return false;
        }

        BlockFace direction = targeting.getPlayerFacing();
        Block attachBlock = playerBlock;
        Block targetBlock = attachBlock.getFace(direction);

        MaterialData material = getBuildingMaterial(parameters, playerBlock);
        
        int distance = 0;
        while (targeting.isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
        {
            distance++;
            attachBlock = targetBlock;
            targetBlock = attachBlock.getFace(direction);
        }
        if (targeting.isTargetable(targetBlock.getType()))
        {
            player.sendMessage("Can't bridge any further");
            return false;
        }
        BlockList bridgeBlocks = new BlockList();
        bridgeBlocks.add(targetBlock);
        targetBlock.setType(material.getType());
        targetBlock.setData(material.getData());

        castMessage(player, "A bridge extends!");
        magic.addToUndoQueue(player, bridgeBlocks);

        // castMessage(player, "Facing " + playerRot + " : " + direction.name()
        // + ", " + distance + " spaces to " + attachBlock.getType().name());

        return true;
    }
}
