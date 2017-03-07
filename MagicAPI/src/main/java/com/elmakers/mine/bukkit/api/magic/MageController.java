package com.elmakers.mine.bukkit.api.magic;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.elmakers.mine.bukkit.api.block.CurrencyItem;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.maps.MapController;
import com.elmakers.mine.bukkit.api.wand.WandTemplate;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.BlockList;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

/**
 * The controller is used for more advanced plugin interaction, and is
 * used heavily by Spells themselves to interact with the Magic plugin's
 * internal functionality.
 */
public interface MageController {

    /**
     * Retrieve the Logger for this API instance.
     *
     * @return The logger being used by the API provider (usually MagicPlugin).
     */
    public Logger getLogger();

    /**
     * Retrieve the Plugin that owns this controller.
     *
     * This will generally be the MagicPlugin, but.. maybe not always?
     *
     * @return The owning plugin instance
     */
    public Plugin getPlugin();
    public MagicAPI getAPI();
    public File getConfigFolder();

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
    public Automaton getAutomaton(Block block);

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
    public ItemStack createItem(String magicItemKey);
    public ItemStack createItem(String magicItemKey, boolean brief);
    public ItemStack createGenericItem(String itemKey);
    public Wand createUpgrade(String wandKey);
    public ItemStack createSpellItem(String spellKey);
    public ItemStack createSpellItem(String spellKey, boolean brief);
    public ItemStack createBrushItem(String brushKey);

    /**
     * Obtains a wand from an item stack. (No copying is done)
     *
     * @param item The item to get a wand of.
     * @return The wand of this item.
     * @throws IllegalArgumentException If the item is not a wand.
     */
    public Wand getWand(ItemStack item);
    public Wand getWand(ConfigurationSection config);
    public WandTemplate getWandTemplate(String key);
    public Collection<WandTemplate> getWandTemplates();
    public void loadWandTemplate(String key, ConfigurationSection wandNode);
    public void unloadWandTemplate(String key);

    public String describeItem(ItemStack item);
    public String getItemKey(ItemStack item);
    public boolean takeItem(Player player, ItemStack item);
    public boolean hasItem(Player player, ItemStack item);

    public SpellCategory getCategory(String key);
    public Collection<SpellCategory> getCategories();
    public Collection<SpellTemplate> getSpellTemplates();
    public Collection<SpellTemplate> getSpellTemplates(boolean showHidden);
    public SpellTemplate getSpellTemplate(String key);
    public Set<String> getWandPathKeys();
    public WandUpgradePath getPath(String key);

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

    public void sendToMages(String message, Location location);
    public Collection<Mage> getMages();
    public void removeMage(Mage mage);
    public void removeMage(String id);

    @Deprecated
    public void forgetMage(Mage mage);

    public String getMessagePrefix();

    public String getReflectiveMaterials(Mage mage, Location location);
    public String getDestructibleMaterials(Mage mage, Location location);
    public Set<String> getSpellOverrides(Mage mage, Location location);

    public Set<Material> getDestructibleMaterials();
    public Set<Material> getBuildingMaterials();
    public Set<Material> getRestrictedMaterials();

    public Collection<String> getMaterialSets();

    public Collection<String> getPlayerNames();

    public int getMessageThrottle();
    public boolean isMage(Entity entity);
    public Mage getMage(CommandSender sender);
    public Mage getMage(Player player);
    public Mage getMage(Entity entity);
    public Mage getMage(String id, String name);
    public Mage getRegisteredMage(String mageId);

    public Collection<String> getBrushKeys();

    public boolean commitAll();

    public void disablePhysics(int interval);

    public boolean hasPermission(CommandSender sender, String pNode, boolean defaultValue);
    public boolean hasPermission(CommandSender sender, String pNode);
    public boolean hasCastPermission(CommandSender sender, SpellTemplate spell);
    public Boolean getRegionCastPermission(Player player, SpellTemplate spell, Location location);
    public Boolean getPersonalCastPermission(Player player, SpellTemplate spell, Location location);
    public boolean isPVPAllowed(Player player, Location location);
    public boolean isExitAllowed(Player player, Location location);

    /**
     * Check whether or not one entity is allowed to target another.
     *
     * This takes into account region-based PVP permissions, mob protection, scoreboard teams and parties
     * via other plugins, depending on global settings.
     *
     * PvP and friendly fire checks can be overridden on a per-spell or global basis.
     *
     * @param attacker The attacking entity
     * @param target The entity potentially being targeted
     * @return true if "attacker" can target "target"
     */
    public boolean canTarget(Entity attacker, Entity target);

    /**
     * This checks scoreboard teams or integrated party groupings, as configured and provided by other plugins,
     * to see if two entities are "friends" with one another.
     *
     * This is primarily used for "only_friendly: true" spells to disallow targeting non-friend players.
     *
     * @param source The source entity
     * @param target The target entity
     * @return true if "source" and "target" are considered friendly.
     */
    public boolean isFriendly(Entity source, Entity target);

    public boolean isUrlIconsEnabled();
    public Set<EntityType> getUndoEntityTypes();

    public boolean sendMail(CommandSender sender, String fromPlayer, String toPlayer, String message);

    public Location getWarp(String warpName);
    public Location getTownLocation(Player player);
    public Map<String, Location> getHomeLocations(Player player);

    public void giveItemToPlayer(Player player, ItemStack itemStack);

    public UndoList undoAny(Block target);
    public UndoList undoRecent(Block target, int timeout);
    public void scheduleUndo(UndoList undoList);
    public void cancelScheduledUndo(UndoList undoList);

