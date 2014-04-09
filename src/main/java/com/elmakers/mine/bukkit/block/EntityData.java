package com.elmakers.mine.bukkit.block;

import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.inventory.ItemStack;

/**
 * This class stores information about an Entity.
 *
 */
public class EntityData {
	protected Location location;
	protected EntityType type;
	protected Art art;
	protected BlockFace facing;
	protected ItemStack item;
	
	public EntityData(Location location, EntityType type) {
		this.type = type;
		this.location = location;
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
	}
	
	public Location getLocation() {
		return location;
	}

	public EntityType getType() {
		return type;
	}
	
	public Art getArt() {
		return art;
	}

	public BlockFace getFacing() {
		return facing;
	}

	public ItemStack getItem() {
		return item;
	}
}
