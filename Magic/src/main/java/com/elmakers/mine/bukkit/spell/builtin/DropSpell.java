package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;

@Deprecated
public class DropSpell extends BlockSpell
{
	private final static int DEFAULT_MAX_RECURSION = 16;
	private int dropCount;
	private boolean falling = true;
	private boolean diagonals = false;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block target = getTargetBlock();
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}

		MaterialSet dropMaterials = controller.getMaterialSetManager().fromConfigEmpty(parameters.getString("drop"));
		if (!dropMaterials.testBlock(target))
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBreakPermission(target))
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

        Collection<ItemStack> drops = new ArrayList<>();
		int maxRecursion = parameters.getInt("recursion_depth", DEFAULT_MAX_RECURSION);
		dropCount = parameters.getInt("drop_count", -1);
		falling = parameters.getBoolean("falling", true);
		diagonals = parameters.getBoolean("diagonals", true);
		drop(target, dropMaterials, drops, maxRecursion);

        for (ItemStack drop : drops) {
            target.getWorld().dropItemNaturally(target.getLocation(), drop);
        }

		// Make this undoable.. even though it means you can exploit it for ore with Rewind. Meh!
		registerForUndo();

		return SpellResult.CAST;
	}

	protected void drop(Block block, MaterialSet dropTypes, Collection<ItemStack> drops, int maxRecursion)
	{		
		drop(block, dropTypes, drops, maxRecursion, 0);
	}

	protected void drop(Block block, MaterialSet dropTypes, Collection<ItemStack> drops, int maxRecursion, int rDepth)
	{
		registerForUndo(block);
		if (dropCount < 0 || drops.size() < dropCount) {
			drops.addAll(block.getDrops());
		} else if (falling) {
			Location blockLocation = block.getLocation();
			FallingBlock falling = block.getWorld().spawnFallingBlock(blockLocation, block.getType(), block.getData());
			falling.setDropItem(false);
		}
		block.setType(Material.AIR);
		
		if (rDepth < maxRecursion)
		{
			tryDrop(block.getRelative(BlockFace.NORTH), dropTypes, drops, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.WEST), dropTypes, drops, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.SOUTH), dropTypes, drops, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.EAST), dropTypes, drops, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.UP), dropTypes, drops, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.DOWN), dropTypes, drops, maxRecursion, rDepth + 1);
			if (diagonals) {
				tryDrop(block.getRelative(BlockFace.NORTH_EAST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(block.getRelative(BlockFace.NORTH_WEST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(block.getRelative(BlockFace.SOUTH_EAST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(block.getRelative(BlockFace.SOUTH_WEST), dropTypes, drops, maxRecursion, rDepth + 1);
				Block up = block.getRelative(BlockFace.UP);
				tryDrop(up.getRelative(BlockFace.NORTH_EAST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(up.getRelative(BlockFace.NORTH_WEST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(up.getRelative(BlockFace.SOUTH_EAST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(up.getRelative(BlockFace.SOUTH_WEST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(up.getRelative(BlockFace.NORTH), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(up.getRelative(BlockFace.WEST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(up.getRelative(BlockFace.SOUTH), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(up.getRelative(BlockFace.EAST), dropTypes, drops, maxRecursion, rDepth + 1);
				Block down = block.getRelative(BlockFace.DOWN);
				tryDrop(down.getRelative(BlockFace.NORTH_EAST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(down.getRelative(BlockFace.NORTH_WEST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(down.getRelative(BlockFace.SOUTH_EAST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(down.getRelative(BlockFace.SOUTH_WEST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(down.getRelative(BlockFace.NORTH), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(down.getRelative(BlockFace.WEST), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(down.getRelative(BlockFace.SOUTH), dropTypes, drops, maxRecursion, rDepth + 1);
				tryDrop(down.getRelative(BlockFace.EAST), dropTypes, drops, maxRecursion, rDepth + 1);
			}
		}
	}

	protected void tryDrop(Block target, MaterialSet dropTypes, Collection<ItemStack> drops, int maxRecursion, int rDepth)
	{
		if (!dropTypes.testBlock(target) || contains(target))
		{
			return;
		}

		drop(target, dropTypes, drops, maxRecursion, rDepth);
	}
}
