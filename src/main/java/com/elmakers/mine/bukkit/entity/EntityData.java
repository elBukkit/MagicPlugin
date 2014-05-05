package com.elmakers.mine.bukkit.entity;

import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;

import com.elmakers.mine.bukkit.utility.InventoryUtils;

/**
 * This class stores information about an Entity.
 *
 */
public class EntityData implements com.elmakers.mine.bukkit.api.entity.EntityData {
	protected Location location;
	protected boolean hasMoved = false;
	protected EntityType type;
	protected Art art;
	protected BlockFace facing;
	protected ItemStack item;
	protected double health = 1;
	protected boolean isBaby;
	protected DyeColor dyeColor;
	protected Horse.Color horseColor;
	protected Horse.Variant horseVariant;
	protected Horse.Style horseStyle;
	protected SkeletonType skeletonType;
	protected Ocelot.Type ocelotType;
	protected Villager.Profession villagerProfession;
	
	public EntityData(Entity entity) {
		this(entity.getLocation(), entity);
	}
	
	public EntityData(Location location, Entity entity) {
		this.type = entity.getType();
		this.location = location;
		if (entity instanceof Hanging) {
			Hanging hanging = (Hanging)entity;
			facing = hanging.getFacing();
			
			// Bukkit gives us a one-off location, it's the attached block)
			this.location = location.getBlock().getRelative(facing.getOppositeFace()).getLocation();
		}

		if (entity instanceof LivingEntity) {
			LivingEntity li = (LivingEntity)entity;
			this.health = li.getHealth();
		}
		
		if (entity instanceof Ageable) {
			Ageable ageable = (Ageable)entity;
			this.isBaby = !ageable.isAdult();
		}
		
		if (entity instanceof Colorable) {
			Colorable colorable = (Colorable)entity;
			dyeColor = colorable.getColor();
		}

		if (entity instanceof Painting) {
			Painting painting = (Painting)entity;
			art = painting.getArt();
		} else if (entity instanceof ItemFrame) {
			ItemFrame itemFrame = (ItemFrame)entity;
			item = itemFrame.getItem();
		} else if (entity instanceof Horse) {
			Horse horse = (Horse)entity;
			horseVariant = horse.getVariant();
			horseColor = horse.getColor();
			horseStyle = horse.getStyle();
		} else if (entity instanceof Skeleton) {
			Skeleton skeleton = (Skeleton)entity;
			skeletonType = skeleton.getSkeletonType();
		} else if (entity instanceof Villager) {
			Villager villager = (Villager)entity;
			villagerProfession = villager.getProfession();
		} else if (entity instanceof Wolf) {
			Wolf wolf = (Wolf)entity;
			dyeColor = wolf.getCollarColor();
		} else if (entity instanceof Ocelot) {
			Ocelot ocelot = (Ocelot)entity;
			ocelotType = ocelot.getCatType();
		} 
	}
	
	/**
	 * API Implementation
	 */
	
	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public EntityType getType() {
		return type;
	}
	
	@Override
	public Art getArt() {
		return art;
	}

	@Override
	public BlockFace getFacing() {
		return facing;
	}

	@Override
	public ItemStack getItem() {
		return item;
	}

	@Override
	public double getHealth() {
		return health;
	}
	
	protected Entity trySpawn() {
		Entity spawned = null;
		try {
			switch (type) {
			case PLAYER:
				// Nope!
			break;
			case PAINTING:
				spawned = InventoryUtils.spawnPainting(location, facing, art);
			break;
			case ITEM_FRAME:
				spawned = InventoryUtils.spawnItemFrame(location, facing, item);
				break;
			case DROPPED_ITEM:
				// TODO: Handle this, would need to store item data.
				spawned = null;
				break;
			default: 
				spawned = location.getWorld().spawnEntity(location, type);
			}
		} catch (Exception ex) {
			
		}
		return spawned;
	}
		
	@Override
	public Entity spawn() {
		Entity spawned = trySpawn();
		if (spawned != null) {
			modify(spawned);
		}
		
		return spawned;
	}
	
	@Override
	public boolean modify(Entity entity) {
		if (entity == null || entity.getType() != type) return false;
		
		// Re-spawn if dead
		if (!entity.isValid() && !(entity instanceof Player)) {
			Entity tryEntity = trySpawn();
			if (tryEntity != null) {
				entity = tryEntity;
			}
		}
		
		if (entity instanceof Ageable) {
			Ageable ageable = (Ageable)entity;
			if (isBaby) {
				ageable.setBaby();
			} else {
				ageable.setAdult();
			}
		}
		
		if (entity instanceof Colorable) {
			Colorable colorable = (Colorable)entity;
			colorable.setColor(dyeColor);
		}

		if (entity instanceof Painting) {
			Painting painting = (Painting) entity;
			painting.setArt(art, true);
			painting.setFacingDirection(facing, true);
		} 
		else if (entity instanceof ItemFrame) {
			ItemFrame itemFrame = (ItemFrame)entity;
			itemFrame.setItem(item);
			itemFrame.setFacingDirection(facing, true);
		} else if (entity instanceof Horse) {
			Horse horse = (Horse)entity;
			horse.setVariant(horseVariant);
			horse.setStyle(horseStyle);
			horse.setColor(horseColor);
		} else if (entity instanceof Skeleton) {
			Skeleton skeleton = (Skeleton)entity;
			skeleton.setSkeletonType(skeletonType);
		} else if (entity instanceof Villager) {
			Villager villager = (Villager)entity;
			villager.setProfession(villagerProfession);
		} else if (entity instanceof Wolf) {
			Wolf wolf = (Wolf)entity;
			wolf.setCollarColor(dyeColor);
		} else if (entity instanceof Ocelot) {
			Ocelot ocelot = (Ocelot)entity;
			ocelot.setCatType(ocelotType);
		}

		if (entity instanceof LivingEntity) {
			LivingEntity li = (LivingEntity)entity;
			try {
				li.setHealth(Math.min(health, li.getMaxHealth()));
			} catch (Throwable ex) {
			}
		}
		
		if (hasMoved) {
			entity.teleport(location);
		}
		
		return true;
	}
	
	public void setHasMoved(boolean moved) {
		this.hasMoved = moved;
	}
}
