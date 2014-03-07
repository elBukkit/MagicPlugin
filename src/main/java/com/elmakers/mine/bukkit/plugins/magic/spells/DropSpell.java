package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Collection;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class DropSpell extends BlockSpell
{
	private final static int DEFAULT_MAX_RECURSION = 16;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		// TODO: Optimize this
		Set<Material> dropMaterials = controller.getMaterialSet(parameters.getString("drop"));
		
		if (!dropMaterials.contains(target.getType()))
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		int maxRecursion = parameters.getInteger("recursion_depth", DEFAULT_MAX_RECURSION);
		BlockList droppedBlocks = new BlockList();
		drop(target, dropMaterials, droppedBlocks, maxRecursion);

		// Make this undoable.. even though it means you can exploit it for ore with Rewind. Meh!
		registerForUndo(droppedBlocks);
		castMessage("Dropped " + droppedBlocks.size() + " blocks");

		return SpellResult.CAST;
	}

	protected void drop(Block block, Set<Material> dropTypes, BlockList minedBlocks, int maxRecursion)
	{		
		drop(block, dropTypes, minedBlocks, maxRecursion, 0);
	}

	protected void drop(Block block, Set<Material> dropTypes, BlockList droppedBlocks, int maxRecursion, int rDepth)
	{
		droppedBlocks.add(block);
		Collection<ItemStack> drops = block.getDrops();
		for (ItemStack drop : drops) {
			block.getWorld().dropItemNaturally(block.getLocation(), drop);
		}

		block.setType(Material.AIR);
		
		if (rDepth < maxRecursion)
		{
			tryDrop(block.getRelative(BlockFace.NORTH), dropTypes, droppedBlocks, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.WEST), dropTypes, droppedBlocks, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.SOUTH), dropTypes, droppedBlocks, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.EAST), dropTypes, droppedBlocks, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.UP), dropTypes, droppedBlocks, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.DOWN), dropTypes, droppedBlocks, maxRecursion, rDepth + 1);
		}
	}

	protected void tryDrop(Block target, Set<Material> dropTypes, BlockList minedBlocks, int maxRecursion, int rDepth)
	{
		if (!dropTypes.contains(target.getType()) || minedBlocks.contains(target))
		{
			return;
		}

		drop(target, dropTypes, minedBlocks, maxRecursion, rDepth);
	}
}
