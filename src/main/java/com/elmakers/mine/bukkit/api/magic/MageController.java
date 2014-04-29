package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.BlockList;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;

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
	
	/**
	 * Create a new Wand from a template.
	 * 
	 * Once created, a Wand is a unique item. It "remembers" which template
	 * it was created from, but this is currently not used for anything.
	 * 
	 * @param wandKey The template key, or blank for a default wand.
	 * @return A new Wand instance, with a useable ItemStack.
	 */
	public Wand createWand(String wandKey);
	
	public void registerAutomata(Block block, String name, String message);
	public boolean unregisterAutomata(Block block);
	
	public void updateBlock(Block block);
	public void updateBlock(String worldName, int x, int y, int z);
	public void updateVolume(String worldName, int minx, int miny, int minz, int maxx, int maxy, int maxz);
	public void update(BlockList blockList);
	
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
	
	public Collection<String> getMaterialSets();
	
	public Collection<String> getPlayerNames();
	
	public int getMessageThrottle();
	public Mage getMage(CommandSender sender);
	
	public Collection<String> getBrushKeys();
	
	public boolean commitAll();
	
	public void disablePhysics(int interval);
	
	public boolean hasPermission(CommandSender sender, String pNode, boolean defaultValue);
	public boolean isPVPAllowed(Location location);
	
	public boolean sendMail(CommandSender sender, String fromPlayer, String toPlayer, String message);
	
	public Location getWarp(String warpName);
	
	public void giveItemToPlayer(Player player, ItemStack itemStack);
	
	public Mage undoAny(Block target);
	public void forgetMage(Mage mage);
}
