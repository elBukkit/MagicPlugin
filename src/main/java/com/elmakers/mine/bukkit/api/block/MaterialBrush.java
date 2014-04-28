package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;

public interface MaterialBrush extends MaterialAndData {
	public void prepare();
	public boolean isReady();
	public Collection<EntityData> getEntities(final Location center, final int radius);
	public boolean hasEntities();
	public boolean update(final Mage mage, final Location location);
	public void update(String activeMaterial);
	public void activate(final Location location, final String material);
	public void setTarget(Location target);
	public void setTarget(Location target, Location center);
}
