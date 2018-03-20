package com.elmakers.mine.bukkit.api.magic;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.BlockList;
import com.elmakers.mine.bukkit.api.block.CurrencyItem;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.maps.MapController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;
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
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
    Logger getLogger();

    /**
     * Retrieve the Plugin that owns this controller.
     *
     * This will generally be the MagicPlugin, but.. maybe not always?
     *
     * @return The owning plugin instance
     */
    Plugin getPlugin();
    MagicAPI getAPI();
    File getConfigFolder();

    /**
     * Return a list of all known LostWand records.
     *
     * @return A list of all known LostWand data.
     */
    Collection<LostWand> getLostWands();

    /**
     * Create a new Wand from a template.
     *
     * Once created, a Wand is a unique item. It "remembers" which template
     * it was created from, but this is currently not used for anything.
     *
     * @param wandKey The template key, or blank for a default wand.
     * @return A new Wand instance, with a useable ItemStack.
     */
    Wand createWand(String wandKey);
    ItemStack createItem(String magicItemKey);
    ItemStack createItem(String magicItemKey, boolean brief);
    ItemStack createGenericItem(String itemKey);
    Wand createUpgrade(String wandKey);
    ItemStack createSpellItem(String spellKey);
    ItemStack createSpellItem(String spellKey, boolean brief);
    ItemStack createBrushItem(String brushKey);

    /**
     * Obtains a wand from an item stack. (No copying is done)
     *
     * @param item The item to get a wand of.
     * @return The wand of this item.
     * @throws IllegalArgumentException If the item is not a wand.
     */
    Wand getWand(ItemStack item);
    Wand getWand(ConfigurationSection config);
    WandTemplate getWandTemplate(String key);
    Collection<WandTemplate> getWandTemplates();
    void loadWandTemplate(String key, ConfigurationSection wandNode);
    void unloadWandTemplate(String key);

    String describeItem(ItemStack item);
    String getItemKey(ItemStack item);
    String getWandKey(ItemStack item);
    boolean takeItem(Player player, ItemStack item);
    boolean hasItem(Player player, ItemStack item);

    SpellCategory getCategory(String key);
    Collection<SpellCategory> getCategories();
    Collection<SpellTemplate> getSpellTemplates();
    Collection<SpellTemplate> getSpellTemplates(boolean showHidden);
    SpellTemplate getSpellTemplate(String key);
    Set<String> getWandPathKeys();
    WandUpgradePath getPath(String key);

    void updateBlock(Block block);
    void updateBlock(String worldName, int x, int y, int z);
    void updateVolume(String worldName, int minx, int miny, int minz, int maxx, int maxy, int maxz);
    void update(BlockList blockList);

    boolean canCreateWorlds();
    int getMaxUndoPersistSize();

    Schematic loadSchematic(String name);

    /**
     * @return Manager of material sets.
     */
    MaterialSetManager getMaterialSetManager();
    @Deprecated
    Collection<String> getMaterialSets();
    @Deprecated
    Set<Material> getMaterialSet(String string);

    void sendToMages(String message, Location location);
    Collection<Mage> getMages();
    Collection<Mage> getMobMages();
    void removeMage(Mage mage);
    void removeMage(String id);

    /**
     * This will remove a Mage after their undo queue and pending spell casts have completed.
     *
     * @param mage
     */
    void forgetMage(Mage mage);

    String getMessagePrefix();

    String getReflectiveMaterials(Mage mage, Location location);
    String getDestructibleMaterials(Mage mage, Location location);
    Set<String> getSpellOverrides(Mage mage, Location location);

    Set<Material> getDestructibleMaterials();
    Set<Material> getBuildingMaterials();
    Set<Material> getRestrictedMaterials();

    MaterialSet getDestructibleMaterialSet();
    MaterialSet getBuildingMaterialSet();
    MaterialSet getRestrictedMaterialSet();

    Collection<String> getPlayerNames();

    int getMessageThrottle();
    boolean isMage(Entity entity);
    @Nonnull Mage getMage(CommandSender sender);
    @Nonnull Mage getMage(Player player);
    @Nonnull Mage getMage(Entity entity);
    @Nonnull Mage getMage(String id, String name);
    Mage getRegisteredMage(String mageId);
    Mage getRegisteredMage(Entity entity);
    Mage getAutomaton(String id, String name);

    Collection<String> getBrushKeys();

    boolean commitAll();

    void disablePhysics(int interval);

    boolean hasPermission(CommandSender sender, String pNode, boolean defaultValue);
    boolean hasPermission(CommandSender sender, String pNode);
    boolean hasCastPermission(CommandSender sender, SpellTemplate spell);
    Boolean getRegionCastPermission(Player player, SpellTemplate spell, Location location);
    Boolean getPersonalCastPermission(Player player, SpellTemplate spell, Location location);
    boolean isPVPAllowed(Player player, Location location);
    boolean isExitAllowed(Player player, Location location);

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
    boolean canTarget(Entity attacker, Entity target);

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
    boolean isFriendly(Entity source, Entity target);

    boolean isUrlIconsEnabled();
    Set<EntityType> getUndoEntityTypes();

    boolean sendMail(CommandSender sender, String fromPlayer, String toPlayer, String message);

    Location getWarp(String warpName);
    Location getTownLocation(Player player);
    Map<String, Location> getHomeLocations(Player player);

    void giveItemToPlayer(Player player, ItemStack itemStack);

    UndoList undoAny(Block target);
    UndoList undoRecent(Block target, int timeout);
    void scheduleUndo(UndoList undoList);
    void cancelScheduledUndo(UndoList undoList);

    String getEntityName(Entity entity);
    String getEntityDisplayName(Entity entity);

    int getMaxWandFillLevel();
    double getWorthBase();
    double getWorthXP();
    double getWorthSkillPoints();
    ItemStack getWorthItem();
    double getWorthItemAmount();
    CurrencyItem getCurrency();
    boolean itemsAreEqual(ItemStack first, ItemStack second);

    void addFlightExemption(Player player, int duration);
    void addFlightExemption(Player player);
    void removeFlightExemption(Player player);

    /**
     * Check to see if the Elementals plugin is present an enabled.
     *
     * If so, Magic will work as a passthrough API.
     *
     * @return
     */
    boolean elementalsEnabled();

    /**
     * Create an Elemental of a specific type at a certain location.
     *
     * @param location The location at which to create the elemental.
     * @param templateName The name of the elemental's template description
     * @param creator The creator of the elemental, may be null.
     * @return False on failure, such as if there are already too many elementals in the world.
     */
    boolean createElemental(Location location, String templateName, CommandSender creator);

    /**
     * Check to see if the given Entity is an Elemental, or part of an Elemental.
     *
     * @param entity The entity to check
     * @return true if this Entity is an Elemental, or part of one.
     */
    boolean isElemental(Entity entity);

    /**
     * Check to see if the given Entity is an NPC of any kind.
     *
     * This currently integrates with Citizens and Shopkeepers, and
     * relies on Entity metadata.
     */
    boolean isNPC(Entity entity);
    boolean isVanished(Entity entity);

    /**
     * Damage the Elemental represented by the given Entity.
     *
     * @param entity The entity to use to determine which Elemental to damage.
     * @param damage The amount of damage to apply.
     * @param fireTicks The duration of fire, if any, to apply.
     * @param attacker The attacker, initiator of damage. May be a command block or console, or null.
     * @return False if the entity is not a living Elemental.
     */
    boolean damageElemental(Entity entity, double damage, int fireTicks, CommandSender attacker);

    /**
     * Change the scale of an Elemental, making it bigger or smaller.
     *
     * Elementals will limit their own size within sane limits. (0.1 - 5.0 for instance).
     *
     * @param entity The entity representing the Elemental to scale
     * @param scale The new scale
     * @return False if the entity is not an Elemental, or other failure
     */
    boolean setElementalScale(Entity entity, double scale);

    /**
     * Get the current scale of an Elemental.
     *
     * @param entity The entity representing the Elemental to interrogate
     * @return The scale of the elemental, or 0 on error.
     */
    double getElementalScale(Entity entity);

    /**
     * Check to see if Magic sounds are enabled.
     *
     * @return true if sounds are enabled.
     */
    boolean soundsEnabled();

    MaterialAndData getRedstoneReplacement();

    /**
     * Get Magic's localization store.
     *
     * @return The Messages controller for getting in-game text.
     */
    Messages getMessages();

    /**
     * Get Magic's URLMap controller, for generating custom
     * image maps.
     */
    MapController getMaps();

    /**
     * Use this to safely load a Magic ItemStack from a config.
     *
     * @param root
     * @param key
     * @return
     */
    ItemStack deserialize(ConfigurationSection root, String key);

    /**
     * Use this to safely save a Magic ItemStack to a config.
     *
     * @param root
     * @param key
     * @param item
     */
    void serialize(ConfigurationSection root, String key, ItemStack item);

    boolean isLocked(Block block);
    void sendPlayerToServer(final Player player, final String server);
    void warpPlayerToServer(final Player player, final String server, final String warp);
    boolean spawnPhysicsBlock(Location location, Material material, short data, Vector velocity);
    boolean isDisguised(Entity entity);
    boolean isPathUpgradingEnabled();
    boolean isSpellUpgradingEnabled();
    boolean isSpellProgressionEnabled();
    boolean isSPEnabled();
    boolean isSPEarnEnabled();
    int getSPMaximum();
    void deleteMage(final String id);
    void disableItemSpawn();
    void enableItemSpawn();
    void setForceSpawn(boolean force);
    String getSpell(ItemStack item);
    String getSpellArgs(ItemStack item);
    
    Set<String> getMobKeys();
    Entity spawnMob(String key, Location location);
    EntityData getMob(String key);
    EntityData getMobByName(String name);
    EntityData loadMob(ConfigurationSection configuration);
    String getBlockSkin(Material blockType);
    String getMobSkin(EntityType mobType);
    void checkResourcePack(CommandSender sender);
    boolean sendResourcePackToAllPlayers(CommandSender sender);
    boolean sendResourcePack(Player player);
    boolean promptResourcePack(Player player);
    boolean commitOnQuit();
    
    Set<String> getItemKeys();
    ItemData getItem(String key);
    ItemData getOrCreateItem(String key);
    ItemData getItem(ItemStack match);
    void unloadItemTemplate(String key);
    void loadItemTemplate(String key, ConfigurationSection itemNode);
    Double getWorth(ItemStack item);
    boolean disguise(Entity entity, ConfigurationSection configuration);
    void managePlayerData(boolean external, boolean backupInventories);
    String getDefaultWandTemplate();
    String getHeroesSkillPrefix();

    Object getWandProperty(ItemStack itemStack, String key);
    @Nonnull <T> T getWandProperty(ItemStack itemStack, @Nonnull String key, @Nonnull T defaultValue);

    /**
     * Remove all custom Magic data from an item, but leave other metadata intact.
     * @param itemStack
     */
    void cleanItem(ItemStack itemStack);

    Set<String> getMageClassKeys();
    MageClassTemplate getMageClassTemplate(String key);
    
    // LightAPI integration
    boolean createLight(Location location, int lightlevel, boolean async);
    boolean deleteLight(Location location, boolean async);
    boolean updateLight(Location location);

    @Nullable String checkRequirements(@Nonnull CastContext context, @Nullable Collection<Requirement> requirements);
    @Nonnull Set<String> getDamageTypes();
    @Nonnull Set<String> getAttributes();

    boolean isWand(ItemStack item);
    boolean isSkill(ItemStack item);
}
