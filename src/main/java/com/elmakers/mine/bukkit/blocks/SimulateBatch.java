package com.elmakers.mine.bukkit.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;

import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.utilities.Target;

public class SimulateBatch extends VolumeBatch {
	private static BlockFace[] neighborFaces = { BlockFace.NORTH, BlockFace.NORTH_EAST, 
		BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
	};
	private static BlockFace[] powerFaces = { BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN, BlockFace.UP };
	
	private enum SimulationState {
		SCANNING_COMMAND, SCANNING, UPDATING, COMMAND_SEARCH, COMMAND_UPDATE, COMMAND_POWER, FINISHED
	};
	
	public enum TargetMode {
		STABILIZE, WANDER, HUNT
	};
	
	public static Material POWER_MATERIAL = Material.REDSTONE_BLOCK;
	
	private Mage mage;
	private BlockSpell spell;
	private Block castCommandBlock;
	private Block commandTargetBlock;
	private TargetMode targetMode = TargetMode.STABILIZE;
	private String castCommand;
	private String commandName;
	private int commandMoveRangeSquared = 9;
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
	private ArrayList<Boolean> liveCounts = new ArrayList<Boolean>();
	private ArrayList<Boolean> birthCounts = new ArrayList<Boolean>();
	private SimulationState state;
	private Location center;

	private List<Block> deadBlocks = new ArrayList<Block>();
	private List<Block> bornBlocks = new ArrayList<Block>();
	private List<Target> potentialCommandBlocks = new LinkedList<Target>();
	private BlockList modifiedBlocks = new BlockList();
	
	public SimulateBatch(BlockSpell spell, Location center, int radius, int yRadius, Material birth, Material death, Set<Integer> liveCounts, Set<Integer> birthCounts) {
		super(spell.getMage().getController(), center.getWorld().getName());
		this.spell = spell;
		this.mage = spell.getMage();
		this.yRadius = yRadius;
		this.center = center.clone();
		
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
			if (distanceSquared < commandMoveRangeSquared) {
				potentialCommandBlocks.add(new Target(center, block));
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
				state = SimulationState.COMMAND_SEARCH;
				
				// Wait at least a tick before re-populating the command block.
				return maxBlocks;
			}
		}
		
		// Each of the following states will end in this tick, to give the
		// MC sim time to register power updates.
		if (state == SimulationState.COMMAND_SEARCH) {
			if (includeCommands && potentialCommandBlocks.size() > 0) {
				switch (targetMode) {
				case STABILIZE:
					Collections.sort(potentialCommandBlocks);
					break;
				case WANDER:
					Collections.shuffle(potentialCommandBlocks);
					break;
				case HUNT:
					// TODO: Entity targeting
					Collections.shuffle(potentialCommandBlocks);
					break;
				}
				
				// Find a valid block for the command
				commandTargetBlock = null;
				Block backupBlock = null;
				while (commandTargetBlock == null && potentialCommandBlocks.size() > 0) {
					Block block = potentialCommandBlocks.remove(0).getBlock();
					if (block != null && block.getType() == birthMaterial) {
						// If we're powering the block, look for one with a powerable neighbor.
						if (!commandPowered) {
							commandTargetBlock = block;
						} else {
							backupBlock = block;
							BlockFace powerFace = findPowerLocation(block, powerSimMaterial);
							if (powerFace != null) {
								commandTargetBlock = block;
							}
						}
					}
				}
				
				// If we didn't find any powerable blocks, but we did find at least one valid sim block
				// just use that one.
				if (commandTargetBlock == null) commandTargetBlock = backupBlock;
			}
			state = SimulationState.COMMAND_UPDATE;
			return processedBlocks;
		}
		
		if (state == SimulationState.COMMAND_UPDATE) {
			if (includeCommands && commandTargetBlock != null) {
				if (!commandTargetBlock.getChunk().isLoaded()) {
					commandTargetBlock.getChunk().load();
					return processedBlocks;
				}
				
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
			state = SimulationState.COMMAND_POWER;
			return processedBlocks;
		}
		
		if (state == SimulationState.COMMAND_POWER) {
			// Continue to power the command block
			// Find a new direction, replace existing block
			if (commandPowered && commandTargetBlock != null && includeCommands) {
				// First try and replace a live cell
				BlockFace powerDirection = findPowerLocation(commandTargetBlock, powerSimMaterial);
				// Next try to replace a dead cell, which will affect the simulation outcome
				// but this is perhaps better than it dying?
				if (powerDirection == null) {
					Bukkit.getLogger().info("Had to fall back to backup location, pattern may diverge");
					powerDirection = findPowerLocation(commandTargetBlock, powerSimMaterialBackup);
				}
				// If it's *still* not valid, search for something breakable.
				if (powerDirection == null) {
					for (BlockFace face : powerFaces) {
						if (spell.isDestructible(commandTargetBlock.getRelative(face))) {
							Bukkit.getLogger().info("Had to fall back to destructible location, pattern may diverge and may destroy blocks");
							powerDirection = face;
							break;
						}
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
			return processedBlocks;
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
				CommandBlock commandBlock = ((CommandBlock)commandData);
				castCommand = commandBlock.getCommand();
				commandName = commandBlock.getName();
			}
			includeCommands = castCommand != null && castCommand.length() > 0;
		}
	}
	
	public void setCommandMoveRange(int commandRadius, boolean reload, TargetMode mode) {
		targetMode = mode == null ? TargetMode.STABILIZE : mode;
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
