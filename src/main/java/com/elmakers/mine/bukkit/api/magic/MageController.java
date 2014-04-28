package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.wand.LostWand;

public interface MageController {
	
	/**
	 * Retrieve the Logger for this API instance.
	 * 
	 * @return The logger being used by the API provider (usually MagicPlugin).
	 */
	public Logger getLogger();
	
	/**
	 * Check to see if the given block is part of an Automata.
	 * 
	 * This currently only works if you specify the redstone block associated
	 * with the automata, though this will hopefully be improved in the future.
	 * 
	 * @param block The block to check.
	 * @return True if this is an Automata's power source.
	 */
	public boolean isAutomata(Block block);

	/**
	 * Return a list of all known LostWand records.
	 * 
	 * @return A list of all known LostWand data.
	 */
	public Collection<LostWand> getLostWands();
	
	public void registerAutomata(Block block, String name, String message);
	public boolean unregisterAutomata(Block block);
	
	public void updateBlock(Block block);
	public void updateBlock(String worldName, int x, int y, int z);
	public void updateVolume(String worldName, int minx, int miny, int minz, int maxx, int maxy, int maxz);
	
	public boolean canCreateWorlds();
	public int getMaxUndoPersistSize();
	
	public Schematic loadSchematic(String name);
	public Set<Material> getMaterialSet(String name);
	
	public int getMaxY();

	public void sendToMages(String message, Location location);
	public Collection<Mage> getMages();
	
	public Plugin getPlugin();
	public String getMessagePrefix();
	
	public Set<Material> getDestructibleMaterials();
	public Set<Material> getBuildingMaterials();
	public Set<Material> getRestrictedMaterials();
}
