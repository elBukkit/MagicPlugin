package com.elmakers.mine.bukkit.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Mine extends Spell
{
    static final String    DEFAULT_DATA      = "0 ,0 ,0  ,0  ,0  ,0  ,4";
    static final String    DEFAULT_MINEABLE  = "14,15,16, 56, 73, 74, 21";
    static final String    DEFAULT_MINED     = "14,15,263,264,331,331,351";

    private int           maxRecursion      = 16;
    private List<Integer> mineableMaterials = new ArrayList<Integer>();
    private List<Integer> minedData         = new ArrayList<Integer>();
    private List<Integer> minedMaterials    = new ArrayList<Integer>();

    @Override
    public String getDescription()
    {
        return "Mines and drops the targeted resources";
    }

    @Override
    public String getName()
    {
        return "mine";
    }

    public boolean isMineable(Block block)
    {
        if (block.getType() == Material.AIR)
        {
            return false;
        }

        return mineableMaterials.contains(block.getType());
    }

    protected void mine(Block block, Material fillMaterial, BlockList minedBlocks)
    {
        mine(block, fillMaterial, minedBlocks, 0);
    }

    protected void mine(Block block, Material fillMaterial, BlockList minedBlocks, int rDepth)
    {
        minedBlocks.add(block);
        block.setType(Material.AIR);

        if (rDepth < maxRecursion)
        {
            tryMine(block.getFace(BlockFace.NORTH), fillMaterial, minedBlocks, rDepth + 1);
            tryMine(block.getFace(BlockFace.WEST), fillMaterial, minedBlocks, rDepth + 1);
            tryMine(block.getFace(BlockFace.SOUTH), fillMaterial, minedBlocks, rDepth + 1);
            tryMine(block.getFace(BlockFace.EAST), fillMaterial, minedBlocks, rDepth + 1);
            tryMine(block.getFace(BlockFace.UP), fillMaterial, minedBlocks, rDepth + 1);
            tryMine(block.getFace(BlockFace.DOWN), fillMaterial, minedBlocks, rDepth + 1);
        }
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Block target = targeting.getTargetBlock();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        if (!isMineable(target))
        {
            sendMessage(player, "Can't mine " + target.getType().name().toLowerCase());
            return false;
        }

        BlockList minedBlocks = new BlockList();
        Material mineMaterial = target.getType();
        mine(target, mineMaterial, minedBlocks);

        World world = player.getWorld();

        int index = mineableMaterials.indexOf(mineMaterial);
        mineMaterial = Material.values()[minedMaterials.get(index)];
        byte data = (byte) (int) minedData.get(index);

        Location itemDrop = new Location(world, target.getX(), target.getY(), target.getZ(), 0, 0);
        ItemStack items = new ItemStack(mineMaterial, minedBlocks.size(), (short) 0, data);
        player.getWorld().dropItemNaturally(itemDrop, items);

        // This isn't undoable, since we can't pick the items back up!
        // So, don't add it to the undo queue.
        castMessage(player, "Mined " + minedBlocks.size() + " blocks of " + mineMaterial.name().toLowerCase());

        return true;
    }

    @Override
    public void onLoad()
    {
        mineableMaterials = csvParser.parseIntegers(DEFAULT_MINEABLE);
        minedMaterials = csvParser.parseIntegers(DEFAULT_MINED);
        minedData = csvParser.parseIntegers(DEFAULT_DATA);
       // maxRecursion = properties.getInteger("spells-mine-recursion", maxRecursion);
    }

    protected void tryMine(Block target, Material fillMaterial, BlockList minedBlocks, int rDepth)
    {
        if (target.getType() != fillMaterial || minedBlocks.contains(target))
        {
            return;
        }

        mine(target, fillMaterial, minedBlocks, rDepth);
    }
}
