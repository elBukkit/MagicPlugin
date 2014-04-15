package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.Automaton;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;

public interface MagicAPI {

	/**
	 * Retrieve the Bukkit Plugin for Magic
	 * 
	 * @return Plugin the Magic Plugin instance, likely "this"
	 */
	public Plugin getPlugin();
	
	/**
	 * Retrieve the Logger for this API instance.
	 * 
	 * @return
	 */
	public Logger getLogger();
	
	/**
	 * Check to see if a CommandSender has permission.
	 * 
	 * This extends Bukkit's permission checks to account for special
	 * Magic bypass flags and Autonoma behavior.
	 * 
	 * @param sender The CommandSender, may also be a Player or null.
	 * @param pNode The permission node to check
	 * @return boolean true if the CommandSender has the requested permission
	 */
	public boolean hasPermission(CommandSender sender, String pNode);
	public boolean hasPermission(CommandSender sender, String pNode, boolean defaultPermission);
	
	/**
	 * Save all Magic data.
	 */
	public void save();
	
	/**
	 * Reload all Magic configurations.
	 */
	public void reload();

	/**
	 * Clear all image and schematic caches
	 */
	public void clearCache();
	
	/**
	 * Commit and clear all loaded undo queues 
	 * 
	 * @return boolean true if anything was committed
	 */
	public boolean commit();
	
	/**
	 * Get all currently loaded Mage instances.
	 * 
	 * These may be players or Automata (command blocks)
	 */
	public Collection<Mage> getMages();
	
	/**
	 * Get all currently loaded Mage instances that have
	 * pending construction batches.
	 * 
	 * These may be players or Automata (command blocks)
	 */
	public Collection<Mage> getMagesWithPendingBatches();
	
	public Mage getMage(CommandSender sender);
	
	/**
	 * A utility method for giving an item to a player. This will place
	 * the item in the player's hand if it is empty, else in the player's
	 * inventory. If there is no room, the item will drop on the ground.
	 * 
	 * If the item is a Wand, and it goes into the player's hand, it
	 * will be activated. Make sure to use this method, or else make
	 * sure to deal with activating wands as the player gets them.
	 * 
	 * @param player The Player to give an item to
	 * @param itemStack The ItemStack to giev the player, may be a Wand.
	 */
	public void giveItemToPlayer(Player player, ItemStack itemStack);
	
	public Wand getWand(ItemStack item);
	public Wand createWand(Material iconMaterial, short iconData);
	public Wand createWand(String wandKey);
	public boolean isWand(ItemStack item);
	
	public ItemStack createSpellItem(String spellKey);
	public ItemStack createBrushItem(String brushKey);
	
	public Collection<LostWand> getLostWands();
	public void removeLostWand(String id);
	
	public Collection<Automaton> getAutomata();
	
	public void cast(String spellName, String[] parameters);
	public void cast(String spellName, String[] parameters, CommandSender sender, Player player);
	
	public Collection<Spell> getSpells();
	public Collection<String> getWandKeys();
	public Collection<String> getPlayerNames();
}
