package com.elmakers.mine.bukkit.blocks;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;

import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.Mage;

public class SimulateBatch extends VolumeBatch {
	private static BlockFace[] neighborFaces = { BlockFace.NORTH, BlockFace.NORTH_EAST, 
		BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
	};
	
	private enum SimulationState {
		SCANNING, UPDATING, COMMAND, CLEANUP, FINISHED
	};
	
	private static Material POWER_MATERAIL = Material.REDSTONE_BLOCK;
	
	private static int COMMAND_UPDATE_DELAY = 0;
	
	private Mage mage;
	private BlockSpell spell;
	private Block castCommandBlock;
	private int commandDistanceSquared;
	private String castCommand;
	private Block commandTarget;
	private World world;
	private Material birthMaterial;
	private Material deathMaterial;
	private boolean includeCommands;
	private BlockFace powerDirection;
	int y;
	int startX;
	int startZ;
	int endX;
	int endZ;
	int x;
	int z;
	int updatingIndex;
	int commandDelay;
	int blockCount;
	
	private SimulationState state;

	BlockList deadBlocks = new BlockList();
	BlockList bornBlocks = new BlockList();
	BlockList modifiedBlocks = new BlockList();
	
	public SimulateBatch(BlockSpell spell, Location center, int radius, Material birth, Material death) {
		super(spell.getMage().getController(), center.getWorld().getName());
		this.mage = spell.getMage();
		this.spell = spell;
		
		this.birthMaterial = birth;
		this.deathMaterial = death;
		this.world = center.getWorld();
		includeCommands = false;
		
		y = center.getBlockY();
		startX = center.getBlockX() - radius / 2;
		startZ = center.getBlockZ() - radius / 2;
		endX = center.getBlockX() + radius / 2;
		endZ = center.getBlockZ() + radius / 2;
		
		x = startX;
		z = startZ;
		
		state = SimulationState.SCANNING;
		updatingIndex = 0;
		commandDistanceSquared = 0;
		commandDelay = 0;
		powerDirection = null;
		blockCount = 0;
	}

	public int size() {
		return modifiedBlocks.size();
	}
	
	public int remaining() {
		return (endX - x) * (endZ - z);
	}

	@Override
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		while (state == SimulationState.SCANNING && processedBlocks <= maxBlocks) {
			Block block = world.getBlockAt(x, y, z);
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
				return processedBlocks;
			}
			
			Material blockMaterial = block.getType();
			if (blockMaterial == birthMaterial) {
				int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
				// Cells with < 2 neighbors die
				if (neighborCount < 2 || neighborCount > 3) {
					deadBlocks.add(block);
				} else {
					blockCount++;
				}
			} else if (blockMaterial == deathMaterial) {
				// Check for exactly 3 neighbors
				if (getNeighborCount(block, birthMaterial, includeCommands) == 3) {
					bornBlocks.add(block);
					blockCount++;
				}
			} else if (includeCommands && blockMaterial == Material.COMMAND && castCommandBlock != null && BlockData.getBlockId(block) == BlockData.getBlockId(castCommandBlock)) {
				// Only process the casting command block.
				
				// Treat this like a living cell.
				int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
				if (neighborCount < 2 || neighborCount > 3) {
					deadBlocks.add(block);
				} else {
					// But also make sure to replace it if it's not being re-born.
					// This will ensure that the command gets re-run if it's still attached to redstone.
					commandTarget = block;
					blockCount++;
				}
			}
			
