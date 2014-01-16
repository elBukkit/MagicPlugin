package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.blocks.BlockList;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MineSpell extends Spell
{
	private static final String		DEFAULT_MINEABLE	= "14,15,16, 56, 73, 74, 21 ,129,153";
	private static final String		DEFAULT_MINED		= "14,15,263,264,331,331,351,388,406";
	private static final String		DEFAULT_DATA		= "0 ,0 ,0  ,0  ,0  ,0  ,4  ,0  ,1";
	private final static int DEFAULT_MAX_RECURSION = 16;

	@SuppressWarnings("deprecation")
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
		List<Material> mineableMaterials = new ArrayList<Material>();
		mineableMaterials.addAll(csv.parseMaterials(DEFAULT_MINEABLE));

		
		if (!isMineable(target, mineableMaterials))
		{
			sendMessage("Can't mine " + target.getType().name().toLowerCase());
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		if (isIndestructible(target)) {
			return SpellResult.NO_TARGET;
		}

		int maxRecursion = parameters.getInteger("recursion_depth", DEFAULT_MAX_RECURSION);
		BlockList minedBlocks = new BlockList();
		Material mineMaterial = target.getType();
		mine(target, mineMaterial, minedBlocks, maxRecursion);

		World world = player.getWorld();

		// TODO: Optimize this
		List<Material> minedMaterials = new ArrayList<Material>();
		minedMaterials.addAll(csv.parseMaterials(DEFAULT_MINED));
		List<Integer> minedData = csv.parseIntegers(DEFAULT_DATA);
		
		int index = mineableMaterials.indexOf(mineMaterial);
		mineMaterial = minedMaterials.get(index);
		byte data = (byte)(int)minedData.get(index);

		Location itemDrop = new Location(world, target.getX(), target.getY(), target.getZ(), 0, 0);
		ItemStack items = new ItemStack(mineMaterial, (int)minedBlocks.size(), (short)0 , data);
		player.getWorld().dropItemNaturally(itemDrop, items);

		// This isn't undoable, since we can't pick the items back up!
		// So, don't add it to the undo queue.
		castMessage("Mined " + minedBlocks.size() + " blocks of " + mineMaterial.name().toLowerCase());

		return SpellResult.SUCCESS;
	}

	protected void mine(Block block, Material fillMaterial, BlockList minedBlocks, int maxRecursion)
	{		
		mine(block, fillMaterial, minedBlocks, maxRecursion, 0);
	}

	protected void mine(Block block, Material fillMaterial, BlockList minedBlocks, int maxRecursion, int rDepth)
	{
		minedBlocks.add(block);
		block.setType(Material.AIR);

		if (rDepth < maxRecursion)
		{
			tryMine(block.getRelative(BlockFace.NORTH), fillMaterial, minedBlocks, maxRecursion, rDepth + 1);
			tryMine(block.getRelative(BlockFace.WEST), fillMaterial, minedBlocks, maxRecursion, rDepth + 1);
			tryMine(block.getRelative(BlockFace.SOUTH), fillMaterial, minedBlocks, maxRecursion, rDepth + 1);
			tryMine(block.getRelative(BlockFace.EAST), fillMaterial, minedBlocks, maxRecursion, rDepth + 1);
			tryMine(block.getRelative(BlockFace.UP), fillMaterial, minedBlocks, maxRecursion, rDepth + 1);
			tryMine(block.getRelative(BlockFace.DOWN), fillMaterial, minedBlocks, maxRecursion, rDepth + 1);
		}
	}

	protected void tryMine(Block target, Material fillMaterial, BlockList minedBlocks, int maxRecursion, int rDepth)
	{
		if (target.getType() != fillMaterial || minedBlocks.contains(target))
		{
			return;
		}

		mine(target, fillMaterial, minedBlocks, maxRecursion, rDepth);
	}

	public boolean isMineable(Block block, List<Material> mineableMaterials)
	{
		if (block.getType() == Material.AIR)
			return false;

		return mineableMaterials.contains(block.getType());
	}
}
