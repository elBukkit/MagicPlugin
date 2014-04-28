package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface BlockList extends Collection<BlockData> {
	public void commit();
	public void prepareForUndo();
	
	public void scheduleCleanup(Mage mage);
	public boolean undoScheduled(Mage mage);
	public boolean undo(Mage mage);
	
	public void save(ConfigurationSection node);
	public void load(ConfigurationSection node);
}
