package com.elmakers.mine.bukkit.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.block.UndoList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.AutomatonLevel;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class SimulateBatch extends SpellBatch {
	private static BlockFace[] NEIGHBOR_FACES = { BlockFace.NORTH, BlockFace.NORTH_EAST, 
		BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
	};
	private static BlockFace[] DIAGONAL_FACES = {  BlockFace.SOUTH_EAST, BlockFace.NORTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST };
	private static BlockFace[] MAIN_FACES = {  BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };
	private static BlockFace[] POWER_FACES = { BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN, BlockFace.UP };
	
	private enum SimulationState {
		SCANNING_COMMAND, SCANNING, UPDATING, COMMAND_SEARCH, COMMON_RESET_REDSTONE, COMMAND_UPDATE, COMMAND_POWER, FINISHED
	};
	
	public enum TargetMode {
		STABILIZE, WANDER, GLIDE, HUNT, FLEE, DIRECTED
	};
	
	public enum TargetType {
		PLAYER, MAGE, MOB, AUTOMATON, ANY
	};
	
	public static Material POWER_MATERIAL = Material.REDSTONE_BLOCK;
	public static int POWER_DELAY_TICKS = 0;
	
	public static boolean DEBUG = false;
	
	private Mage mage;
	private BlockSpell blockSpell;
	private Block castCommandBlock;
	private Block commandTargetBlock;
    private Block powerTargetBlock;
	private TargetMode targetMode = TargetMode.STABILIZE;
	private TargetType targetType = TargetType.PLAYER;
	private String castCommand;
	private String commandName;
	private AutomatonLevel level;
	private String dropItem;
	private int dropXp;
	private boolean reverseTargetDistanceScore = false;
	private boolean concurrent = false;
	private int commandMoveRangeSquared = 9;
	private int huntMaxRange = 128;
	private int castRange = 32;
	private int huntMinRange = 4;
	private int birthRangeSquared = 0;
	private int liveRangeSquared = 0;
	private double huntFov = Math.PI * 1.8;
	private boolean commandReload;
	private boolean commandPowered;
	private World world;
	private MaterialAndData birthMaterial;
	private Material deathMaterial;
	private MaterialAndData powerSimMaterialBackup;
	private MaterialAndData powerSimMaterial;
	private boolean includeCommands;
	private int radius;
	private int x;
	private int y;
	private int z;
	private int r;
	private int yRadius;
	private int updatingIndex;
	private int powerDelayTicks;
	private ArrayList<Boolean> liveCounts = new ArrayList<Boolean>();
	private ArrayList<Boolean> birthCounts = new ArrayList<Boolean>();
	private ArrayList<Boolean> diagonalLiveCounts = new ArrayList<Boolean>();
	private ArrayList<Boolean> diagonalBirthCounts = new ArrayList<Boolean>();
	private SimulationState state;
	private Location center;

	private List<Block> deadBlocks = new ArrayList<Block>();
	private List<Block> bornBlocks = new ArrayList<Block>();
	private List<Target> potentialCommandBlocks = new LinkedList<Target>();
	
	public SimulateBatch(BlockSpell spell, Location center, int radius, int yRadius, MaterialAndData birth, Material death, Set<Integer> liveCounts, Set<Integer> birthCounts) {
		super(spell);

		this.blockSpell = spell;
		this.mage = spell.getMage();
		this.yRadius = yRadius;
		this.radius = radius;
		this.center = center.clone();
		
		this.birthMaterial = birth;
		this.deathMaterial = death;
		
		this.powerSimMaterial = birthMaterial;
		this.powerSimMaterialBackup = new MaterialAndData(deathMaterial);
		mapIntegers(liveCounts, this.liveCounts);
		mapIntegers(birthCounts, this.birthCounts);
		this.world = center.getWorld();
		includeCommands = false;
		
		x = 0;
		y = 0;
		z = 0;
		r = 0;
		
		state = SimulationState.SCANNING_COMMAND;
		updatingIndex = 0;
	}

	public int size() {
		return radius * radius * radius * 8;
	}
	
	public int remaining() {
		if (r >= radius) return 0;
		return (radius - r) *  (radius - r) *  (radius - r) * 8;
	}
	
	protected void checkForPotentialCommand(Block block, int distanceSquared) {
		if (includeCommands) {
			if (distanceSquared <= commandMoveRangeSquared) {
				// commandMoveRangeSquared is kind of too big, but it doesn't matter all that much
				// we still look at targets that end up with a score of 0, it just affects the sort ordering.
				potentialCommandBlocks.add(new Target(center, block, huntMinRange, huntMaxRange, huntFov, reverseTargetDistanceScore));
			}
		}
	}
	
	protected void die() {
		String message = spell.getMessage("death_broadcast").replace("$name", commandName);
		if (message.length() > 0) {
			controller.sendToMages(message, center);	
		}
		
		// Kill power block
		if (castCommandBlock == null) {
			castCommandBlock = center.getBlock();
		}

        for (BlockFace powerFace : POWER_FACES) {
            Block checkForPower = castCommandBlock.getRelative(powerFace);
            if (commandReload) {
                controller.unregisterAutomata(checkForPower);
            }
            if (checkForPower.getType() == POWER_MATERIAL) {
                BlockData commitBlock = UndoList.register(checkForPower);
                commitBlock.setMaterial(Material.AIR);
                commitBlock.modify(checkForPower);
                commitBlock.commit();
            } else {
                BlockData commitBlock = UndoList.getBlockData(checkForPower.getLocation());
                if (commitBlock != null)
                {
                    commitBlock.setMaterial(Material.AIR);
                }
            }
        }

		// Drop item
		if (dropItem != null && dropItem.length() > 0) {
			Wand magicItem = controller.createWand(dropItem);
			if (magicItem != null) {
				center.getWorld().dropItemNaturally(center, magicItem.getItem());
			}
		}
		
		// Drop Xp
		if (dropXp > 0) {
			Entity entity = center.getWorld().spawnEntity(center, EntityType.EXPERIENCE_ORB);
			if (entity != null && entity instanceof ExperienceOrb) {
				ExperienceOrb orb = (ExperienceOrb)entity;
				orb.setExperience(dropXp);
			}
		}
		if (includeCommands && castCommandBlock != null) {
            BlockData commitBlock = UndoList.register(castCommandBlock);
            commitBlock.setMaterial(Material.AIR);
            commitBlock.modify(castCommandBlock);
            commitBlock.commit();
		}
		
		if (level != null) {
			level.onDeath(mage, birthMaterial);
		}
		if (!mage.isPlayer()) {
			controller.forgetMage(mage);
		}
	}
	
	protected void killBlock(Block block) {
		if (concurrent) {
			registerForUndo(block);
			block.setType(deathMaterial);
		} else {
			deadBlocks.add(block);
		}
	}
	
	protected void birthBlock(Block block) {
		if (concurrent) {
			registerForUndo(block);
			birthMaterial.modify(block);
		} else {
			bornBlocks.add(block);
		}
	}
	
	protected boolean simulateBlock(int dx, int dy, int dz) {
		int x = center.getBlockX() + dx;
		int y = center.getBlockY() + dy;
		int z = center.getBlockZ() + dz;
		Block block = world.getBlockAt(x, y, z);
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
			return false;
		}
		
		Material blockMaterial = block.getType();
		if (birthMaterial.is(block)) {
			int distanceSquared = liveRangeSquared > 0 || includeCommands ? 
					(int)Math.ceil(block.getLocation().distanceSquared(castCommandBlock.getLocation())) : 0;

			if (liveRangeSquared <= 0 || distanceSquared <= liveRangeSquared) {
				if (diagonalLiveCounts.size() > 0) {
					int faceNeighborCount = getFaceNeighborCount(block, birthMaterial, includeCommands);
					int diagonalNeighborCount = getDiagonalNeighborCount(block, birthMaterial, includeCommands);
					if (faceNeighborCount >= liveCounts.size() || !liveCounts.get(faceNeighborCount)
						|| diagonalNeighborCount >= diagonalLiveCounts.size() || !diagonalLiveCounts.get(diagonalNeighborCount)) {
						killBlock(block);
					} else {
						checkForPotentialCommand(block, distanceSquared);
					}
				} else {
					int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
					if (neighborCount >= liveCounts.size() || !liveCounts.get(neighborCount)) {
						killBlock(block);
					} else {
						checkForPotentialCommand(block, distanceSquared);
					}
				}
			} else {
				killBlock(block);
			}
		} else if (blockMaterial == deathMaterial) {
			int distanceSquared = birthRangeSquared > 0 || includeCommands ? 
					(int)Math.ceil(block.getLocation().distanceSquared(castCommandBlock.getLocation())) : 0;

			if (birthRangeSquared <= 0 || distanceSquared <= birthRangeSquared) {	
				if (diagonalBirthCounts.size() > 0) {
					int faceNeighborCount = getFaceNeighborCount(block, birthMaterial, includeCommands);
					int diagonalNeighborCount = getDiagonalNeighborCount(block, birthMaterial, includeCommands);
					if (faceNeighborCount < birthCounts.size() && birthCounts.get(faceNeighborCount)
						&& diagonalNeighborCount < diagonalBirthCounts.size() && diagonalBirthCounts.get(diagonalNeighborCount)) {
						birthBlock(block);
						checkForPotentialCommand(block, distanceSquared);
					}
				} else {
					int neighborCount = getNeighborCount(block, birthMaterial, includeCommands);
					if (neighborCount < birthCounts.size() && birthCounts.get(neighborCount)) {
						birthBlock(block);
						checkForPotentialCommand(block, distanceSquared);
					}
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
		
		return true;
	}
	
	protected boolean simulateBlocks(int x, int y, int z) {
		boolean success = true;
		if (y != 0) {
			success = success && simulateBlock(x, -y, z);
			if (x != 0) success = success && simulateBlock(-x, -y, z);
			if (z != 0) success = success && simulateBlock(x, -y, -z);
			if (x != 0 && z != 0) success = success && simulateBlock(-x, -y, -z);
		}
		success = success && simulateBlock(x, y, z);
		if (x != 0) success = success && simulateBlock(-x, y, z);
		if (z != 0) success = success && simulateBlock(x, y, -z);
		if (z != 0 && x != 0) success = success && simulateBlock(-x, y, -z);
		return success;
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
							controller.unregisterAutomata(checkForPower);
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
			if (!simulateBlocks(x, y, z)) {
				return processedBlocks;
			}
			
			y++;
			if (y > yRadius) {
				y = 0;
				if (x < radius) {
					x++;
				} else {
					z--;
					if (z < 0) {
						r++;
						z = r;
						x = 0;
					}
				}
			}
			
			if (r > radius) 
			{
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
				
				if (birthMaterial.is(killBlock)) {
					registerForUndo(killBlock);
					killBlock.setType(deathMaterial);
				} else {
					// If this block was destroyed while we were processing,
					// avoid spawning a random birth block.
					// This tries to make it so automata don't "cheat" when
					// getting destroyed. A bit hacky though, I'm not about
					// to re-simulate...
					if (bornBlocks.size() > 0) {
						bornBlocks.remove(bornBlocks.size() - 1);
					}
				}
				processedBlocks++;
			}
			
			int bornIndex = updatingIndex - deadBlocks.size();
			if (bornIndex >= 0 && bornIndex < bornBlocks.size()) {
				Block birthBlock = bornBlocks.get(bornIndex);
				if (!birthBlock.getChunk().isLoaded()) {
					birthBlock.getChunk().load();
					return processedBlocks;
				}
				registerForUndo(birthBlock);
				birthMaterial.modify(birthBlock);
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
                powerTargetBlock = null;
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

                // Search for a power block
                if (commandTargetBlock != null) {
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
                            if (blockSpell.isDestructible(commandTargetBlock.getRelative(face))) {
                                if (DEBUG) {
                                    controller.getLogger().info("Had to fall back to destructible location, pattern may diverge and may destroy blocks");
                                }
                                powerDirection = face;
                                break;
                            }
                        }
                    }

                    if (powerDirection != null) {
                        powerTargetBlock = commandTargetBlock.getRelative(powerDirection);
                    }
                }
			}
			if (DEBUG) {
				if (commandTargetBlock != null) {
					controller.getLogger().info("MOVED: " + commandTargetBlock.getLocation().toVector().subtract(center.toVector()));
				}
			}
			state = SimulationState.COMMON_RESET_REDSTONE;
			return processedBlocks;
		}

        if (state == SimulationState.COMMON_RESET_REDSTONE) {
            if (includeCommands && commandTargetBlock != null) {
                commandTargetBlock.setData((byte)0);
            }
            if (includeCommands && powerTargetBlock != null) {
                powerTargetBlock.setData((byte)0);
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
			powerDelayTicks = POWER_DELAY_TICKS;
			state = SimulationState.COMMAND_POWER;
			return processedBlocks;
		}
		
		if (state == SimulationState.COMMAND_POWER) {
			// Continue to power the command block
			if (commandPowered && powerTargetBlock != null && includeCommands) {
				// Wait a bit before powering for redstone signals to reset
				if (powerDelayTicks > 0) {
					powerDelayTicks--;
					return processedBlocks;
				}

				if (powerTargetBlock != null) {
                    powerTargetBlock.setType(POWER_MATERIAL);
					if (commandReload) {
						String automataName = commandName;
						if (automataName == null || automataName.length() <= 1) {
							automataName = controller.getMessages().get("automata.default_name");
						}
						controller.registerAutomata(powerTargetBlock, automataName, "automata.awaken");
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
	
	public void setDrop(String dropName, int dropXp) {
		this.dropItem = dropName;
		this.dropXp = dropXp;
	}
	
	public void setLevel(AutomatonLevel level) {
		this.level = level;
		this.commandMoveRangeSquared = level.getMoveRangeSquared(commandMoveRangeSquared);
		this.dropXp = level.getDropXp(dropXp);
		this.liveRangeSquared = level.getLiveRangeSquared(liveRangeSquared);
		this.birthRangeSquared = level.getBirthRangeSquared(birthRangeSquared);
		this.radius = level.getRadius(radius);
		this.yRadius = level.getYRadius(yRadius);
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
	
	public void setCastRange(int range) {
		castRange = range;
	}

	public void setMinHuntRange(int range) {
		huntMinRange = range;
	}
	
	public void setTargetType(TargetType targetType) {
		this.targetType = targetType;
	}
	
	@SuppressWarnings("deprecation")
	public void target(TargetMode mode) {
		targetMode = mode == null ? TargetMode.STABILIZE : mode;
		switch (targetMode) 
		{
		case FLEE:
		case HUNT:
		case DIRECTED:
			Target bestTarget = null;
			reverseTargetDistanceScore = true;
			if (targetType == TargetType.ANY || targetType == TargetType.MOB)
			{
				List<Entity> entities = CompatibilityUtils.getNearbyEntities(center, huntMaxRange, huntMaxRange, huntMaxRange);
				for (Entity entity : entities)
				{
					// We'll get the players from the Mages list
					if (entity instanceof Player || !(entity instanceof LivingEntity) || entity.isDead()) continue;
					if (!entity.getLocation().getWorld().equals(center.getWorld())) continue;
					LivingEntity li = (LivingEntity)entity;
					if (li.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
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
					if (targetType == TargetType.PLAYER && mage.getPlayer() == null) continue;
					if (mage.isDead() || !mage.isOnline() || !mage.hasLocation()) continue;
					if (!mage.getLocation().getWorld().equals(center.getWorld())) continue;
					if (!mage.getLocation().getWorld().equals(center.getWorld())) continue;
					
					if (!mage.isPlayer()) {
						// Check for automata of the same type, kinda hacky.. ?
						Block block = mage.getLocation().getBlock();
						if (block.getType() == Material.COMMAND) {
							BlockState blockState = block.getState();
							if (blockState != null && blockState instanceof CommandBlock) {
								CommandBlock command = (CommandBlock)blockState;
								String commandString = command.getCommand();
								if (commandString != null && commandString.length() > 0 && commandString.startsWith("cast " + spell.getKey())) {
									continue;
								}
							}
						}
					} else {
						Player player = mage.getPlayer();
						if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
					}
					
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
				 		" score: " + bestTarget.getScore() + " location: " + center + " -> " + bestTarget.getLocation() + " move " + commandMoveRangeSquared);
				}
				Vector direction = null;
				
				if (targetMode == TargetMode.DIRECTED) {
					direction = bestTarget.getLocation().getDirection();
					if (DEBUG) {
						controller.getLogger().info(" *Directed: " + direction);
					}
				} else {
					Location targetLocation = bestTarget.getLocation();
					direction = targetLocation.toVector().subtract(center.toVector());
				}
				
				if (direction != null) {
					center.setDirection(direction);
				}
				
				// Check for obstruction
				// TODO Think about this more..
				/*
				Block block = spell.getInteractBlock();
				if (block.getType() != Material.AIR && block.getType() != POWER_MATERIAL && !!birthMaterial.is(block)) {
					// TODO: Use location.setDirection in 1.7+
					center = CompatibilityUtils.setDirection(center, new Vector(0, 1, 0));
				}
				*/
				
				if (level != null && center.distanceSquared(bestTarget.getLocation()) < castRange * castRange) {
					level.onTick(mage, birthMaterial);
				}
				
				// After ticking, re-position for movement. This way spells still fire towards the target.
				if (targetMode == TargetMode.FLEE) {
					direction = direction.multiply(-1);
					// Don't Flee upward
					if (direction.getY() > 0) {
						direction.setY(-direction.getY());
					}
				}
			}
			break;
		case GLIDE:
			reverseTargetDistanceScore = true;
			break;
		default:
			reverseTargetDistanceScore = false;
		}
	}
	
	public void setCommandMoveRange(int commandRadius, boolean reload) {
		commandReload = reload;
		commandMoveRangeSquared = commandRadius * commandRadius;
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
        return getDiagonalNeighborCount(block, liveMaterial, includeCommands) + getFaceNeighborCount(block, liveMaterial, includeCommands);
	}

    protected int getFaceNeighborCount(Block block, MaterialAndData liveMaterial, boolean includeCommands) {
        int liveCount = 0;
        BlockFace[] faces = yRadius > 0 ? POWER_FACES : MAIN_FACES;
        for (BlockFace face : faces) {
            if (liveMaterial.is(block.getRelative(face))) {
                liveCount++;
            }
        }
        return liveCount;
    }

    protected int getDiagonalNeighborCount(Block block, MaterialAndData liveMaterial, boolean includeCommands) {
        int liveCount = 0;
        for (BlockFace face : DIAGONAL_FACES) {
            if (liveMaterial.is(block.getRelative(face))) {
                liveCount++;
            }
        }

        if (yRadius > 0) {
            Block upBlock = block.getRelative(BlockFace.UP);
            for (BlockFace face : NEIGHBOR_FACES) {
                if (liveMaterial.is(upBlock.getRelative(face))) {
                    liveCount++;
                }
            }

            Block downBlock = block.getRelative(BlockFace.DOWN);
            for (BlockFace face : NEIGHBOR_FACES) {
                if (liveMaterial.is(downBlock.getRelative(face))) {
                    liveCount++;
                }
            }
        }
        return liveCount;
    }

	public void setConcurrent(boolean concurrent) {
		this.concurrent = concurrent;
	}
	
	@Override
	public void finish() {
		state = SimulationState.FINISHED;
		super.finish();
	}
	
	protected void mapIntegers(Collection<Integer> flags, List<Boolean> flagMap) {
		for (Integer flag : flags) {
			while (flagMap.size() <= flag) {
				flagMap.add(false);
			}
			flagMap.set(flag, true);
		}
	}
	
	public void setDiagonalLiveRules(Collection<Integer> rules) {
		mapIntegers(rules, this.diagonalLiveCounts);
	}
	
	public void setDiagonalBirthRules(Collection<Integer> rules) {
		mapIntegers(rules, this.diagonalBirthCounts);
	}
}
