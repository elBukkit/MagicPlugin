package com.elmakers.mine.bukkit.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PoweredRail;
import org.bukkit.material.RedstoneWire;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.ConstructionType;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

public class ConstructBatch extends BrushBatch {
	private final Location center;
	private Vector orient = null;
	private final int radius;
	private final ConstructionType type;
	private final int thickness;
	private final boolean spawnFallingBlocks;
	private float fallingBlockSpeed = 0;
    private Vector fallingDirection = null;
	private final Map<Long, BlockData> attachedBlockMap = new HashMap<>();
	private final List<BlockData> attachedBlockList = new ArrayList<>();
	private final List<BlockData> delayedBlocks = new ArrayList<>();
	private final @Nonnull MaterialSet attachables;
	private final @Nonnull MaterialSet attachablesWall;
	private final @Nonnull MaterialSet attachablesDouble;
	private final @Nonnull MaterialSet delayed;
	private Set<String> replace;
	private Map<String, String> commandMap;

	private boolean finishedNonAttached = false;
	private boolean finishedAttached = false;
	private int attachedBlockIndex = 0;
	private int delayedBlockIndex = 0;
	private Integer maxOrientDimension = null;
	private Integer minOrientDimension = null;
	private boolean power = false;
    private boolean commit = false;
    private double breakable = 0;
    private double backfireChance = 0;
    private Vector bounds = null;
	private boolean applyPhysics = false;
	private boolean consume = false;
	private boolean consumeVariants = true;

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

