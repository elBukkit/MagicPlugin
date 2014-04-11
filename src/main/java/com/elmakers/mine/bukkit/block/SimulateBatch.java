package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.Messages;
import com.elmakers.mine.bukkit.utilities.RandomUtils;
import com.elmakers.mine.bukkit.utilities.Target;

public class SimulateBatch extends VolumeBatch {
	private static BlockFace[] neighborFaces = { BlockFace.NORTH, BlockFace.NORTH_EAST, 
		BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
	};
	private static BlockFace[] POWER_FACES = { BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN, BlockFace.UP };
	
	private enum SimulationState {
		SCANNING_COMMAND, SCANNING, UPDATING, COMMAND_SEARCH, COMMAND_UPDATE, COMMAND_POWER, FINISHED
	};
	
	public enum TargetMode {
		STABILIZE, WANDER, GLIDE, HUNT, FLEE, DIRECTED
	};
	
	public enum TargetType {
		PLAYER, MAGE, MOB, AUTOMATON, ANY
	};
	
	public static Material POWER_MATERIAL = Material.REDSTONE_BLOCK;
	
	public static boolean DEBUG = false;
	
	private Mage mage;
	private BlockSpell spell;
	private Block castCommandBlock;
	private Block commandTargetBlock;
	private TargetMode targetMode = TargetMode.STABILIZE;
	private TargetType targetType = TargetType.PLAYER;
	private Location targetLocation;
	private String castCommand;
	private String commandName;
	private String tickSpell;
	private String deathSpell;
	private float castProbability;
	private String dropItem;
	private boolean reverseTargetDistanceScore = false;
	private int commandMoveRangeSquared = 9;
	private int huntMaxRange = 128;
	private int huntMinRange = 4;
	private int birthRangeSquared = 0;
	private int liveRangeSquared = 0;
	
	// 3.0 is roughly 180 degrees, gives him a wider FOV
	private float huntFov = 3.0f;
	private boolean commandReload;
	private boolean commandPowered;
	private World world;
	private MaterialAndData birthMaterial;
	private Material deathMaterial;
	private MaterialAndData powerSimMaterialBackup;
	private MaterialAndData powerSimMaterial;
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
	
