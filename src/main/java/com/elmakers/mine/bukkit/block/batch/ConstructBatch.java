package com.elmakers.mine.bukkit.block.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PoweredRail;
import org.bukkit.material.RedstoneWire;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.ConstructionType;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public class ConstructBatch extends BrushBatch {
	private final Location center;
	private Vector orient = null;
	private final int radius;
	private final ConstructionType type;
	private final int thickness;
	private final boolean spawnFallingBlocks;
	private float fallingBlockSpeed = 0;
	private final Map<Long, BlockData> attachedBlockMap = new HashMap<Long, BlockData>();
	private final List<BlockData> attachedBlockList = new ArrayList<BlockData>();
	private final List<BlockData> delayedBlocks = new ArrayList<BlockData>();
	private final Set<Material> attachables;
	private final Set<Material> attachablesWall;
	private final Set<Material> attachablesDouble;
	private final Set<Material> delayed;
	private Set<Material> replace;
	private Map<String, String> commandMap;
    private Map<String, String> signMap;
	
	private boolean finishedNonAttached = false;
	private boolean finishedAttached = false;
	private int attachedBlockIndex = 0;
	private int delayedBlockIndex = 0;
	private Integer maxOrientDimension = null;
	private Integer minOrientDimension = null;
	private boolean power = false;
    private int breakable = 0;
	
	private int x = 0;
	private int y = 0;
	private int z = 0;
	private int r = 0;

	private boolean limitYAxis = false;
	// TODO.. min X, Z, etc
	
	public ConstructBatch(BrushSpell spell, Location center, ConstructionType type, int radius, int thickness, boolean spawnFallingBlocks, Vector orientVector) {
		super(spell);
		this.center = center;
		this.radius = radius;
		this.type = type;
		this.thickness = thickness;
		this.spawnFallingBlocks = spawnFallingBlocks;
		this.attachables = mage.getController().getMaterialSet("attachable");
		this.attachablesWall = mage.getController().getMaterialSet("attachable_wall");
		this.attachablesDouble = mage.getController().getMaterialSet("attachable_double");
		this.delayed = mage.getController().getMaterialSet("delayed");
        this.orient = orientVector == null ? new Vector(0, 1, 0) : orientVector;
	}
	
	public void setPower(boolean power) {
		this.power = power;
	}

    public void setBreakable(int breakable) {
        this.breakable = breakable;
    }
	
	public void setFallingBlockSpeed(float speed) {
		fallingBlockSpeed = speed;
	}
	
	public void setOrientDimensionMax(int maxDim) {
		this.maxOrientDimension = maxDim;
	}
	
	public void setOrientDimensionMin(int minDim) {
		this.minOrientDimension = minDim;
	}
	
	protected boolean canAttachTo(Material attachMaterial, Material material, boolean vertical) {
		// For double-high blocks, a material can always attach to itself.
		if (vertical && attachMaterial == material) return true;

			// Should I use my own list for this? This one seems good and efficient.
		if (material.isTransparent()) return false;
		
		// Can't attach to any attachables either- some of these (like signs) aren't transparent.
		return !attachables.contains(material) && !attachablesWall.contains(material) && !attachablesDouble.contains(material);
	}
	
	public int size() {
		return radius * radius * radius * 8;
	}
	
	public int remaining() {
		if (r >= radius) return 0;
		return (radius - r) * (radius - r) * (radius - r) * 8;
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		if (finishedAttached) {
			if (delayedBlockIndex >= delayedBlocks.size()) {
				finish();
			} else while (delayedBlockIndex < delayedBlocks.size() && processedBlocks <  maxBlocks) {
				BlockData delayed = delayedBlocks.get(delayedBlockIndex);
				Block block = delayed.getBlock();
				if (!block.getChunk().isLoaded()) {
					block.getChunk().load();
					return processedBlocks;
				}

                modifyWith(block, delayed);
				
				delayedBlockIndex++;
			}
		} else if (finishedNonAttached) {
			while (attachedBlockIndex < attachedBlockList.size() && processedBlocks <  maxBlocks) {
				BlockData attach = attachedBlockList.get(attachedBlockIndex);
				Block block = attach.getBlock();
				if (!block.getChunk().isLoaded()) {
					block.getChunk().load();
					return processedBlocks;
				}

				// TODO: Port all this to fill... or move to BlockSpell?
				
				// Always check the the block underneath the target
				Block underneath = block.getRelative(BlockFace.DOWN);

				Material material = attach.getMaterial();
				boolean ok = canAttachTo(material, underneath.getType(), true);

				if (!ok && attachablesDouble.contains(material)) {
					BlockData attachedUnder = attachedBlockMap.get(BlockData.getBlockId(underneath));
					ok = (attachedUnder != null && attachedUnder.getMaterial() == material);
					
					if (!ok) {
						Block above = block.getRelative(BlockFace.UP);
						BlockData attachedAbove = attachedBlockMap.get(BlockData.getBlockId(above));
						ok = (attachedAbove != null && attachedAbove.getMaterial() == material);
					}
				}
				
				// TODO : More specific checks: crops, potato, carrot, melon/pumpkin, cactus, etc.
				
				if (!ok) {
					// Check for a wall attachable. These are assumed to also be ok
					// on the ground.
					boolean canAttachToWall = attachablesWall.contains(material);
					if (canAttachToWall) {
						final BlockFace[] faces = {BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};
						for (BlockFace face : faces) {
							if (canAttachTo(material, block.getRelative(face).getType(), false)) {
								ok = true;
								break;
							}
						}
					}
				}
				
				if (ok) {
                    modifyWith(block, attach);
				}
				
				attachedBlockIndex++;
			}
			if (attachedBlockIndex >= attachedBlockList.size()) {
				finishedAttached = true;
			}
		} else {
			int yBounds = radius;
			if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockY() > 0) {
				limitYAxis = true;
				yBounds = Math.max(minOrientDimension == null ? radius : minOrientDimension, maxOrientDimension == null ? radius : maxOrientDimension);
			}
			yBounds = Math.min(yBounds, 255);
			
			while (processedBlocks <= maxBlocks && !finishedNonAttached) {
				if (!fillBlock(x, y, z)) {
					return processedBlocks;
				}
				
				int xBounds = r;
				int zBounds = r;
				if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockX() > 0) {
					xBounds = Math.max(minOrientDimension == null ? r : minOrientDimension, maxOrientDimension == null ? r : maxOrientDimension);
				}
				
				if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockZ() > 0) {
					zBounds = Math.max(minOrientDimension == null ? r : minOrientDimension, maxOrientDimension == null ? r : maxOrientDimension);
				}
				
				y++;
				if (y > yBounds) {
					y = 0;
					if (x < xBounds) {
						x++;
					} else {
						z--;
						if (z < 0) {
							r++;
							zBounds = r;
							if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockZ() > 0) {
								zBounds = Math.max(minOrientDimension == null ? r : minOrientDimension, maxOrientDimension == null ? r : maxOrientDimension);
							}
							z = zBounds;
							x = 0;
						}
					}
				}
				
				if (r > radius) 
				{
					finishedNonAttached = true;
					break;
				}
				processedBlocks++;
			}
		}
		
		return processedBlocks;
	}

	public boolean fillBlock(int x, int y, int z)
	{
		boolean fillBlock = false;
		switch(type) {
			case SPHERE:
				int maxDistanceSquared = radius * radius;
				float mx = (float)x - 0.1f;
				float my = (float)y - 0.1f;
				float mz = (float)z - 0.1f;
				
				int distanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
				if (thickness == 0)
				{
					fillBlock = distanceSquared <= maxDistanceSquared;
				} 
				else 
				{
					mx++;
					my++;
					mz++;
					int outerDistanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
					fillBlock = maxDistanceSquared >= distanceSquared - thickness && maxDistanceSquared <= outerDistanceSquared;
				}	
				break;
			case PYRAMID:
				int elevation = radius - y;
				if (thickness == 0) {
					fillBlock = (x <= elevation) && (z <= elevation);
				} else {
					fillBlock = (x <= elevation && x >= elevation - thickness && z <= elevation) 
							 || (z <= elevation && z >= elevation - thickness && x <= elevation);
				}
				break;
			default: 
				fillBlock = thickness == 0 ? true : (x >= radius - thickness || y >= radius - thickness || z >= radius - thickness);
				break;
		}
		boolean success = true;
		if (fillBlock)
		{
			if (y != 0) {
				success = success && constructBlock(x, -y, z);
				if (x != 0) success = success && constructBlock(-x, -y, z);
				if (z != 0) success = success && constructBlock(x, -y, -z);
				if (x != 0 && z != 0) success = success && constructBlock(-x, -y, -z);
			}
			success = success && constructBlock(x, y, z);
			if (x != 0) success = success && constructBlock(-x, y, z);
			if (z != 0) success = success && constructBlock(x, y, -z);
			if (z != 0 && x != 0) success = success && constructBlock(-x, y, -z);
		}
		return success;
	}

	@SuppressWarnings("deprecation")
	public boolean constructBlock(int dx, int dy, int dz)
	{
		// Special-case hackiness..
		if (limitYAxis && minOrientDimension != null && dy < -minOrientDimension) return true;
		if (limitYAxis && maxOrientDimension != null && dy > maxOrientDimension) return true;
		
		// Initial range checks, we skip everything if this is not sane.
		int x = center.getBlockX() + dx;
		int y = center.getBlockY() + dy;
		int z = center.getBlockZ() + dz;
		
		if (y < 0 || y > controller.getMaxY()) return true;
		
		// Make sure the block is loaded.
		Block block = center.getWorld().getBlockAt(x, y, z);
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
			return false;
		}

        // Destructibility and permission checks
        if (!spell.isDestructible(block))
        {
            return true;
        }

        if (replace != null && replace.size() > 0 && !replace.contains(block.getType()))
        {
            return true;
        }

        if (!spell.hasBuildPermission(block))
        {
            return true;
        }
		
		// Check for power mode.
		if (power)
		{
			Material material = block.getType();
			BlockState blockState = block.getState();
			MaterialData data = blockState.getData();
			boolean powerBlock = false;
			if (data instanceof Button) {
				Button powerData = (Button)data;
				registerForUndo(block);
				powerData.setPowered(!powerData.isPowered());
				powerBlock = true;
			} else if (data instanceof Lever) {
				Lever powerData = (Lever)data;
				registerForUndo(block);
				powerData.setPowered(!powerData.isPowered());
				powerBlock = true;
			} else if (data instanceof PistonBaseMaterial) {
				PistonBaseMaterial powerData = (PistonBaseMaterial)data;
				registerForUndo(block);
				powerData.setPowered(!powerData.isPowered());
				powerBlock = true;
			} else if (data instanceof PoweredRail) {
				PoweredRail powerData = (PoweredRail)data;
				registerForUndo(block);
				powerData.setPowered(!powerData.isPowered());
				powerBlock = true;
			} else if (data instanceof RedstoneWire) {
				RedstoneWire wireData = (RedstoneWire)data;
				registerForUndo(block);
				wireData.setData((byte)(15 - wireData.getData()));
				powerBlock = true;
			} else if (material == Material.REDSTONE_BLOCK) {
				
				// A work-around for double-powering Automata.
				// It'd be really cool to maybe find the associated command
				// block and temporarily disable it, or something.
				if (!controller.isAutomata(block)) {
					registerForUndo(block);
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, material.getId());
					block.setType(Material.AIR);
				}
			} else if (material == Material.REDSTONE_TORCH_OFF) {
				registerForUndo(block);
				block.setType(Material.REDSTONE_TORCH_ON);
			} else if (material == Material.REDSTONE_TORCH_ON) {
				registerForUndo(block);
				block.setType(Material.REDSTONE_TORCH_OFF);
			} else if (material == Material.TNT) {
				registerForUndo(block);
				block.setType(Material.AIR);
				
				// Kaboomy time!
				registerForUndo(block.getLocation().getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT));
			}
			
			if (powerBlock) {
				blockState.update();
			}
			
			return true;
		}

		// Prepare material brush, it may update
		// given the current target (clone, replicate)
		MaterialBrush brush = spell.getBrush();
		brush.update(mage, block.getLocation());
		
		// Make sure the brush is ready, it may need to load chunks.
		if (!brush.isReady()) {
			brush.prepare();
			return false;
		}
		
		// Postpone attachable blocks to a second batch
		if (attachables.contains(brush.getMaterial()) || attachablesWall.contains(brush.getMaterial()) || attachablesDouble.contains(brush.getMaterial())) {
			BlockData attachBlock = new BlockData(block);
			attachBlock.updateFrom(brush);
			attachedBlockMap.put(attachBlock.getId(), attachBlock);
			attachedBlockList.add(attachBlock);
			return true;
		}
		
		if (delayed.contains(brush.getMaterial())) {
			BlockData delayBlock = new BlockData(block);
			delayBlock.updateFrom(brush);
			delayedBlocks.add(delayBlock);
			return true;
		}

        modifyWith(block, brush);
		return true;
	}

    protected void modifyWith(Block block, MaterialAndData brush) {
        Material previousMaterial = block.getType();
        byte previousData = block.getData();

        if (brush.isDifferent(block)) {
            registerForUndo(block);

            // Check for command overrides
            if (commandMap != null && brush.getMaterial() == Material.COMMAND) {
                String commandKey = brush.getCommandLine();
                if (commandKey != null && commandKey.length() > 0 && commandMap.containsKey(commandKey)) {
                    brush.setCommandLine(commandMap.get(commandKey));
                }
            }

            // Check for sign overrides
            if (signMap != null && (brush.getMaterial() == Material.SIGN_POST || brush.getMaterial() == Material.WALL_SIGN)) {
                String[] lines = brush.getSignLines();
                if (lines != null && lines.length > 0 && !signMap.isEmpty())
                {
                    for (int i = 0; i < lines.length; i++)
                    {
                        String line = lines[i];
                        if (line != null && line.length() > 0 && signMap.containsKey(line)) {
                            lines[i] = signMap.get(line);
                        }
                    }

                    brush.setSignLines(lines);
                }
            }

            brush.modify(block);
            if (breakable > 0) {
                block.setMetadata("breakable", new FixedMetadataValue(controller.getPlugin(), breakable));
            }
            if (spawnFallingBlocks) {
                FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
                falling.setDropItem(false);
                if (fallingBlockSpeed != 0) {
                    Vector direction = falling.getLocation().subtract(center).toVector().normalize().multiply(fallingBlockSpeed);
                    falling.setVelocity(direction);
                }
                registerForUndo(falling);
            }
        }
    }
	
	public void addCommandMapping(String key, String command) {
		if (commandMap == null) {
			commandMap = new HashMap<String, String>();
		}
		
		commandMap.put(key,  command);
	}

    public void addSignMapping(String key, String text) {
        if (signMap == null) {
            signMap = new HashMap<String, String>();
        }

        signMap.put(key,  text);
    }

    public void setReplace(Collection<Material> replace) {
		this.replace = new HashSet<Material>();
		this.replace.addAll(replace);
	}
	
	@Override
	protected boolean contains(Location location) {
		if (thickness != 0) return false;
		if (!location.getWorld().equals(center.getWorld())) return false;
		
		// TODO: Handle PYRAMID better, thickness, max dimensions, etc.
		switch (type) {
		case SPHERE:
			int radiusSquared = radius * radius;
				return (location.distanceSquared(center) <= radiusSquared);
		default:
			return location.getBlockX() >= center.getBlockX() - radius
				&& location.getBlockX() <= center.getBlockX() + radius
				&& location.getBlockY() >= center.getBlockY() - radius
				&& location.getBlockY() <= center.getBlockY() + radius
				&& location.getBlockZ() >= center.getBlockZ() - radius
				&& location.getBlockZ() <= center.getBlockZ() + radius;
		}
	}
}
