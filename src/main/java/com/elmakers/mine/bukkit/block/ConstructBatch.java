package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private final int thickness;
	private final Mage mage;
	private final BrushSpell spell;
	private final boolean spawnFallingBlocks;
	private Vector fallingBlockVelocity = null;
	private boolean copyEntities = true;
	private final Map<Long, BlockData> attachedBlockMap = new HashMap<Long, BlockData>();
	private final List<BlockData> attachedBlockList = new ArrayList<BlockData>();
	private final List<BlockData> delayedBlocks = new ArrayList<BlockData>();
	private final Set<Material> attachables;
	private final Set<Material> attachablesWall;
	private final Set<Material> attachablesDouble;
	private final Set<Material> delayed;
	private Map<String, String> commandMap;
	
	private boolean finishedNonAttached = false;
	private boolean finishedAttached = false;
	private int attachedBlockIndex = 0;
	private int delayedBlockIndex = 0;
	private Integer maxOrientDimension = null;
	private Integer minOrientDimension = null;
	private boolean power = false;
	
	private int x = 0;
	private int y = 0;
	private int z = 0;
	private int r = 0;

	private boolean limitYAxis = false;
	// TODO.. min X, Z, etc
	
	public ConstructBatch(BrushSpell spell, Location center, ConstructionType type, int radius, int thickness, boolean spawnFallingBlocks, Location orientToLocation) {
		super(spell.getMage().getController(), center.getWorld().getName());
		this.center = center;
		this.radius = radius;
		this.type = type;
		this.thickness = thickness;
		this.spawnFallingBlocks = spawnFallingBlocks;
		this.mage = spell.getMage();
		this.spell = spell;
		this.attachables = mage.getController().getMaterialSet("attachable");
		this.attachablesWall = mage.getController().getMaterialSet("attachable_wall");
		this.attachablesDouble = mage.getController().getMaterialSet("attachable_double");
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
	
	public void setPower(boolean power) {
		this.power = power;
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
	
	protected boolean canAttachTo(Material attachMaterial, Material material, boolean vertical) {
		// For double-high blocks, a material can always attach to itself.
		if (vertical && attachMaterial == material) return true;

			// Should I use my own list for this? This one seems good and efficient.
		if (material.isTransparent()) return false;
		
		// Can't attach to any attachables either- some of these (like signs) aren't transparent.
		return !attachables.contains(material) && !attachablesWall.contains(material) && !attachablesDouble.contains(material);
	}
	
	public int size() {
		return constructedBlocks.size();
	}
	
	public int remaining() {
		return delayedBlocks.size() + attachedBlockList.size() + ((r - x) * (r - y) * (r - z)) * 4;
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
					constructedBlocks.add(block);
					attach.modify(block);
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
	
	@Override
	public void finish() {
		if (!finished) {
			super.finish();
			
			MaterialBrush brush = spell.getMaterialBrush();
			if (copyEntities && thickness == 0 && brush != null && brush.hasEntities()) {
				// TODO: Handle Non-spherical construction types!
				List<EntityData> entities = brush.getEntities(center, radius);
				
				for (EntityData entity : entities) {
					Location targetLocation = entity.getLocation();
					
					switch (entity.getType()) {
					case PAINTING:
						InventoryUtils.spawnPainting(targetLocation, entity.getFacing(), entity.getArt());
					break;
					case ITEM_FRAME:
						InventoryUtils.spawnItemFrame(targetLocation, entity.getFacing(), entity.getItem());
						break;
					default: break;
					}
				}
			}
			
			spell.registerForUndo(constructedBlocks);
			
			String message = spell.getMessage("cast_finish");
			message = message.replace("$count", Integer.toString(constructedBlocks.size()));
			mage.castMessage(message);
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
				//spells.getLog().info("(" + x + "," + y + "," + z + ") : " + fillBlock + " = " + distanceSquared + " : " + maxDistanceSquared);
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

	public int getDistanceSquared(int x, int y, int z)
	{
		return x * x + y * y + z * z;
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
		
		if (y < 0 || y > MAX_Y) return true;
		
		// Make sure the block is loaded.
		Block block = center.getWorld().getBlockAt(x, y, z);
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
			return false;
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
				constructedBlocks.add(block);
				powerData.setPowered(!powerData.isPowered());
				powerBlock = true;
			} else if (data instanceof Lever) {
				Lever powerData = (Lever)data;
				constructedBlocks.add(block);
				powerData.setPowered(!powerData.isPowered());
				powerBlock = true;
			} else if (data instanceof PistonBaseMaterial) {
				PistonBaseMaterial powerData = (PistonBaseMaterial)data;
				constructedBlocks.add(block);
				powerData.setPowered(!powerData.isPowered());
				powerBlock = true;
			} else if (data instanceof PoweredRail) {
				PoweredRail powerData = (PoweredRail)data;
				constructedBlocks.add(block);
				powerData.setPowered(!powerData.isPowered());
				powerBlock = true;
			} else if (data instanceof RedstoneWire) {
				RedstoneWire wireData = (RedstoneWire)data;
				constructedBlocks.add(block);
				wireData.setData((byte)(15 - wireData.getData()));
				powerBlock = true;
			} else if (material == Material.REDSTONE_BLOCK) {
				constructedBlocks.add(block);
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, material.getId());
				block.setType(Material.AIR);
			} else if (material == Material.REDSTONE_TORCH_OFF) {
				constructedBlocks.add(block);
				block.setType(Material.REDSTONE_TORCH_ON);
			} else if (material == Material.REDSTONE_TORCH_ON) {
				constructedBlocks.add(block);
				block.setType(Material.REDSTONE_TORCH_OFF);
			} else if (material == Material.TNT) {
				// Not adding this to the undo batch because
				// A) The main use of this is for the EMP spell, which auto-undoes itself
				// B) We're about to spawn primed TNT, so there's no going back anyway...
				//    (or the hopefully-future snapshot undo feature will take over anyway... maybe?)
				// constructedBlocks.add(block);
				block.setType(Material.AIR);
				// Um, sure why not?
				block.getLocation().getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT);
			}
			
			if (powerBlock) {
				blockState.update();
			}
			
			return true;
		}

		// Prepare material brush, it may update
		// given the current target (clone, replicate)
		MaterialBrush brush = spell.getMaterialBrush();
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
			
			// Check for command overrides
			if (commandMap != null && brush.getMaterial() == Material.COMMAND) {
				String commandKey = brush.getCommandLine();
				if (commandKey != null && commandKey.length() > 0 && commandMap.containsKey(commandKey)) {
					brush.setCommandLine(commandMap.get(commandKey));
				}
			}
			
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
	
	public void addCommandMapping(String key, String command) {
		if (commandMap == null) {
			commandMap = new HashMap<String, String>();
		}
		
		commandMap.put(key,  command);
	}
}
