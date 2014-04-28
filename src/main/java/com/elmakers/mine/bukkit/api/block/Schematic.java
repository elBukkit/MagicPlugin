package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.entity.EntityData;

public interface Schematic {
	public boolean contains(Vector v);
	public MaterialAndData getBlock(Vector v);
	public Collection<EntityData> getEntities(Location center, int radius);
	public Collection<EntityData> getAllEntities();
}
