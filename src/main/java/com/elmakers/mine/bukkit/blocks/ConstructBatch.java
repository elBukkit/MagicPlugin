package com.elmakers.mine.bukkit.blocks;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public class ConstructBatch extends VolumeBatch {
	
	// TODO: Global max-y config value
	private final static int MAX_Y = 255;
	
	private final BlockList constructedBlocks = new BlockList();
	private final Location center;
	private Vector orient = null;
	private final int radius;
	private final ConstructionType type;
	private final boolean fill;
	private final Mage mage;
	private final BrushSpell spell;
	private final boolean spawnFallingBlocks;
	private Vector fallingBlockVelocity = null;
	private boolean copyEntities = true;
	private final BlockList attachedBlocks = new BlockList();
	private final BlockList delayedBlocks = new BlockList();
	private final Set<Material> attachables;
	private final Set<Material> attachablesWall;
	private final Set<Material> delayed;
	
	private boolean finishedNonAttached = false;
	private boolean finishedAttached = false;
	private int attachedBlockIndex = 0;
	private int delayedBlockIndex = 0;
	private Integer maxOrientDimension = null;
	private Integer minOrientDimension = null;
	
	private int x = 0;
	private int y = 0;
	private int z = 0;
	private int r = 0;
	
	public ConstructBatch(BrushSpell spell, Location center, ConstructionType type, int radius, boolean fill, boolean spawnFallingBlocks, Location orientToLocation) {
		super(spell.getMage().getController(), center.getWorld().getName());
		this.center = center;
		this.radius = radius;
		this.type = type;
		this.fill = fill;
		this.spawnFallingBlocks = spawnFallingBlocks;
		this.mage = spell.getMage();
		this.spell = spell;
		this.attachables = mage.getController().getMaterialSet("attachable");
		this.attachablesWall = mage.getController().getMaterialSet("attachable_wall");
		this.delayed = mage.getController().getMaterialSet("delayed");
		if (orientToLocation != null) {
			Vector orientTo = orientToLocation.toVector().subtract(center.toVector());
			orientTo.setX(Math.abs(orientTo.getX()));
			orientTo.setY(Math.abs(orientTo.getY()));
			orientTo.setZ(Math.abs(orientTo.getZ()));
			if (orientTo.getX() < orientTo.getZ() && orientTo.getX() < orientTo.getY()) {
				orient = new Vector(1, 0, 0);
			} else if (orientTo.getZ() < orientTo.getX() && orientTo.getZ() < orientTo.getY()) {
				orient = new Vector(0, 0, 1);
			} else {
				orient = new Vector(0, 1, 0);
			}
		} else {
			orient = new Vector(0, 1, 0);
		}
	}
	
	public void setFallingBlockVelocity(Vector velocity) {
		fallingBlockVelocity = velocity;
	}
	
	public void setOrientDimensionMax(int maxDim) {
		this.maxOrientDimension = maxDim;
	}
	
	public void setOrientDimensionMin(int minDim) {
		this.minOrientDimension = minDim;
	}
	
	protected boolean canAttachTo(Material material) {
		// Should I use my own list for this? This one seems good and efficient.
		if (material.isTransparent()) return false;
		
		// Can't attach to any attachables either- some of these (like signs) aren't transparent.
		return !attachables.contains(material);
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
				
				constructedBlocks.add(block);
				delayed.modify(block);
				
				delayedBlockIndex++;
			}
		} else if (finishedNonAttached) {
			while (attachedBlockIndex < attachedBlocks.size() && processedBlocks <  maxBlocks) {
				BlockData attach = attachedBlocks.get(attachedBlockIndex);
				Block block = attach.getBlock();
				if (!block.getChunk().isLoaded()) {
					block.getChunk().load();
					return processedBlocks;
				}
				
				// TODO: Port all this to fill... or move to BlockSpell?
				
				// Always check the the block underneath the target
				Block underneath = block.getRelative(BlockFace.DOWN);
				
				boolean ok = canAttachTo(underneath.getType());
				
				// TODO : More specific checks: crops, potato, carrot, melon/pumpkin, cactus, etc.
				
				if (!ok) {
					// Check for a wall attachable. These are assumed to also be ok
					// on the ground.
					Material material = attach.getMaterial();
					boolean canAttachToWall = attachablesWall.contains(material);
					if (canAttachToWall) {
						final BlockFace[] faces = {BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};
						for (BlockFace face : faces) {
							if (canAttachTo(block.getRelative(face).getType())) {
								ok = true;
								break;
							}
						}
					}
				}
				
				if (ok) {
					constructedBlocks.add(block);
					attach.modify(block);
				}
				
				attachedBlockIndex++;
			}
			if (attachedBlockIndex >= attachedBlocks.size()) {
				finishedAttached = true;
			}
		} else {
			int yBounds = radius;
			if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockY() > 0) {
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
	
	@Override
	public void finish() {
		if (!finished) {
			super.finish();
			
			MaterialBrush brush = spell.getMaterialBrush();
			if (copyEntities && fill && brush != null && brush.isReplicating()) {
				// TODO: Handle Non-spherical construction types!
				int radiusSquared = radius * radius;
				World targetWorld = center.getWorld();

				// First clear all hanging entities from the area.
				List<Entity> targetEntities = targetWorld.getEntities();
				for (Entity entity : targetEntities) {
					// Specific check only for what we copy. This could be more abstract.
					if (entity instanceof Painting || entity instanceof ItemFrame) {
						if (entity.getLocation().distanceSquared(center) <= radiusSquared) {
							entity.remove();
						}
					}
				}
				
				// Now copy all hanging entities from the source location
				Location cloneLocation = brush.toTargetLocation(center);
				World sourceWorld = cloneLocation.getWorld();
				List<Entity> entities = sourceWorld.getEntities();
				for (Entity entity : entities) {
					if (entity instanceof Painting) {
						if (entity.getLocation().distanceSquared(cloneLocation) > radiusSquared) continue;
						Painting painting = (Painting)entity;
						Location attachedLocation = painting.getLocation().getBlock().getRelative(painting.getAttachedFace()).getLocation();
						Location targetLocation = brush.fromTargetLocation(center.getWorld(), attachedLocation);
						try {
							Painting newPainting = (Painting)center.getWorld().spawnEntity(targetLocation, EntityType.PAINTING);
							if (newPainting != null) {
								targetLocation = brush.fromTargetLocation(center.getWorld(), painting.getLocation());								
								newPainting.teleport(targetLocation);
								newPainting.setArt(painting.getArt());
								newPainting.setFacingDirection(painting.getFacing());
							}
						} catch (Exception ex) {
							// controller.getLogger().warning(ex.getMessage());
							targetWorld.dropItemNaturally(targetLocation, new ItemStack(Material.PAINTING, 1));
						}
					} else if (entity instanceof ItemFrame) {
						if (entity.getLocation().distanceSquared(cloneLocation) > radiusSquared) continue;
						ItemFrame itemFrame = (ItemFrame)entity;
						Location attachedLocation = itemFrame.getLocation().getBlock().getRelative(itemFrame.getAttachedFace()).getLocation();
						Location targetLocation = brush.fromTargetLocation(center.getWorld(), attachedLocation);
						ItemStack itemStack = InventoryUtils.getCopy(itemFrame.getItem());
						try {
							ItemFrame newItemFrame = (ItemFrame)center.getWorld().spawnEntity(targetLocation, EntityType.ITEM_FRAME);
							if (newItemFrame != null) {
								targetLocation = brush.fromTargetLocation(center.getWorld(), itemFrame.getLocation());
								newItemFrame.teleport(targetLocation);
								newItemFrame.setFacingDirection(itemFrame.getFacing());
								newItemFrame.setRotation(itemFrame.getRotation());
								if (itemStack != null) {
									newItemFrame.setItem(itemStack);
								}
							}
						} catch (Exception ex) {
							// controller.getLogger().warning(ex.getMessage());
							if (itemStack != null) {
								targetWorld.dropItemNaturally(targetLocation, itemStack);
							}
							targetWorld.dropItemNaturally(targetLocation, new ItemStack(Material.ITEM_FRAME, 1));
						}
					}
				}
			}
			
			spell.registerForUndo(constructedBlocks);
			mage.castMessage("Constructed " + constructedBlocks.size() + " blocks");
		}
	}
	
	public void  setCopyEntities(boolean doCopy) {
		copyEntities = doCopy;
	}

	public boolean fillBlock(int x, int y, int z)
	{
		boolean fillBlock = false;
		switch(type) {
			case SPHERE:
				int maxDistanceSquared = radius * radius;
				float mx = (float)x - 0.5f;
				float my = (float)y - 0.5f;
				float mz = (float)z - 0.5f;
				
				int distanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
				if (fill)
				{
					fillBlock = distanceSquared <= maxDistanceSquared;
				} 
				else 
				{
					mx++;
					my++;
					mz++;
					int outerDistanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
					fillBlock = maxDistanceSquared >= distanceSquared && maxDistanceSquared <= outerDistanceSquared;
				}	
				//spells.getLog().info("(" + x + "," + y + "," + z + ") : " + fillBlock + " = " + distanceSquared + " : " + maxDistanceSquared);
				break;
			case PYRAMID:
				int elevation = radius - y;
				if (fill) {
					fillBlock = (x <= elevation) && (z <= elevation);
				} else {
					fillBlock = (x == elevation && z <= elevation) || (z == elevation && x <= elevation);
				}
				break;
			default: 
				fillBlock = fill ? true : (x == radius || y == radius || z == radius);
				break;
		}
		boolean success = true;
		if (fillBlock)
		{
			success = success && constructBlock(x, y, z);
			success = success && constructBlock(-x, y, z);
			success = success && constructBlock(x, -y, z);
			success = success && constructBlock(x, y, -z);
			success = success && constructBlock(-x, -y, z);
			success = success && constructBlock(x, -y, -z);
			success = success && constructBlock(-x, y, -z);
			success = success && constructBlock(-x, -y, -z);
		}
		return success;
	}

	public int getDistanceSquared(int x, int y, int z)
	{
		return x * x + y * y + z * z;
	}

	@SuppressWarnings("deprecation")
	public boolean constructBlock(int dx, int dy, int dz)
	{
		// Initial range checks, we skip everything if this is not sane.
		int x = center.getBlockX() + dx;
		int y = center.getBlockY() + dy;
		int z = center.getBlockZ() + dz;
		
		if (y < 0 || y > MAX_Y) return true;
		
		// Prepare material brush, it may update
		// given the current target (clone, replicate)
		Block block = center.getWorld().getBlockAt(x, y, z);
		MaterialBrush brush = spell.getMaterialBrush();
		brush.update(mage, block.getLocation());
		
		// Make sure the brush is ready, it may need to load chunks.
		if (!brush.isReady()) {
			brush.prepare();
			return false;
		}
		
		// Postpone attachable blocks to a second batch
		if (attachables.contains(brush.getMaterial())) {
			BlockData attachBlock = new BlockData(block);
			attachBlock.updateFrom(brush);
			attachedBlocks.add(attachBlock);
			return true;
		}
		
		if (delayed.contains(brush.getMaterial())) {
			BlockData delayBlock = new BlockData(block);
			delayBlock.updateFrom(brush);
			delayedBlocks.add(delayBlock);
			return true;
		}
		
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
			return false;
		}
		
		if (!spell.isDestructible(block))
		{
			return true;
		}
		if (!spell.hasBuildPermission(block)) 
		{
			return true;
		}
		
		Material previousMaterial = block.getType();
		byte previousData = block.getData();
		
		if (brush.isDifferent(block)) {			
			updateBlock(center.getWorld().getName(), x, y, z);
			constructedBlocks.add(block);
			brush.modify(block);
			if (spawnFallingBlocks) {
				FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
				falling.setDropItem(false);
				if (fallingBlockVelocity != null) {
					falling.setVelocity(fallingBlockVelocity);
				}
			}
		}
		return true;
	}
	
	public void setTimeToLive(int timeToLive) {
		this.constructedBlocks.setTimeToLive(timeToLive);
	}
}