		MaterialSetManager materials = mage.getController().getMaterialSetManager();
		this.attachables = materials.getMaterialSetEmpty("attachable");
		this.attachablesWall = materials.getMaterialSetEmpty("attachable_wall");
		this.attachablesDouble = materials.getMaterialSetEmpty("attachable_double");
		this.delayed = materials.getMaterialSetEmpty("delayed");
        this.orient = orientVector == null ? new Vector(0, 1, 0) : orientVector;
	}

    public void setBounds(Vector bounds) {
        this.bounds = bounds;
    }

	public void setPower(boolean power) {
		this.power = power;
        this.undoList.setApplyPhysics(true);
	}

    public void setBackfireChance(double backfireChance) {
        this.backfireChance = backfireChance;
    }

    public void setBreakable(double breakable) {
        this.breakable = breakable;
    }

	public void setFallingBlockSpeed(float speed) {
		fallingBlockSpeed = speed;
	}

    public void setFallingDirection(Vector direction) {
        fallingDirection = direction;
    }

	public void setOrientDimensionMax(int maxDim) {
		this.maxOrientDimension = maxDim;
	}

	public void setOrientDimensionMin(int minDim) {
		this.minOrientDimension = minDim;
	}

	@Deprecated // Material
	protected boolean canAttachTo(Material attachMaterial, Material material, boolean vertical) {
		// For double-high blocks, a material can always attach to itself.
		if (vertical && attachMaterial == material) return true;

			// Should I use my own list for this? This one seems good and efficient.
		if (material.isTransparent()) return false;

		// Can't attach to any attachables either- some of these (like signs) aren't transparent.
		return !attachables.testMaterial(material) && !attachablesWall.testMaterial(material) && !attachablesDouble.testMaterial(material);
	}

	@Override
    public int size() {
		return radius * radius * radius * 8;
	}

	@Override
    public int remaining() {
		if (r >= radius) return 0;
		return (radius - r) * (radius - r) * (radius - r) * 8;
	}

	@Override
    public int process(int maxBlocks) {
		int processedBlocks = 0;
		if (finishedAttached) {
			if (delayedBlockIndex >= delayedBlocks.size()) {
				finish();
			} else while (delayedBlockIndex < delayedBlocks.size() && processedBlocks <  maxBlocks && !finished) {
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
			while (attachedBlockIndex < attachedBlockList.size() && processedBlocks <  maxBlocks && !finished) {
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

				if (!ok && attachablesDouble.testMaterialAndData(attach)) {
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
					boolean canAttachToWall = attachablesWall.testMaterialAndData(attach);
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
            if (bounds != null) {
                yBounds = Math.min(yBounds, (int)bounds.getY());
            }
			yBounds = Math.min(yBounds, center.getWorld().getMaxHeight());

			while (processedBlocks <= maxBlocks && !finishedNonAttached && !finished) {
				if (!fillBlock(x, y, z)) {
					return processedBlocks;
				}

				int xBounds = r;
				int zBounds = r;
				if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockX() > 0) {
					xBounds = Math.max(minOrientDimension == null ? r : minOrientDimension, maxOrientDimension == null ? r : maxOrientDimension);
				}
                if (bounds != null) {
                    xBounds = Math.min(xBounds, (int) bounds.getX());
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
                            if (bounds != null) {
                                zBounds = Math.min(zBounds, (int)bounds.getZ());
                            }
							z = zBounds;
							x = 0;
						}
					}
				}

				if (r > radius || (bounds != null && r > bounds.getZ() && r > bounds.getX()))
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
        int maxDistanceSquared = radius * radius;
        int distanceSquared;
        int outerDistanceSquared;
        float mx;
        float my;
        float mz;
        switch(type) {
			case SPHERE:
				mx = x - 0.1f;
				my = y - 0.1f;
				mz = z - 0.1f;

				distanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
				if (thickness == 0)
				{
					fillBlock = distanceSquared <= maxDistanceSquared;
				}
				else
				{
					mx++;
					my++;
					mz++;
					outerDistanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
					fillBlock = maxDistanceSquared >= distanceSquared - thickness && maxDistanceSquared <= outerDistanceSquared;
				}
				break;
            case CYLINDER:
                mx = x - 0.1f;
                mz = z - 0.1f;

                distanceSquared = (int)((mx * mx) + (mz * mz));
                if (thickness == 0)
                {
                    fillBlock = distanceSquared <= maxDistanceSquared;
                }
                else
                {
                    mx++;
                    mz++;
                    outerDistanceSquared = (int)((mx * mx) + (mz * mz));
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
				fillBlock = thickness == 0 ? true : (x > radius - thickness || y > radius - thickness || z > radius - thickness);
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
        if (bounds != null) {
            if (dx > bounds.getX() || dy > bounds.getY() || dz > bounds.getZ()) return true;
            if (dx < -bounds.getX() || dy < -bounds.getY() || dz < -bounds.getZ()) return true;
        }

		// Initial range checks, we skip everything if this is not sane.
		int x = center.getBlockX() + dx;
		int y = center.getBlockY() + dy;
		int z = center.getBlockZ() + dz;

		if (y < 0 || y > center.getWorld().getMaxHeight()) return true;

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

        if (replace != null && replace.size() > 0)
        {
            com.elmakers.mine.bukkit.block.MaterialAndData check = new com.elmakers.mine.bukkit.block.MaterialAndData(block);
            if (!replace.contains(check.getKey()) && !replace.contains(check.getWildDataKey())) {
                return true;
            }
        }

        // Prepare material brush, it may update
        // given the current target (clone, replicate)
        MaterialBrush brush = spell.getBrush();
        brush.update(mage, block.getLocation());

        if (brush.isErase()) {
            if (!spell.hasBreakPermission(block)) {
                return true;
            }
        } else {
            if (!spell.hasBuildPermission(block)) {
                return true;
            }
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
				registerForUndo(block);
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, material.getId());
				controller.getRedstoneReplacement().modify(block, applyPhysics);
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

		// Make sure the brush is ready, it may need to load chunks.
		if (!brush.isReady()) {
			brush.prepare();
			return false;
		}

		// Postpone attachable blocks to a second batch
		if (attachables.testMaterialAndData(brush) || attachablesWall.testMaterialAndData(brush) || attachablesDouble.testMaterialAndData(brush)) {
			BlockData attachBlock = new BlockData(block);
			attachBlock.updateFrom(brush);
			attachedBlockMap.put(attachBlock.getId(), attachBlock);
			attachedBlockList.add(attachBlock);
			return true;
		}

		if (delayed.testMaterialAndData(brush)) {
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
        byte previousData = DeprecatedUtils.getData(block);

        if (brush.isValid() && (brush.isDifferent(block) || commit)) {
			if (consume && !context.isConsumeFree() && brush.getMaterial() != Material.AIR) {
				ItemStack requires = brush.getItemStack(1);
				if (!mage.hasItem(requires, consumeVariants)) {
					String requiresMessage = context.getMessage("insufficient_resources");
					context.sendMessage(requiresMessage.replace("$cost", brush.getName()));
					finish();
					return;
				}
				mage.removeItem(requires, consumeVariants);
			}
			
            if (!commit) {
                registerForUndo(block);
            }

            // Check for command overrides
            if (commandMap != null && brush.getMaterial() == Material.COMMAND) {
                String commandKey = brush.getCommandLine();
                if (commandKey != null && commandKey.length() > 0 && commandMap.containsKey(commandKey)) {
                    brush.setCommandLine(commandMap.get(commandKey));
                }
            }

            brush.modify(block, applyPhysics);
            if (breakable > 0) {
                spell.getCurrentCast().registerBreakable(block, breakable);
            }
            if (backfireChance > 0) {
                spell.getCurrentCast().registerReflective(block, backfireChance);
            }
            if (spawnFallingBlocks) {
                FallingBlock falling = DeprecatedUtils.spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
                falling.setDropItem(false);
                if (fallingBlockSpeed != 0) {
                    Vector direction = this.fallingDirection != null ? this.fallingDirection :
                            falling.getLocation().subtract(center).toVector();
                    direction = direction.normalize().multiply(fallingBlockSpeed);
					SafetyUtils.setVelocity(falling, direction);
                }
                registerForUndo(falling);
            }
            if (commit) {
                com.elmakers.mine.bukkit.api.block.BlockData blockData = UndoList.register(block);
                blockData.commit();
            }
        }
    }

	public void addCommandMapping(String key, String command) {
		if (commandMap == null) {
			commandMap = new HashMap<>();
		}

		commandMap.put(key,  command);
	}

    public void setReplace(Collection<MaterialAndData> replace) {
		this.replace = new HashSet<>();
        for (MaterialAndData material : replace) {
            this.replace.add(material.getKey());
        }
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

	public void setApplyPhysics(boolean physics) {
		this.applyPhysics = physics;
	}

    public void setCommit(boolean commit) {
        this.commit = commit;
    }
	public void setConsume(boolean consume) {
		this.consume = consume;
	}
	public void setConsumeVariants(boolean variants) {
		this.consumeVariants = variants;
	}
}
