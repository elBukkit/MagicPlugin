package com.elmakers.mine.bukkit.api.entity;

import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface EntityData {
	public Location getLocation();
	public EntityType getType();
	public Art getArt();
	public BlockFace getFacing();
	public ItemStack getItem();
	public double getHealth();
	
	public Entity spawn();
	public boolean modify(Entity entity);
}
