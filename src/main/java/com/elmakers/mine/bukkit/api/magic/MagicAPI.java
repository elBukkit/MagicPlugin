package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;

/*! \mainpage Magic Bukkit Plugin API
*
* \section intro_sec Introduction
*
* This is the API for the Magic plugin for Bukkit. Other plugins can
* use this API to integrate with Magic.
* 
* If you wish to extend Magic, such as by adding a completely new Spell
* or EffectPlayer class, you will need to build against MagicLib:
* 
* http://jenkins.elmakers.com/job/MagicLib/doxygen/
* 
* \section issues_sec Issues
* 
* For issues with the API, or suggestions, use our Issue Tracker:
* 
* http://jira.elmakers.com/browse/API/
* 
* \section start_sec Getting Started
* 
* If you haven't done so already, get started with Bukkit by getting a basic
* shell of a plugin working. You should at least have a working Plugin that
* loads in Bukkit (add a debug print to onEnable to be sure!) before you
* start trying to integrate with other Plugins. See here for general help:
* 
* http://wiki.bukkit.org/Plugin_Tutorial
* 
* \section maven_sec Building with Maven
* 
* Once you have a project set up, it is easy to build against the Magic API
* with Maven. Simply add the elmakers repository to your repository list,
* and then add a dependency for MagicAPI. A typical setup would look like:
* 
* <pre>
* &lt;dependencies&gt;
* &lt;dependency&gt;
* 	&lt;groupId&gt;org.bukkit&lt;/groupId&gt;
* 	&lt;artifactId&gt;bukkit&lt;/artifactId&gt;
* 	&lt;version&gt;1.6.4-R2.0&lt;/version&gt;
* 	&lt;scope&gt;provided&lt;/scope&gt;
* &lt;/dependency&gt;
* &lt;dependency&gt;
* 	&lt;groupId&gt;com.elmakers.mine.bukkit&lt;/groupId&gt;
* 	&lt;artifactId&gt;MagicAPI&lt;/artifactId&gt;
* 	&lt;version&gt;1.0&lt;/version&gt;
* 	&lt;scope&gt;provided&lt;/scope&gt;
* &lt;/dependency&gt;
* &lt;/dependencies&gt;
* &lt;repositories&gt;
* &lt;repository&gt;
*     &lt;id&gt;bukkit-repo&lt;/id&gt;
*     &lt;url&gt;http://repo.bukkit.org/content/groups/public/ &lt;/url&gt;
* &lt;/repository&gt;
* &lt;repository&gt;
*     &lt;id&gt;elmakers-repo&lt;/id&gt;
*     &lt;url&gt;http://maven.elmakers.com/repository/ &lt;/url&gt;
* &lt;/repository&gt;
* &lt;/repositories&gt;
* </pre>
* 
* \section plugin_sec Getting the API reference
* 
* A Plugin that wishes to interact with Magic should generally check
* the PluginManager for the "Magic" plugin. If present, the Plugin may
* cast it to MagicAPI and use it.
* 
* If you wish to softdepend to Magic, make sure to not use any of these API classes
* unless you know the Magic plugin is loaded. Make sure you're not building the API
* into your plugin, it should always be referenced externally (e.g. "provided" in Maven).
* 
* <pre>
*       public MagicAPI getMagicAPI() {
*           Plugin magicPlugin = Bukkit.getPluginManager().getPlugin("Magic");
* 		    if (magicPlugin == null || !(magicPlugin instanceof MagicAPI)) {
* 			    return null;
* 		    }
*           return (MagicAPI)magicPlugin;
*       }
* </pre>
* 
* \section example_sec Examples
*
* \subsection casting Casting Spells
* 
* A plugin may cast spells directly, or on behalf of logged in players.
* 
* \subsection wands Creating Wands
* 
* A plugin may create or modify Wand items.
*/

/**
 * This is the primary entry point into the Magic API.
 * 
 * A Plugin should generally check for the "Magic" plugin, and
 * then cast that Plugin to MagicAPI to interact with it.
 * 
 */