			z++;
			if (z > endZ) {
				z = startZ;
				x++;
			}
			if (x > endX) {
				// Clear the command block's power source, if it exists (only spports redstone blocks at the moment)
				if (castCommandBlock != null && castCommandBlock.getType() == Material.COMMAND) {
					if (castCommandBlock.getRelative(BlockFace.DOWN).getType() == POWER_MATERAIL) {
						powerDirection = BlockFace.DOWN;
					} else if (castCommandBlock.getRelative(BlockFace.UP).getType() == POWER_MATERAIL) {
						powerDirection = BlockFace.UP;
					}
					
					if (powerDirection != null) {
						Block powerBlock = castCommandBlock.getRelative(powerDirection);
						modifiedBlocks.add(powerBlock);
						powerBlock.setType(Material.AIR);
					}
				}
				
				state = SimulationState.UPDATING;
			}
		}
		
		while (state == SimulationState.UPDATING && processedBlocks <= maxBlocks) {
			List<BlockData> deadBlockList = deadBlocks.getBlockList();
			if (deadBlockList != null && updatingIndex < deadBlockList.size()) {
				BlockData killBlock = deadBlockList.get(updatingIndex);
				modifiedBlocks.add(killBlock);
				Block block = killBlock.getBlock();
				block.setType(deathMaterial);
				controller.updateBlock(block);
				processedBlocks++;
			}
			
			List<BlockData> bornBlockList = bornBlocks.getBlockList();
			int bornIndex = updatingIndex - deadBlocks.size();
			if (bornBlockList != null && bornIndex >= 0 && bornIndex < bornBlockList.size()) {
				BlockData birthBlock = bornBlockList.get(bornIndex);
				modifiedBlocks.add(birthBlock);
				Block block = birthBlock.getBlock();
				if (includeCommands) {
					if (commandTarget == null || block.getLocation().distanceSquared(commandTarget.getLocation()) < commandDistanceSquared) {
						commandTarget = block;
					}
				}
				block.setType(birthMaterial);
				controller.updateBlock(block);
			}
			
			updatingIndex++;
			if (updatingIndex > deadBlocks.size() + bornBlocks.size()) {
				state = SimulationState.COMMAND;
				
				// Wait at least a tick before re-populating the command block.
				return maxBlocks;
			}
		}
		
		if (state == SimulationState.COMMAND) {
			if (commandTarget != null && commandDelay == 0) {
				commandTarget.setType(Material.AIR);
			}
			commandDelay++;
			if (commandDelay >= COMMAND_UPDATE_DELAY) {
				if (commandTarget != null) {
					commandTarget.setType(Material.COMMAND);
					BlockState commandData = commandTarget.getState();
					if (castCommand != null && commandData != null && commandData instanceof CommandBlock) {
						CommandBlock copyCommand = (CommandBlock)commandData;
						copyCommand.setCommand(castCommand);
						copyCommand.update();
						
						// Continue to power the command block if it was initially powered by a 
						// redstone block above or below it.
						// because this can cause overlapping commands and badness.
						if (powerDirection != null) {
							Block powerBlock = commandTarget.getRelative(powerDirection);
							if (spell.isDestructible(powerBlock)) {
								commandTarget.getRelative(powerDirection).setType(POWER_MATERAIL);
								modifiedBlocks.add(powerBlock);
							}
						}
					}
				}
				state = SimulationState.CLEANUP;
			}
			return maxBlocks;
		}
		
		if (state == SimulationState.CLEANUP) {
			// Remove the power block if the simulation has collapsed.
			if (blockCount == 0 && powerDirection != null && commandTarget != null && commandTarget.getRelative(powerDirection).getType() == POWER_MATERAIL) {
				Block powerBlock = commandTarget.getRelative(powerDirection);
				modifiedBlocks.add(powerBlock);
				powerBlock.setType(Material.AIR);
			}
			state = SimulationState.FINISHED;
			return maxBlocks;
		}
		
		if (state == SimulationState.FINISHED) {
			finish();
		}
		
		return processedBlocks;
	}

	public void setCommandBlock(Block block) {
		castCommandBlock = block;
		if (castCommandBlock.getType() == Material.COMMAND) {
			BlockState commandData = castCommandBlock.getState();
			if (commandData != null && commandData instanceof CommandBlock) {
				castCommand = ((CommandBlock)commandData).getCommand();
			}
			includeCommands = castCommand != null && castCommand.length() > 0;
		}
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
	
	@Override
	public void finish() {
		if (!finished) {
			super.finish();
			mage.registerForUndo(modifiedBlocks);
		}
	}
}
