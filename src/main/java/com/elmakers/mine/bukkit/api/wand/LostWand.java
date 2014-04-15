package com.elmakers.mine.bukkit.api.wand;

import org.bukkit.Location;

public interface LostWand {
	public Location getLocation();
	public String getName();
	public String getId();
	public String getOwner();
}
