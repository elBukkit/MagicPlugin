package com.elmakers.mine.bukkit.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
	private static BlockFace[] powerFaces = { BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN, BlockFace.UP };
	
	private enum SimulationState {
		SCANNING_COMMAND, SCANNING, UPDATING, COMMAND, FINISHED
	};
	
	public static Material POWER_MATERIAL = Material.REDSTONE_BLOCK;
	
	private static int COMMAND_UPDATE_DELAY = 0;
	private static int COMMAND_POWER_DELAY = 1;
	
	private Mage mage;
	private BlockSpell spell;
	private Block castCommandBlock;
	private Block commandTargetBlock;
	private int commandDistanceSquared;
	private String castCommand;
	private String commandName;
	private int commandMoveRangeSquared = 9;
	private boolean commandDrift;
	private boolean commandReload;
	private boolean commandPowered;
	private World world;
	private Material birthMaterial;
	private Material deathMaterial;
	private Material powerSimMaterialBackup;
	private Material powerSimMaterial;
	private boolean includeCommands;
	private int startX;
	private int startZ;
	private int startY;
	private int endY;
	private int endX;
	private int endZ;
	private int x;
	private int y;
	private int z;
	private int yRadius;
	private int updatingIndex;
	private int commandDelay;
	private ArrayList<Boolean> liveCounts = new ArrayList<Boolean>();
	private ArrayList<Boolean> birthCounts = new ArrayList<Boolean>();
	private Random random = new Random();
	private SimulationState state;

	private List<Block> deadBlocks = new ArrayList<Block>();
	private List<Block> bornBlocks = new ArrayList<Block>();
	private List<Block> potentialCommandBlocks = new ArrayList<Block>();
	private BlockList modifiedBlocks = new BlockList();
	
	public SimulateBatch(BlockSpell spell, Location center, int radius, int yRadius, Material birth, Material death, Set<Integer> liveCounts, Set<Integer> birthCounts) {
		super(spell.getMage().getController(), center.getWorld().getName());
		this.spell = spell;
		this.mage = spell.getMage();
		this.yRadius = yRadius;
		
		this.birthMaterial = birth;
		this.deathMaterial = death;
		
		this.powerSimMaterial = birthMaterial;
		this.powerSimMaterialBackup = deathMaterial;
		
		for (Integer liveCount : liveCounts) {
			while (this.liveCounts.size() < liveCount) {
				this.liveCounts.add(false);
			}
			this.liveCounts.add(true);
		}
		for (Integer birthCount : birthCounts) {
			while (this.birthCounts.size() < birthCount) {
				this.birthCounts.add(false);
			}
			this.birthCounts.add(true);
		}
		this.world = center.getWorld();
		includeCommands = false;
		commandDrift = false;
		
		startY = center.getBlockY() - yRadius;
		endY = center.getBlockY() + yRadius;
		startX = center.getBlockX() - radius;
		startZ = center.getBlockZ() - radius;
		endX = center.getBlockX() + radius;
		endZ = center.getBlockZ() + radius;
		
		x = startX;
		y = startY;
		z = startZ;
		
		state = SimulationState.SCANNING_COMMAND;
		updatingIndex = 0;
		commandDistanceSquared = 0;
		commandDelay = 0;
	}

	public int size() {
		return modifiedBlocks.size();
	}
	
	public int remaining() {
		return (endX - x) * (endZ - z) * (endY - y);
	}
	
	protected void checkForPotentialCommand(Block block) {
		if (includeCommands) {
			int distanceSquared = (int)Math.floor(block.getLocation().distanceSquared(castCommandBlock.getLocation()));
			if (
			(
				commandTargetBlock == null ||
				distanceSquared < commandDistanceSquared)
			)
			{
				commandTargetBlock = block;
				commandDistanceSquared = distanceSquared;
			}
			
			if (distanceSquared < commandMoveRangeSquared) {
				potentialCommandBlocks.add(block);
			}
		}
	}

	@Override
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		if (state == SimulationState.SCANNING_COMMAND) {
			// Process the casting command block first, and only if specially configured to do so.
			if (includeCommands && castCommandBlock != null) {
				// We are going to rely on the block toggling to kick this back to life when the chunk
				// reloads, so for now just bail and hope the timing works out.
				if (!castCommandBlock.getChunk().isLoaded()) {
					finish();
					// TODO: Maybe Scatter-shot and register all 6 surrounding power blocks for reload toggle.
					// Can't really do it without the chunk being loaded though, so hrm.
					return processedBlocks;
				}
				
				commandTargetBlock = castCommandBlock;
				
				// Check for power blocks
				for (BlockFace powerFace : powerFaces) {
					Block checkForPower = castCommandBlock.getRelative(powerFace);
					if (checkForPower.getType() == POWER_MATERIAL) {
						if (commandReload) {
							controller.unregisterBlockForReloadToggle(checkForPower);
						}
						checkForPower.setType(powerSimMaterial);
						commandPowered = true;
					}
				}
				
				// Make this a normal block so the sim will process it
				// this also serves to reset the command block for the next tick, if it lives.
				castCommandBlock.setType(birthMaterial);
			}
			
			processedBlocks++;
			state = SimulationState.SCANNING;
		}
		
		while (state == SimulationState.SCANNING && processedBlocks <= maxBlocks) {
			Block block = world.getBlockAt(x, y, z);
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
				return processedBlocks;
			}
			
			Material blockMaterial = block.getType();
			if (blockMaterial == birthMaterial) {
				int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
				if (neighborCount >= liveCounts.size() || !liveCounts.get(neighborCount)) {
					deadBlocks.add(block);
				} else {
					checkForPotentialCommand(block);
				}
			} else if (blockMaterial == deathMaterial) {
				int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
				if (neighborCount < birthCounts.size() && birthCounts.get(neighborCount)) {
					bornBlocks.add(block);
					checkForPotentialCommand(block);
				}
			}
			
			z++;
			if (z > endZ) {
				z = startZ;
				x++;
			}
			if (x > endX) {
				x = startX;
				z = startZ;
				y++;
			}
			
			if (y > endY) {
				state = SimulationState.UPDATING;
			}
		}
		
		while (state == SimulationState.UPDATING && processedBlocks <= maxBlocks) {
			int deadIndex = updatingIndex;
			if (deadIndex >= 0 && deadIndex < deadBlocks.size()) {
				Block killBlock = deadBlocks.get(deadIndex);
				if (!killBlock.getChunk().isLoaded()) {
					killBlock.getChunk().load();
					return processedBlocks;
				}
				modifiedBlocks.add(killBlock);
				killBlock.setType(deathMaterial);
				controller.updateBlock(killBlock);
				processedBlocks++;
			}
			
			int bornIndex = updatingIndex - deadBlocks.size();
			if (bornIndex >= 0 && bornIndex < bornBlocks.size()) {
				Block birthBlock = bornBlocks.get(bornIndex);
				if (!birthBlock.getChunk().isLoaded()) {
					birthBlock.getChunk().load();
					return processedBlocks;
				}
				modifiedBlocks.add(birthBlock);
				birthBlock.setType(birthMaterial);
				controller.updateBlock(birthBlock);
			}
			
			updatingIndex++;
			if (updatingIndex >= deadBlocks.size() + bornBlocks.size()) {
				state = SimulationState.COMMAND;
				
				// Wait at least a tick before re-populating the command block.
				return maxBlocks;
			}
		}
		
		if (state == SimulationState.COMMAND) {
			if (commandDelay == 0 && includeCommands && commandTargetBlock != null) {
				if (!commandTargetBlock.getChunk().isLoaded()) {
					commandTargetBlock.getChunk().load();
					return processedBlocks;
				}
				// Find a valid block for the command
				Block testBlock = commandTargetBlock;
				if ((testBlock == null || commandDrift || testBlock.getType() != birthMaterial) && potentialCommandBlocks.size() > 0) {
					testBlock = findPotentialCommandLocation();
				}
				
				if (testBlock != null) {
					if (commandPowered) {
						BlockFace powerFace = findPowerLocation(testBlock, powerSimMaterial);
						while (powerFace == null && potentialCommandBlocks.size() > 0) {
							testBlock = findPotentialCommandLocation();
							powerFace = findPowerLocation(testBlock, powerSimMaterial);
						}
						
						if (powerFace != null) {
							commandTargetBlock = testBlock;
						}
					} else {
						commandTargetBlock = testBlock;
					}
				}
			}
			if (commandDelay == COMMAND_UPDATE_DELAY && commandTargetBlock != null && includeCommands) {
				commandTargetBlock.setType(Material.COMMAND);
				BlockState commandData = commandTargetBlock.getState();
				if (castCommand != null && commandData != null && commandData instanceof CommandBlock) {
					CommandBlock copyCommand = (CommandBlock)commandData;
					copyCommand.setCommand(castCommand);
					copyCommand.setName(commandName);
					copyCommand.update();
					
					// Also move the mage
					mage.setLocation(commandTargetBlock.getLocation());
				} else {
					commandTargetBlock = null;
				}
			}
			if (commandDelay >= COMMAND_POWER_DELAY) {
				// Continue to power the command block
				// Find a new direction, replace existing block
				if (commandPowered && commandTargetBlock != null && includeCommands) {
					// First try and replace a live cell
					BlockFace powerDirection = findPowerLocation(commandTargetBlock, powerSimMaterial);
					// Next try to replace a dead cell, which will affect the simulation outcome
					// but this is perhaps better than it dying?
					if (powerDirection == null) {
						powerDirection = findPowerLocation(commandTargetBlock, powerSimMaterialBackup);
					}
					// If it's *still* not valid, search for something breakable.
					for (BlockFace face : powerFaces) {
						if (spell.isDestructible(commandTargetBlock.getRelative(face))) {
							powerDirection = face;
							break;
						}
					}
					
					if (powerDirection != null) {
						Block powerBlock = commandTargetBlock.getRelative(powerDirection);
						powerBlock.setType(POWER_MATERIAL);
						if (commandReload) {
							controller.registerBlockForReloadToggle(powerBlock);
						}
					}
				}
				state = SimulationState.FINISHED;
			}
			commandDelay++;
			return maxBlocks;
		}
		
		if (state == SimulationState.FINISHED) {
			finish();
		}
		
		return processedBlocks;
	}
	
	protected Block findPotentialCommandLocation() {
		Block potential = null;
		if (potentialCommandBlocks.size() > 0) {
			int index = random.nextInt(potentialCommandBlocks.size());
			potential = potentialCommandBlocks.get(index);
			potentialCommandBlocks.remove(index);
		}
		return potential;
	}

	public void setCommandBlock(Block block) {
		castCommandBlock = block;
		if (castCommandBlock.getType() == Material.COMMAND) {
			BlockState commandData = castCommandBlock.getState();
			if (commandData != null && commandData instanceof CommandBlock) {
				CommandBlock commandBlock = ((CommandBlock)commandData);
				castCommand = commandBlock.getCommand();
				commandName = commandBlock.getName();
			}
			includeCommands = castCommand != null && castCommand.length() > 0;
		}
	}
	
	public void setCommandMoveRange(int commandRadius, boolean drift, boolean reload) {
		commandDrift = drift;
		commandReload = reload;
		commandMoveRangeSquared = commandRadius * commandRadius;
	}
	
	protected boolean isAlive(Block block, Material liveMaterial, boolean includeCommands)
	{
		Material neighborType = block.getType();
		return (neighborType == liveMaterial || (includeCommands && (neighborType == Material.COMMAND || neighborType == POWER_MATERIAL)));
	}
	
	protected BlockFace findPowerLocation(Block block, Material targetMaterial) {
		for (BlockFace face : powerFaces) {
			if (block.getRelative(face).getType() == targetMaterial) {
				return face;
			}
		}
		return null;
	}

	protected int getNeighborCount(Block block, Material liveMaterial, boolean includeCommands) {
		int liveCount = 0;
		for (BlockFace face : neighborFaces) {
			if (isAlive(block.getRelative(face), liveMaterial, includeCommands)) {
				liveCount++;
			}
		}
		
		if (yRadius > 0) {
			Block upBlock = block.getRelative(BlockFace.UP);
			if (isAlive(upBlock, liveMaterial, includeCommands)) {
				liveCount++;
			}
			for (BlockFace face : neighborFaces) {
				if (isAlive(upBlock.getRelative(face), liveMaterial, includeCommands)) {
					liveCount++;
				}
			}

			Block downBlock = block.getRelative(BlockFace.DOWN);
			if (isAlive(downBlock, liveMaterial, includeCommands)) {
				liveCount++;
			}
			for (BlockFace face : neighborFaces) {
				if (isAlive(downBlock.getRelative(face), liveMaterial, includeCommands)) {
					liveCount++;
				}
			}
		}
		
		return liveCount;
	}
	
	@Override
	public void finish() {
		state = SimulationState.FINISHED;
		if (!finished) {
			super.finish();
			mage.registerForUndo(modifiedBlocks);
		}
	}
}
