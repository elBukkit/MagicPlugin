package com.elmakers.mine.bukkit.entity;

import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.InventoryUtils;

/**
 * This class stores information about an Entity.
 *
 */
public class EntityData implements com.elmakers.mine.bukkit.api.entity.EntityData {
	protected Location location;
	protected EntityType type;
	protected Art art;
	protected BlockFace facing;
	protected ItemStack item;
	protected double health = 1;
	
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
		if (entity instanceof Painting) {
			Painting painting = (Painting)entity;
			art = painting.getArt();
		}
		if (entity instanceof ItemFrame) {
			ItemFrame itemFrame = (ItemFrame)entity;
			item = itemFrame.getItem();
		}
		if (entity instanceof LivingEntity) {
			LivingEntity li = (LivingEntity)entity;
			// This is a bit of a hack, but we don't generally store dead entities
			// unless we want to bring them back to life.
			if (li.isDead() || li.getHealth() <= 0) {
				this.health = li.getMaxHealth();
			} else {
				this.health = li.getHealth();
			}
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
	
	@Override
	public Entity spawn() {
		Entity spawned = null;
		switch (type) {
		case PAINTING:
			spawned = InventoryUtils.spawnPainting(location, facing, art);
		break;
		case ITEM_FRAME:
			spawned = InventoryUtils.spawnItemFrame(location, facing, item);
			break;
		case DROPPED_ITEM:
			// TODO: Handle this
			spawned = null;
			break;
		default: 
			spawned = location.getWorld().spawnEntity(location, type);
			if (spawned instanceof LivingEntity) {
				LivingEntity li = (LivingEntity)spawned;
				if (health <= li.getMaxHealth()) {
					li.setHealth(health);
				}
			}
		}
		
		return spawned;
	}
}
