package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Collection;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;

public class DropSpell extends BlockSpell
{
	private final static int DEFAULT_MAX_RECURSION = 16;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block target = getTargetBlock();
		if (target == null)
		{
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

		int maxRecursion = parameters.getInt("recursion_depth", DEFAULT_MAX_RECURSION);
		drop(target, dropMaterials, maxRecursion);

		// Make this undoable.. even though it means you can exploit it for ore with Rewind. Meh!
		registerForUndo();

		return SpellResult.CAST;
	}

	protected void drop(Block block, Set<Material> dropTypes, int maxRecursion)
	{		
		drop(block, dropTypes, maxRecursion, 0);
	}

	protected void drop(Block block, Set<Material> dropTypes, int maxRecursion, int rDepth)
	{
		registerForUndo(block);
		Collection<ItemStack> drops = block.getDrops();
		for (ItemStack drop : drops) {
			block.getWorld().dropItemNaturally(block.getLocation(), drop);
		}

		block.setType(Material.AIR);
		
		if (rDepth < maxRecursion)
		{
			tryDrop(block.getRelative(BlockFace.NORTH), dropTypes, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.WEST), dropTypes, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.SOUTH), dropTypes, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.EAST), dropTypes, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.UP), dropTypes, maxRecursion, rDepth + 1);
			tryDrop(block.getRelative(BlockFace.DOWN), dropTypes, maxRecursion, rDepth + 1);
		}
	}

	protected void tryDrop(Block target, Set<Material> dropTypes, int maxRecursion, int rDepth)
	{
		if (!dropTypes.contains(target.getType()) || contains(target))
		{
			return;
		}

		drop(target, dropTypes, maxRecursion, rDepth);
	}
}
