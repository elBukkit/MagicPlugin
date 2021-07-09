package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
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
* or EffectPlayer class, you will need to build against Magic directly.
*
* \section issues_sec Issues
*
* For issues with the API, or suggestions, use our Issue Tracker:
*
* https://github.com/elBukkit/MagicPlugin/issues
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
*     &lt;groupId&gt;org.bukkit&lt;/groupId&gt;
*     &lt;artifactId&gt;bukkit&lt;/artifactId&gt;
*     &lt;version&gt;1.6.4-R2.0&lt;/version&gt;
*     &lt;scope&gt;provided&lt;/scope&gt;
* &lt;/dependency&gt;
* &lt;dependency&gt;
*     &lt;groupId&gt;com.elmakers.mine.bukkit&lt;/groupId&gt;
*     &lt;artifactId&gt;MagicAPI&lt;/artifactId&gt;
*     &lt;version&gt;1.0&lt;/version&gt;
*     &lt;scope&gt;provided&lt;/scope&gt;
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
*       MagicAPI getMagicAPI() {
*           Plugin magicPlugin = Bukkit.getPluginManager().getPlugin("Magic");
*             if (magicPlugin == null || !(magicPlugin instanceof MagicAPI)) {
*                 return null;
*             }
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
 * <p>A Plugin should generally check for the "Magic" plugin, and
 * then cast that Plugin to MagicAPI to interact with it.
 */
public interface MagicAPI {

    /**
     * Retrieve the Bukkit Plugin for Magic
     *
     * @return The Magic Plugin instance, likely "this"
     */
    Plugin getPlugin();

    /**
     * Retrieve the Logger for this API instance.
     *
     * @return The logger being used by the API provider (usually MagicPlugin).
     */
    Logger getLogger();

    /**
     * Check to see if a CommandSender has permission.
     *
     * <p>This extends Bukkit's permission checks to account for special
     * Magic bypass flags and Automata behavior.
     *
     * @param sender The CommandSender, may also be a Player or null.
     * @param pNode The permission node to check
     * @return True if the CommandSender has the requested permission
     */
    boolean hasPermission(CommandSender sender, String pNode);

    /**
     * Check for permission, but include a default permission. This is generally used
     * to do permission checks against something you can't specify in the defaults (such
     * as a dynamically generated list), but would like permission to be enabled by
     * default.
     *
     * <p>This is used, for instance, in Magic's list of wands and spells- players have
     * access to all spells by default, unless a -Magic.cast.spellname pnode is added.
     *
     * @param sender The CommandSender, may also be a Player or null.
     * @param pNode The permission node to check
     * @param defaultPermission set to true to enable permission by default
     * @return True if the sender has permission
     */
    @Deprecated
    boolean hasPermission(CommandSender sender, String pNode, boolean defaultPermission);

    /**
     * Save all Magic data.
     */
    void save();

    /**
     * Reload all Magic configurations.
     */
    void reload();

    /**
     * Reload all Magic configurations, report success/fail to sender
     */
    void reload(CommandSender sender);

    /**
     * Clear all image and schematic caches
     */
    void clearCache();

    /**
     * Commit and clear all loaded undo queues
     *
     * @return True if anything was committed
     */
    boolean commit();

    /**
     * Get all currently loaded Mage instances.
     *
     * <p>These may be players or Automata (command blocks)
     *
     * @return The list of all currently active mages.
     */
    @Deprecated
    Collection<Mage> getMages();

    /**
     * Get all currently loaded Mage instances that have
     * pending construction batches.
     *
     * <p>These may be players or Automata (command blocks)
     *
     * @return The list of Mages that have pending constructions in progress.
     */
    Collection<Mage> getMagesWithPendingBatches();
    Collection<UndoList> getPendingUndo();

    /**
     * Retrieve or create a Mage for a particular CommandSender.
     *
     * <p>There is one Mage for the Console sender, one Mage for each Player,
     * and one Mage for each named Command Block.
     *
     * <p>Each Mage is persistent and singular- two command blocks with the same
     * name, if loaded at the same time, will use the same Mage. This may
     * cause conflicts with cooldowns or other persistent Spell data.
     *
     * @param sender the CommandSender (Player, Command block, etc) to turn into a Mage
     * @return Mage instance, new, created or loaded.
     */
    @Deprecated
    Mage getMage(CommandSender sender);

    /**
     * Retrieve or create a Mage for a particular Entity.
     *
     * <p>Every Entity should use its own Mage for casting spells. Entity
     * Mages are tracked by UUID, and their data is saved and restored
     * as normal.
     *
     * @param entity the Entity to turn into a Mage
     * @param sender the CommandSender to use, optional
     * @return Mage instance, new, created or loaded.
     */
    @Deprecated
    Mage getMage(Entity entity, CommandSender sender);

    /**
     * A utility method for giving an item to a player. This will place
     * the item in the player's hand if it is empty, else in the player's
     * inventory. If there is no room, the item will drop on the ground.
     *
     * <p>If the item is a Wand, and it goes into the player's hand, it
     * will be activated. Make sure to use this method, or else make
     * sure to deal with activating wands as the player gets them.
     *
     * @param player The Player to give an item to
     * @param itemStack The ItemStack to giev the player, may be a Wand.
     */
    void giveItemToPlayer(Player player, ItemStack itemStack);

    /**
     * Give experience to a player, in a way that is safe for wands.
     *
     * <p>This method may get removed in the future if Wands can be smarter
     * about XP being added externally.
     *
     * @param player The Player to give XP
     * @param xp The amount of XP to give
     */
    void giveExperienceToPlayer(Player player, int xp);

    /**
     * A utility method to get the names of all currently logged-in Players.
     *
     * <p>Useful for tab-completion.
     *
     * @return The names of all logged-in players.
     */
    Collection<String> getPlayerNames();

    /**
     * Retrieve the keys for all wand templates. These can be used
     * with createWand to create a new Wand from a template.
     *
     * @return A list of all known wand template keys.
     */
    Collection<String> getWandKeys();

    /**
     * Create a new Magic item. This could be a wand, spell, upgrade
     * or brush.
     *
     * @param magicItemKey The template key, may be a wand, spell, etc.
     * @return An ItemStack representing the magic item.
     */
    @Nullable
    ItemStack createItem(String magicItemKey);
    @Nullable
    ItemStack createItem(String magicItemKey, Mage mage);

    /**
     * Create a generic version of an item with no extra data.
     * @param magicItemKey The template key, may be a wand, spell, etc.
     * @return The specified item.
     */
    @Nullable
    ItemStack createGenericItem(String magicItemKey);

    /**
     * Return the key name of an item, so it can be re-created with
     * createItem.
     */
    String getItemKey(ItemStack item);

    /**
     * Return a string description of an item.
     *
     * <p>This will use the display name if set, then Vault if
     * present, then falling back to the Material name.
     *
     * @param item The item stack.
     * @return A description of this item
     */
    String describeItem(ItemStack item);

    /**
     * Check to see if a player has a specific item.
     */
    boolean hasItem(Player player, ItemStack item);

    /**
     * Check to see if a player has a specific item, and remove it
     * from the player's inventory.
     */
    boolean takeItem(Player player, ItemStack item);

    /**
     * Create a new Wand from a template.
     *
     * <p>Once created, a Wand is a unique item. It "remembers" which template
     * it was created from, but this is currently not used for anything.
     *
     * @param wandKey The template key, or blank for a default wand.
     * @return A new Wand instance, with a useable ItemStack.
     */
    Wand createWand(String wandKey);

    /**
     * Turn the given ItemStack into a wand
     *
     * @param item The item to use as the wand's icon.
     * @return The wand instance, or null on error.
     */
    @Nullable
    Wand createWand(ItemStack item);

    /**
     * Create a new Wand instance out of a given Material type.
     *
     * <p>This will create a new ItemStack to represent the Wand.
     *
     * @param iconMaterial The type of Material to use as the Wand icon.
     * @param iconData Data used for durability or icon variants.
     * @return A newly-created Wand.
     */
    Wand createWand(Material iconMaterial, short iconData);

    /**
     * Create an upgrade Wand item from a template.
     *
     * <p>This can be used to create upgrade items from wand
     * templates that are not originally meant to be upgrades.
     *
     * @param wandKey The template key
     * @return A new Wand instance, converted to an upgrade if necessary.
     */
    Wand createUpgrade(String wandKey);

    /**
     * Load a Wand instance of an ItemStack. Will return null if the
     * given ItemStack does not have Wand NMS data.
     *
     * @param item The item to load Wand data from.
     * @return The wand instance, or null on error.
     */
    Wand getWand(ItemStack item);

    /**
     * Check to see if an existing item is a Wand. This will inspect
     * the item's NBT data.
     *
     * @param item The ItemStack to inspect.
     * @return true if the item is a Wand, in which case getWand can be used.
     */
    boolean isWand(ItemStack item);

    /**
     * Check to see if an existing item is a wand upgrade. This will inspect
     * the item's NBT data.
     *
     * @param item The ItemStack to inspect.
     * @return true if the item is a wand upgrade, in which case getWand can be used.
     */
    boolean isUpgrade(ItemStack item);

    /**
     * Check to see if an existing item is a spell item. This will inspect
     * the item's NBT data.
     *
     * @param item The ItemStack to inspect.
     * @return true if the item is a spell, in which case getSpell can be used.
     */
    boolean isSpell(ItemStack item);

    /**
     * Check to see if an existing item is a material brush item. This will inspect
     * the item's NBT data.
     *
     * @param item The ItemStack to inspect.
     * @return true if the item is a wand upgrade, in which case getBrush can be used.
     */
    boolean isBrush(ItemStack item);

    /**
     * Get the key of the Spell or SpellTemplate represented by an item.
     *
     * @param item The item to inspect
     * @return The key of the Spell represented by this item.
     */
    @Nullable
    String getSpell(ItemStack item);

    /**
     * Get the key of the material brush represented by an item.
     *
     * @param item The item to inspect
     * @return The key of the material brush represented by this item.
     */
    @Nullable
    String getBrush(ItemStack item);

    /**
     * Create an ItemStack that represents a Spell.
     *
     * <p>This item will be absorbed by a Wand on activate, adding that Spell
     * to the Wand, if the Wand finds this item in a Player's inventory.
     *
     * @param spellKey The Spell to create an item for.
     * @return A new ItemStack, or null on error.
     */
    ItemStack createSpellItem(String spellKey);

    /**
     * Create an ItemStack that represents a Material Brush.
     *
     * <p>This item will be absorbed by a Wand on activate, adding that brush
     * to the Wand, if the Wand finds this item in a Player's inventory.
     *
     * @param brushKey The Material brush to create an item for.
     * @return A new ItemStack, or null on error.
     */
    @Nullable
    ItemStack createBrushItem(String brushKey);

    /**
     * Return a list of all known LostWand records.
     *
     * @return A list of all known LostWand data.
     */
    Collection<LostWand> getLostWands();

    /**
     * Forget a specific LostWand.
     *
     * <p>The ItemStack will not be removed if it still exists, and
     * a new LostWand record may get created if the ItemStack is found
     * again by the Chunk scanner.
     *
     * @param id The id of the LostWand to forget.
     */
    void removeLostWand(String id);

    /**
     * Get a list of all known Automaton records.
     *
     * @return The list of currently known Automaton blocks.
     */
    Collection<Mage> getAutomata();

    /**
     * Cast a specific Spell, with optional parameters.
     *
     * <p>The parameters are generally in alternating key/value format, such as
     *
     * <code>{"radius", "32", "range", "64"}</code>
     *
     * <p>This Spell will be cast using the COMMAND Mage.
     *
     * @param spellName The key name of the Spell to cast
     * @param parameters A list of parameters, as if cast from the command-line.
     * @return true if the spell succeeds, else false
     */
    boolean cast(String spellName, String[] parameters);

    /**
     * Cast a specific Spell, with optional parameters, using a specific CommandSender and/or Player.
     *
     * <p>The parameters are generally in alternating key/value format, such as
     *
     * <code>{"radius", "32", "range", "64"}</code>
     *
     * <p>The CommandSender and Player may differ, in which case both will be notified of Spell results.
     *
     * @param spellName The key name of the Spell to cast
     * @param parameters A list of parameters, as if cast from the command-line.
     * @param sender The CommandSender that originated this Spell
     * @param entity The Entity this Spell is cast on behalf of, may be Player or differ from sender
     * @return true if the spell succeeds, else false
     */
    boolean cast(String spellName, String[] parameters, CommandSender sender, Entity entity);
    boolean cast(String spellName, ConfigurationSection parameters, CommandSender sender, Entity entity);

    /**
     * Get a list of all currently loaded SpellTemplate records, as defined in spells.defaults.yml
     * and spells.yml
     *
     * <p>A Spell is created for a Mage from a SpellTemplate.
     *
     * @return A list of all known SpellTemplate definitions.
     */
    Collection<SpellTemplate> getSpellTemplates();
    Collection<SpellTemplate> getSpellTemplates(boolean showHidden);

    /**
     * Retrieve a specific SpellTemplate.
     *
     * @param key The key of the SpellTemplate to look up.
     * @return The requested SpellTemplate, or null on failure.
     */
    @Nullable
    SpellTemplate getSpellTemplate(String key);

    /**
     * Get a list of all valid Material Brush names.
     *
     * <p>This will include all Block Materials, as well as special brushes
     * (copy, clone, erase, replicate, map) and any known schematic brushes.
     *
     * @return A list of all valid brush keys.
     */
    Collection<String> getBrushes();

    /**
     * Get a list of all known schematics.
     *
     * <p>These will be loaded from Magic's built-in schematic collection,
     * or from an external source (e.g. WorldEdit).
     *
     * <p>The list may be empty if schematics are disabled.
     *
     * <p>These are the raw schematic names, and do not have the "schematic:" prefix or ".schematic" extension.
     *
     * @return The list of known schematic names.
     */
    Collection<String> getSchematicNames();

    /**
     * Get the MageController.
     *
     * <p>The controller is used for more advanced plugin interaction, and is
     * used heavily by Spells themselves to interact with the Magic plugin's
     * internal functionality.
     *
     * @return The current MageController, there is only one.
     */
    MageController getController();

    /**
     * Returns a written book item describing all of the spells in
     * a given category.
     *
     * @param category The category to look up
     * @param count How many to give (max 1 stack)
     * @return An ItemStack spell book
     */
    ItemStack getSpellBook(SpellCategory category, int count);

    /**
     * Return the Messages controller, which manages Magic's
     * localization store.
     */
    Messages getMessages();
}