	public SimulateBatch(BlockSpell spell, Location center, int radius, int yRadius, MaterialAndData birth, Material death, Set<Integer> liveCounts, Set<Integer> birthCounts) {
		super(spell.getMage().getController(), center.getWorld().getName());
		this.spell = spell;
		this.mage = spell.getMage();
		this.yRadius = yRadius;
		this.center = center.clone();
		this.targetLocation = center.clone();
		
		this.birthMaterial = birth;
		this.deathMaterial = death;
		
		this.powerSimMaterial = birthMaterial;
		this.powerSimMaterialBackup = new MaterialAndData(deathMaterial);
		
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
	
	protected void checkForPotentialCommand(Block block, int distanceSquared) {
		if (includeCommands) {
			if (distanceSquared < commandMoveRangeSquared) {
				// commandMoveRangeSquared is kind of too big, but it doesn't matter all that much
				// we still look at targets that end up with a score of 0, it just affects the sort ordering.
				potentialCommandBlocks.add(new Target(targetLocation, block, huntMinRange, huntMaxRange, huntFov, reverseTargetDistanceScore));
			}
		}
	}
	
	protected void die() {
		String message = spell.getMessage("death_broadcast").replace("$name", commandName);
		if (message.length() > 0) {
			controller.sendToMages(message, center);	
		}
		if (dropItem != null && dropItem.length() > 0) {
			Wand magicItem = Wand.createWand(controller, dropItem);
			if (magicItem != null) {
				center.getWorld().dropItemNaturally(center, magicItem.getItem());
			}
		}
		if (deathSpell.length() > 0) {
			String spellName = deathSpell;
			String[] defaultParameters = {"cost_reduction", "1", "destructible", "air," + birthMaterial.getMaterial().name().toLowerCase(), "target", "self"};
			String[] parameters = defaultParameters;
			
			String[] pieces = StringUtils.split(deathSpell, " ");
			if (pieces != null && pieces.length > 1) {
				spellName = pieces[0];
				parameters = new String[defaultParameters.length + pieces.length - 1];
				for (int i = 0; i < defaultParameters.length; i++) {
					parameters[i] = defaultParameters[i];
				}
				for (int i = 1; i < pieces.length; i++) {
					parameters[i + defaultParameters.length - 1] = pieces[i];
				}
			}
			Spell spell = mage.getSpell(spellName);
			if (spell != null) {
				if (DEBUG) {
					controller.getLogger().info(commandName + " casting " + tickSpell + " on death");
				}
				spell.cast(parameters);
			}
		}
		if (includeCommands && castCommandBlock != null && castCommandBlock.getType() == Material.COMMAND) {
			castCommandBlock.setType(Material.AIR);
		}
		if (!mage.isPlayer()) {
			controller.forgetMage(mage);
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
				
				// Check for death since activation (e.g. during delay period)
				if (castCommandBlock.getType() != Material.COMMAND) {
					die();
					finish();
					return processedBlocks;
				}
				
				// Check for power blocks
				for (BlockFace powerFace : POWER_FACES) {
					Block checkForPower = castCommandBlock.getRelative(powerFace);
					if (checkForPower.getType() == POWER_MATERIAL) {
						if (commandReload) {
							controller.unregisterBlockForReloadToggle(checkForPower);
						}
						powerSimMaterial.modify(checkForPower);
						commandPowered = true;
					}
				}
				
				if (!commandPowered) {
					die();
					finish();
					return processedBlocks;
				}
				
				// Make this a normal block so the sim will process it
				// this also serves to reset the command block for the next tick, if it lives.
				birthMaterial.modify(castCommandBlock);
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
			if (birthMaterial.is(block)) {
				int distanceSquared = liveRangeSquared > 0 || includeCommands ? 
						(int)Math.floor(block.getLocation().distanceSquared(castCommandBlock.getLocation())) : 0;

				if (liveRangeSquared <= 0 || distanceSquared <= liveRangeSquared) {
					int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
					if (neighborCount >= liveCounts.size() || !liveCounts.get(neighborCount)) {
						deadBlocks.add(block);
					} else {
						checkForPotentialCommand(block, distanceSquared);
					}
				} else {
					deadBlocks.add(block);
				}
			} else if (blockMaterial == deathMaterial) {
				int distanceSquared = birthRangeSquared > 0 || includeCommands ? 
						(int)Math.floor(block.getLocation().distanceSquared(castCommandBlock.getLocation())) : 0;

				if (birthRangeSquared <= 0 || distanceSquared <= birthRangeSquared) {	
					int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
					if (neighborCount < birthCounts.size() && birthCounts.get(neighborCount)) {
						bornBlocks.add(block);
						checkForPotentialCommand(block, distanceSquared);
					}
				}
			} else if (includeCommands && blockMaterial == Material.COMMAND && commandName != null && commandName.length() > 1) {
				// Absorb nearby commands of the same name.
				BlockState commandData = block.getState();
				if (commandData != null && commandData instanceof CommandBlock) {
					CommandBlock commandBlock = ((CommandBlock)commandData);
					if (commandBlock.getName().equals(commandName)) {
						block.setType(deathMaterial);
						if (DEBUG) {
							controller.getLogger().info("CONSUMED clone at " + block.getLocation().toVector());
						}
					}
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
				birthMaterial.modify(birthBlock);
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
				case HUNT:
					Collections.sort(potentialCommandBlocks);
					break;
				case FLEE:
					Collections.sort(potentialCommandBlocks);
					break;
				default:
					Collections.shuffle(potentialCommandBlocks);
					break;
				}
				
				// Find a valid block for the command
				commandTargetBlock = null;
				Block backupBlock = null;
				while (commandTargetBlock == null && potentialCommandBlocks.size() > 0) {
					Block block = potentialCommandBlocks.remove(0).getBlock();
					if (block != null && birthMaterial.is(block)) {
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
			if (DEBUG) {
				if (commandTargetBlock != null) {
					controller.getLogger().info("MOVED: " + commandTargetBlock.getLocation().toVector().subtract(center.toVector()));
				}
			}
			state = SimulationState.COMMAND_UPDATE;
			return processedBlocks;
		}
		
		if (state == SimulationState.COMMAND_UPDATE) {
			if (includeCommands) {
				if (commandTargetBlock != null) {
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
						Location newLocation = commandTargetBlock.getLocation();
						newLocation.setPitch(center.getPitch());
						newLocation.setYaw(center.getYaw());
						mage.setLocation(newLocation);
					} else {
						commandTargetBlock = null;
					}
				} else {
					die();
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
					if (DEBUG) {
						controller.getLogger().info("Had to fall back to backup location, pattern may diverge");
					}
					powerDirection = findPowerLocation(commandTargetBlock, powerSimMaterialBackup);
				}
				// If it's *still* not valid, search for something breakable.
				if (powerDirection == null) {
					for (BlockFace face : POWER_FACES) {
						if (spell.isDestructible(commandTargetBlock.getRelative(face))) {
							if (DEBUG) {
								controller.getLogger().info("Had to fall back to destructible location, pattern may diverge and may destroy blocks");
							}
							powerDirection = face;
							break;
						}
					}
				}
				
				if (powerDirection != null) {
					Block powerBlock = commandTargetBlock.getRelative(powerDirection);
					powerBlock.setType(POWER_MATERIAL);
					if (commandReload) {
						String automataName = commandName;
						if (automataName == null || automataName.length() <= 1) {
							automataName = Messages.get("automata.default_name");
						}
						controller.registerBlockForReloadToggle(powerBlock, automataName, "automata.awaken");
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
	
	public void setDrop(String dropName) {
		this.dropItem = dropName;
	}
	
	public void setTickCast(String cast, float probability) {
		tickSpell = cast;
		castProbability = probability;
	}
	
	public void setDeathCast(String cast) {
		deathSpell = cast;
	}
	
	public void setBirthRange(int range) {
		birthRangeSquared = range * range;
	}

	public void setLiveRange(int range) {
		liveRangeSquared = range * range;
	}
	
	public void setMaxHuntRange(int range) {
		huntMaxRange = range;
	}

	public void setMinHuntRange(int range) {
		huntMinRange = range;
	}
	
	public void setTargetType(TargetType targetType) {
		this.targetType = targetType;
	}
	
	public void target(TargetMode mode) {
		targetMode = mode == null ? TargetMode.STABILIZE : mode;
		switch (targetMode) 
		{
		case FLEE:
		case HUNT:
		case DIRECTED:
			targetLocation = center.clone();
			reverseTargetDistanceScore = targetMode == TargetMode.FLEE;
			Target bestTarget = null;
			if (targetType == TargetType.ANY || targetType == TargetType.MOB)
			{
				List<Entity> entities = targetLocation.getWorld().getEntities();
				for (Entity entity : entities)
				{
					// We'll get the players from the Mages list
					if (entity instanceof Player || !(entity instanceof LivingEntity) || entity.isDead()) continue;
					Target newScore = new Target(center, entity, huntMinRange, huntMaxRange, huntFov, false);
					int score = newScore.getScore();
					if (bestTarget == null || score > bestTarget.getScore()) {
						bestTarget = newScore;
					}
				}
			}
			if (targetType == TargetType.MAGE || targetType == TargetType.AUTOMATON || targetType == TargetType.ANY || targetType == TargetType.PLAYER)
			{
				Collection<Mage> mages = controller.getMages();
				for (Mage mage : mages)
				{
					if (mage == this.mage) continue;
					if (targetType == TargetType.AUTOMATON && mage.getPlayer() != null) continue;
					if (mage.isDead() || !mage.isOnline() || !mage.hasLocation()) continue;
					
					Target newScore = new Target(center, mage, huntMinRange, huntMaxRange, huntFov, false);
					int score = newScore.getScore();
					if (bestTarget == null || score > bestTarget.getScore()) {
						bestTarget = newScore;
					}
				}
			}
			
			if (bestTarget != null) 
			{
				String targetDescription = bestTarget.getEntity() == null ? "NONE" :
					((bestTarget instanceof Player) ? ((Player)bestTarget.getEntity()).getName() : bestTarget.getEntity().getType().name());
				
				if (DEBUG) {
					controller.getLogger().info(" *Tracking " + targetDescription + 
				 		" score: " + bestTarget.getScore() + " location: " + center + " -> " + bestTarget.getLocation() + " min " + huntMinRange);
				}
				if (targetMode == TargetMode.DIRECTED) {
					Vector direction = bestTarget.getLocation().getDirection();
					center = RandomUtils.setDirection(center, direction);
					targetLocation = center.clone().add(direction.normalize().multiply(huntMaxRange));
					if (DEBUG) {
						controller.getLogger().info(" *Directed " + direction + " to " + targetLocation.toVector()); 
					}
				} else {
					targetLocation = bestTarget.getLocation();
					Vector direction = targetMode == TargetMode.FLEE ? center.toVector().subtract(targetLocation.toVector()) : targetLocation.toVector().subtract(center.toVector());
					center = RandomUtils.setDirection(center, direction);
				}
				
				if (tickSpell.length() > 0) {
					if (Math.random() < castProbability) {
						Spell spell = mage.getSpell(tickSpell);
						if (spell != null) {
							if (DEBUG) {
								controller.getLogger().info(commandName + " casting " + tickSpell + " at " + targetDescription);
							}
							String[] parameters = {"cost_reduction", "1", "target_through", "air," + birthMaterial.getMaterial().name().toLowerCase() + "," + Material.COMMAND.name().toLowerCase() + "," + POWER_MATERIAL.name().toLowerCase()};
							spell.cast(parameters);
						}
					}
				}
			}
			break;
		case GLIDE:
			targetLocation = center.clone();
			reverseTargetDistanceScore = true;
			break;
		default:
			targetLocation = center.clone();
			reverseTargetDistanceScore = true;
		}
	}
	
	public void setCommandMoveRange(int commandRadius, boolean reload) {
		commandReload = reload;
		commandMoveRangeSquared = commandRadius * commandRadius;
	}
	
	protected boolean isAlive(Block block, MaterialAndData liveMaterial, boolean includeCommands)
	{
		Material neighborType = block.getType();
		return (liveMaterial.is(block) || (includeCommands && (neighborType == Material.COMMAND || neighborType == POWER_MATERIAL)));
	}
	
	public static BlockFace findPowerLocation(Block block, MaterialAndData targetMaterial) {
		for (BlockFace face : POWER_FACES) {
			if (targetMaterial.is(block.getRelative(face))) {
				return face;
			}
		}
		return null;
	}

	protected int getNeighborCount(Block block, MaterialAndData liveMaterial, boolean includeCommands) {
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
			spell.registerForUndo(modifiedBlocks);
		}
	}
}