public interface MagicAPI {

	/**
	 * Retrieve the Bukkit Plugin for Magic
	 * 
	 * @return The Magic Plugin instance, likely "this"
	 */
	public Plugin getPlugin();
	
	/**
	 * Retrieve the Logger for this API instance.
	 * 
	 * @return The logger being used by the API provider (usually MagicPlugin).
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
	 * @return True if the CommandSender has the requested permission
	 */
	public boolean hasPermission(CommandSender sender, String pNode);
	
	/**
	 * Check for permission, but include a default permission. This is generally used
	 * to do permission checks against something you can't specify in the defaults (such
	 * as a dynamically generated list), but would like permission to be enabled by
	 * default.
	 * 
	 * This is used, for instance, in Magic's list of wands and spells- players have
	 * access to all spells by default, unless a -Magic.cast.spellname pnode is added.
	 * 
	 * @param sender The CommandSender, may also be a Player or null.
	 * @param pNode The permission node to check
	 * @param defaultPermission set to true to enable permission by default
	 * @return True if the sender has permission
	 */
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
	 * @return True if anything was committed
	 */
	public boolean commit();
	
	/**
	 * Get all currently loaded Mage instances.
	 * 
	 * These may be players or Automata (command blocks)
	 * 
	 * @return The list of all currently active mages.
	 */
	public Collection<Mage> getMages();
	
	/**
	 * Get all currently loaded Mage instances that have
	 * pending construction batches.
	 * 
	 * These may be players or Automata (command blocks)
	 * 
	 * @return The list of Mages that have pending constructions in progress.
	 */
	public Collection<Mage> getMagesWithPendingBatches();
	
	/**
	 * Retrieve or create a Mage for a particlar CommandSender.
	 * 
	 * There is one Mage for the Console sender, one Mage for each Player,
	 * and one Mage for each named Command Block.
	 * 
	 * Each Mage is persistent and singular- two command blocks with the same
	 * name, if loaded at the same time, will use the same Mage. This may
	 * cause conflicts with cooldowns or other persistent Spell data.
	 * 
	 * @param sender the CommandSender (Player, Command block, etc) to turn into a Mage
	 * @return Mage instance, new, created or loaded.
	 */
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

	/**
	 * A utility method to get the names of all currently logged-in Players.
	 * 
	 * Useful for tab-completion.
	 * 
	 * @return The names of all logged-in players.
	 */
	public Collection<String> getPlayerNames();

	/**
	 * Retrieve the keys for all wand templates. These can be used
	 * with createWand to create a new Wand from a template.
	 * 
	 * @return A list of all known wand template keys.
	 */
	public Collection<String> getWandKeys();
	
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
	
	/**
	 * Load a Wand instance of an ItemStack. Will return null if the
	 * given ItemStack does not have Wand NMS data.
	 * 
	 * @param item The item to load Wand data from.
	 * @return The wand instance, or null on error.
	 */
	public Wand getWand(ItemStack item);
	
	/**
	 * Create a new Wand instance out of a given Material type.
	 * 
	 * This will create a new ItemStack to represent the Wand.
	 * 
	 * @param iconMaterial The type of Material to use as the Wand icon.
	 * @param iconData Data used for durability or icon variants.
	 * @return A newly-created Wand.
	 */
	public Wand createWand(Material iconMaterial, short iconData);
	
	/**
	 * Check to see if an existing item is a Wand. This will inspect
	 * the item's NBT data.
	 * 
	 * @param item The ItemStack to inspect.
	 * @return true if the item is a Wand, in which case getWand can be used.
	 */
	public boolean isWand(ItemStack item);
	
	/**
	 * Create an ItemStack that represents a Spell.
	 * 
	 * This item will be absorbed by a Wand on activate, adding that Spell
	 * to the Wand, if the Wand finds this item in a Player's inventory.
	 * 
	 * @param spellKey The Spell to create an item for.
	 * @return A new ItemStack, or null on error.
	 */
	public ItemStack createSpellItem(String spellKey);
	
