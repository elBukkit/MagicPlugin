package com.elmakers.mine.bukkit.api.wand;

import org.bukkit.Location;

/**
 * Represents a Wand that has been "lost", as in spawned as an ItemStack.
 * 
 * Since Wands are indestructible by default, and show on the dynmap, they are
 * also tracked and stored persistently. 
 * 
 * A LostWand record will be triggered on Chunk load, and search for its associated
 * Wand in the Item entities loaded from the Chunk.
 * 
 * If the Wand is not found, the LostWand record will be automatically unregistered.
 * 
 * If the Wand is found, the LostWand record is updated.
 * 
 * These records are used in the Recall spell to return a Player to their lost Wands,
 * as well as to repopulate dynmap after a restart, since the markers are not persistent.
 */
public interface LostWand {
	public Location getLocation();
	public String getName();
	public String getId();
	public String getOwner();
}
