package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class AlterSpell extends Spell
{
    static final String  DEFAULT_ADJUSTABLES = "6, 8, 9, 10,11,17,18,23,35,50,52,53,54,55,58,59,60,61,62,63,64,65,66,67,68,69,71,75,76,77,81,83,85,86";
    static final String  DEFAULT_RECURSABLES = "17,18,59";

    private MaterialList adjustableMaterials = new MaterialList();
    private MaterialList recursableMaterials = new MaterialList();

    private final int    recurseDistance     = 32;

    protected void adjust(Block block, byte dataValue, BlockList adjustedBlocks, boolean recursive, int rDepth)
    {
        adjustedBlocks.add(block);
        block.setData(dataValue);

        if (recursive && rDepth < recurseDistance)
        {
            Material targetMaterial = block.getType();
            tryAdjust(block.getFace(BlockFace.NORTH), dataValue, targetMaterial, adjustedBlocks, rDepth + 1);
            tryAdjust(block.getFace(BlockFace.WEST), dataValue, targetMaterial, adjustedBlocks, rDepth + 1);
            tryAdjust(block.getFace(BlockFace.SOUTH), dataValue, targetMaterial, adjustedBlocks, rDepth + 1);
            tryAdjust(block.getFace(BlockFace.EAST), dataValue, targetMaterial, adjustedBlocks, rDepth + 1);
            tryAdjust(block.getFace(BlockFace.UP), dataValue, targetMaterial, adjustedBlocks, rDepth + 1);
            tryAdjust(block.getFace(BlockFace.DOWN), dataValue, targetMaterial, adjustedBlocks, rDepth + 1);
        }
    }

    @Override
    public String getDescription()
    {
        return "Alter certain objects";
    }

    @Override
    public String getName()
    {
        return "alter";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Block targetBlock = targeting.getTargetBlock();
        if (targetBlock == null)
        {
            castMessage(player, "No target");
            return false;
        }
        if (!adjustableMaterials.contains(targetBlock.getType()))
        {
            player.sendMessage("Can't adjust " + targetBlock.getType().name().toLowerCase());
            return false;
        }

        BlockList undoList = new BlockList();
        int originalData = targetBlock.getData();

        // TODO: Get from MaterialData...
        int minValue = 0;// minData.get(materialIndex);
        int maxValue = 15;// maxData.get(materialIndex);
        int dataSize = maxValue - minValue + 1;

        byte data = (byte) ((originalData - minValue + 1) % dataSize + minValue);

        boolean recursive = recursableMaterials.contains(targetBlock.getType());

        adjust(targetBlock, data, undoList, recursive, 0);

        magic.addToUndoQueue(player, undoList);

        castMessage(player, "Adjusting " + targetBlock.getType().name().toLowerCase() + " from " + originalData + " to " + data);

        return true;
    }

    @Override
    public void onLoad()
    {
        adjustableMaterials = getMaterialList("adjustable", DEFAULT_ADJUSTABLES);
        recursableMaterials = getMaterialList("adjustable-recurse", DEFAULT_RECURSABLES);
    }

    protected void tryAdjust(Block target, byte dataValue, Material targetMaterial, BlockList adjustedBlocks, int rDepth)
    {
        if (target.getType() != targetMaterial || adjustedBlocks.contains(target))
        {
            return;
        }

        adjust(target, dataValue, adjustedBlocks, true, rDepth);
    }
}