	/**
	 * Create an ItemStack that represents a Material Brush.
	 * 
	 * This item will be absorbed by a Wand on activate, adding that brush
	 * to the Wand, if the Wand finds this item in a Player's inventory.
	 * 
	 * @param brushKey The Material brush to create an item for.
	 * @return A new ItemStack, or null on error.
	 */
	public ItemStack createBrushItem(String brushKey);
	
	/**
	 * Return a list of all known LostWand records.
	 * 
	 * @return A list of all known LostWand data.
	 */
	public Collection<LostWand> getLostWands();
	
	/**
	 * Forget a specific LostWand.
	 * 
	 * The ItemStack will not be removed if it still exists, and
	 * a new LostWand record may get created if the ItemStack is found
	 * again by the Chunk scanner.
	 * 
	 * @param id The id of the LostWand to forget.
	 */
	public void removeLostWand(String id);
	
	/**
	 * Get a list of all known Automaton records.
	 * 
	 * @return The list of currently known Automaton blocks.
	 */
	public Collection<Automaton> getAutomata();
	
	/**
	 * Cast a specific Spell, with optional parameters.
	 * 
	 * The parameters are generally in alternating key/value format, such as
	 * 
	 * {"radius", "32", "range", "64"}
	 * 
	 * This Spell will be cast using the COMMAND Mage.
	 * 
	 * @param spellName The key name of the Spell to cast
	 * @param parameters A list of parameters, as if cast from the command-line.
	 */
	public void cast(String spellName, String[] parameters);
	
	/**
	 * Cast a specific Spell, with optional parameters, using a specific CommandSender and/or Player.
	 * 
	 * The parameters are generally in alternating key/value format, such as
	 * 
	 * {"radius", "32", "range", "64"}
	 * 
	 * The CommandSender and Player may differ, in which case both will be notified of Spell results.
	 * 
	 * @param spellName The key name of the Spell to cast
	 * @param parameters A list of parameters, as if cast from the command-line.
	 * @param sender The CommandSender that originated this Spell
	 * @param entity The Enttiy this Spell is cast on behalf of, may be Player or differ from sender
	 */
	public void cast(String spellName, String[] parameters, CommandSender sender, Entity entity);
	
	/**
	 * Get a list of all currently loaded SpellTemplate records, as defined in spells.defaults.yml
	 * and spells.yml
	 * 
	 * A Spell is created for a Mage from a SpellTemplate.
	 * 
	 * @return A list of all known SpellTemplate definitions.
	 */
	public Collection<SpellTemplate> getSpellTemplates();
	
	/**
	 * Retrieve a specific SpellTemplate.
	 * 
	 * @param key The key of the SpellTemplate to look up.
	 * @return The requested SpellTemplate, or null on failure.
	 */
	public SpellTemplate getSpellTemplate(String key);
	
	/**
	 * Get a list of all valid Material Brush names.
	 * 
	 * This will include all Block Materials, as well as special brushes
	 * (copy, clone, erase, replicate, map) and any known schematic brushes.
	 * 
	 * @return A list of all valid brush keys.
	 */
	public Collection<String> getBrushes();
	
	/**
	 * Get a list of all known schematics.
	 * 
	 * These will be loaded from Magic's built-in schematic collection,
	 * or from an external source (e.g. WorldEdit).
	 * 
	 * The list may be empty if schematics are disabled.
	 * 
	 * These are the raw schematic names, and do not have the "schematic:" prefix or ".schematic" extension.
	 * 
	 * @return The list of known schematic names.
	 */
	public Collection<String> getSchematicNames();
	
	/**
	 * Get the MageController.
	 * 
	 * The controller is used for more advanced plugin interaction, and is 
	 * used heavily by Spells themselves to interact with the Magic plugin's
	 * internal functionality.
	 * 
	 * @return The current MageController, there is only one.
	 */
	public MageController getController();
}
