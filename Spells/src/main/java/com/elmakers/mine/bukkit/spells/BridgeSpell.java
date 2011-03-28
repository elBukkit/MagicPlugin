package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;

public class BridgeSpell extends Spell
{
    int MAX_SEARCH_DISTANCE = 16;

    @Override
    public String getCategory()
    {
        return "construction";
    }

    @Override
    public String getDescription()
    {
        return "Extends the ground underneath you";
    }

    @Override
    public Material getMaterial()
    {
        return Material.GOLD_HOE;
    }

    @Override
    public String getName()
    {
        return "bridge";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        Block playerBlock = getPlayerBlock();
        if (playerBlock == null)
        {
            // no spot found to bridge
            player.sendMessage("You need to be standing on something");
            return false;
        }

        BlockFace direction = getPlayerFacing();
        Block attachBlock = playerBlock;
        Block targetBlock = attachBlock.getFace(direction);

        Material material = targetBlock.getType();
        byte data = targetBlock.getData();

        ItemStack buildWith = getBuildingMaterial();
        if (buildWith != null)
        {
            material = buildWith.getType();
            data = getItemData(buildWith);
        }

        int distance = 0;
        while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
        {
            distance++;
            attachBlock = targetBlock;
            targetBlock = attachBlock.getFace(direction);
        }
        if (isTargetable(targetBlock.getType()))
        {
            player.sendMessage("Can't bridge any further");
            return false;
        }
        BlockList bridgeBlocks = new BlockList();
        bridgeBlocks.add(targetBlock);
        targetBlock.setType(material);
        targetBlock.setData(data);

        castMessage(player, "A bridge extends!");
        spells.addToUndoQueue(player, bridgeBlocks);

        // castMessage(player, "Facing " + playerRot + " : " + direction.name()
        // + ", " + distance + " spaces to " + attachBlock.getType().name());

        return true;
    }
}