    public String getEntityName(Entity entity);
    public String getEntityDisplayName(Entity entity);

    public int getMaxWandFillLevel();
    public double getWorthBase();
    public double getWorthXP();
    public double getWorthSkillPoints();
    public ItemStack getWorthItem();
    public double getWorthItemAmount();
    public CurrencyItem getCurrency();
    public boolean itemsAreEqual(ItemStack first, ItemStack second);

    public void addFlightExemption(Player player, int duration);
    public void addFlightExemption(Player player);
    public void removeFlightExemption(Player player);

    /**
     * Check to see if the Elementals plugin is present an enabled.
     *
     * If so, Magic will work as a passthrough API.
     *
     * @return
     */
    public boolean elementalsEnabled();

    /**
     * Create an Elemental of a specific type at a certain location.
     *
     * @param location The location at which to create the elemental.
     * @param templateName The name of the elemental's template description
     * @param creator The creator of the elemental, may be null.
     * @return False on failure, such as if there are already too many elementals in the world.
     */
    public boolean createElemental(Location location, String templateName, CommandSender creator);

    /**
     * Check to see if the given Entity is an Elemental, or part of an Elemental.
     *
     * @param entity The entity to check
     * @return true if this Entity is an Elemental, or part of one.
     */
    public boolean isElemental(Entity entity);

    /**
     * Check to see if the given Entity is an NPC of any kind.
     *
     * This currently integrates with Citizens and Shopkeepers, and
     * relies on Entity metadata.
     */
    public boolean isNPC(Entity entity);
    public boolean isVanished(Entity entity);

    /**
     * Damage the Elemental represented by the given Entity.
     *
     * @param entity The entity to use to determine which Elemental to damage.
     * @param damage The amount of damage to apply.
     * @param fireTicks The duration of fire, if any, to apply.
     * @param attacker The attacker, initiator of damage. May be a command block or console, or null.
     * @return False if the entity is not a living Elemental.
     */
    public boolean damageElemental(Entity entity, double damage, int fireTicks, CommandSender attacker);

    /**
     * Change the scale of an Elemental, making it bigger or smaller.
     *
     * Elementals will limit their own size within sane limits. (0.1 - 5.0 for instance).
     *
     * @param entity The entity representing the Elemental to scale
     * @param scale The new scale
     * @return False if the entity is not an Elemental, or other failure
     */
    public boolean setElementalScale(Entity entity, double scale);

    /**
     * Get the current scale of an Elemental.
     *
     * @param entity The entity representing the Elemental to interrogate
     * @return The scale of the elemental, or 0 on error.
     */
    public double getElementalScale(Entity entity);

    /**
     * Check to see if Magic sounds are enabled.
     *
     * @return true if sounds are enabled.
     */
    public boolean soundsEnabled();

    public MaterialAndData getRedstoneReplacement();

    /**
     * Get Magic's localization store.
     *
     * @return The Messages controller for getting in-game text.
     */
    public Messages getMessages();

    /**
     * Get Magic's URLMap controller, for generating custom
     * image maps.
     */
    public MapController getMaps();

    /**
     * Use this to safely load a Magic ItemStack from a config.
     *
     * @param root
     * @param key
     * @return
     */
    public ItemStack deserialize(ConfigurationSection root, String key);

    /**
     * Use this to safely save a Magic ItemStack to a config.
     *
     * @param root
     * @param key
     * @param item
     */
    public void serialize(ConfigurationSection root, String key, ItemStack item);

    public boolean isLocked(Block block);
    public void sendPlayerToServer(final Player player, final String server);
    public void warpPlayerToServer(final Player player, final String server, final String warp);
    public boolean spawnPhysicsBlock(Location location, Material material, short data, Vector velocity);
    public boolean isDisguised(Entity entity);
    public boolean isPathUpgradingEnabled();
    public boolean isSpellUpgradingEnabled();
    public boolean isSpellProgressionEnabled();
    public boolean isSPEnabled();
    public boolean isSPEarnEnabled();
    public int getSPMaximum();
    public void deleteMage(final String id);
    public void disableItemSpawn();
    public void enableItemSpawn();
    public void setForceSpawn(boolean force);
    public String getSpell(ItemStack item);
    public String getSpellArgs(ItemStack item);
    
    public Set<String> getMobKeys();
    public Entity spawnMob(String key, Location location);
    public EntityData getMob(String key);
    public EntityData getMobByName(String name);
    public EntityData loadMob(ConfigurationSection configuration);
    public String getBlockSkin(Material blockType);
    public String getMobSkin(EntityType mobType);
    public void checkResourcePack(CommandSender sender);
    public boolean commitOnQuit();
    
    public Set<String> getItemKeys();
    public ItemData getItem(String key);
    public ItemData getOrCreateItem(String key);
    public ItemData getItem(ItemStack match);
    public void unloadItemTemplate(String key);
    public void loadItemTemplate(String key, ConfigurationSection itemNode);
    public Double getWorth(ItemStack item);
    public boolean disguise(Entity entity, ConfigurationSection configuration);
    public void managePlayerData(boolean external, boolean backupInventories);
    String getDefaultWandTemplate();
    String getHeroesSkillPrefix();

    Object getWandProperty(ItemStack itemStack, String key);
    @Nonnull <T> T getWandProperty(ItemStack itemStack, @Nonnull String key, @Nonnull T defaultValue);

    /**
     * Remove all custom Magic data from an item, but leave other metadata intact.
     * @param itemStack
     */
    void cleanItem(ItemStack itemStack);
}
