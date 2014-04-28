package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;

/**
 * Implements a Collection of Blocks, for quick getting/putting while iterating
 * over a set or area of blocks.
 * 
 * This stores BlockData objects, which are hashable via their Persisted
 * inheritance, and their LocationData id (which itself has a hash function
 * based on world name and BlockVector's hash function)
 * 
 */
public interface BlockList extends Collection<BlockData> {
	public void commit();
	public void prepareForUndo();
	
	public void scheduleCleanup(Mage mage);
	public boolean undoScheduled(Mage mage);
	public boolean undo(Mage mage);
	
	public void save(ConfigurationSection node);
	public void load(ConfigurationSection node);
}
