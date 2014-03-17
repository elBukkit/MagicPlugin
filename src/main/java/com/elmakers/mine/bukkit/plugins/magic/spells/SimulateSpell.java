package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.blocks.BlockData;
import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class SimulateSpell extends BlockSpell {
	
	private static final int DEFAULT_RADIUS = 32;
	private static BlockFace[] neighborFaces = { BlockFace.NORTH, BlockFace.NORTH_EAST, 
		BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
	};

	@Override
	public SpellResult onCast(ConfigurationNode parameters) {
		int radius = parameters.getInt("radius", DEFAULT_RADIUS);
		radius = parameters.getInt("r", radius);
		
		Target t = getTarget();
		if (t == null) {
			return SpellResult.NO_TARGET;
		}
		
		Block target = t.getBlock();
		if (target == null) {
			return SpellResult.NO_TARGET;
		}
		
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		boolean includeCommands = parameters.getBoolean("commands", true);
		Material birthMaterial = target.getType();
		birthMaterial = parameters.getMaterial("material", birthMaterial);
		Material deathMaterial = parameters.getMaterial("death_material", Material.AIR);
		
		World world = target.getWorld();
		int y = target.getY();
		int startX = target.getX() - radius / 2;
		int startZ = target.getZ() - radius / 2;
		int endX = target.getX() + radius / 2;
		int endZ = target.getZ() + radius / 2;
		
		BlockList deadBlocks = new BlockList();
		BlockList bornBlocks = new BlockList();
		
		for (int x = startX; x <= endX; x++) {
			for (int z = startZ; z <= endZ; z++) {
				Block block = world.getBlockAt(x, y, z);
				Material blockMaterial = block.getType();
				if (blockMaterial == birthMaterial) {
					int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
					// Cells with < 2 neighbors die
					if (neighborCount < 2 || neighborCount > 3) {
						deadBlocks.add(block);
					}
				} else if (blockMaterial == deathMaterial) {
					// Check for exactly 3 neighbors
					if (getNeighborCount(block, birthMaterial, includeCommands) == 3) {
						bornBlocks.add(block);
					}
				} else if (includeCommands && blockMaterial == Material.COMMAND) {
					// TODO : Handle command blocks in a special way, and always simulate them.
					// This should probably be made into a config option, but the main purposes of
					// this spell is to create command-block based automota, so it needs to be able
					// to move the "source" command block around.
				}
			}
		}

		BlockList undoBlocks = new BlockList();
		
		List<BlockData> deadBlockList = deadBlocks.getBlockList();
		if (deadBlockList != null) {
			for (BlockData killBlock : deadBlockList) {
				undoBlocks.add(killBlock);
				killBlock.getBlock().setType(deathMaterial);
				controller.updateBlock(killBlock.getBlock());
			}
		}
		
		List<BlockData> bornBlockList = bornBlocks.getBlockList();
		if (bornBlockList != null) {
			for (BlockData birthBlock : bornBlockList) {
				undoBlocks.add(birthBlock);
				birthBlock.getBlock().setType(birthMaterial);
				controller.updateBlock(birthBlock.getBlock());
			}
		}
		
		registerForUndo(undoBlocks);
		
		return SpellResult.CAST;
	}
	
	protected int getNeighborCount(Block block, Material liveMaterial, boolean includeCommands) {
		int liveCount = 0;
		for (BlockFace face : neighborFaces) {
			Material neighborType = block.getRelative(face).getType();
			if (neighborType == liveMaterial || (includeCommands && neighborType == Material.COMMAND)) {
				liveCount++;
			}
		}
		
		return liveCount;
	}
}
