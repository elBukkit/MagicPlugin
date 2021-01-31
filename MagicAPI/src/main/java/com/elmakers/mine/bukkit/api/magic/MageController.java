package com.elmakers.mine.bukkit.api.magic;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.BlockList;
import com.elmakers.mine.bukkit.api.block.CurrencyItem;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.effect.EffectContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.maps.MapController;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.api.protection.PlayerWarp;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandTemplate;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;

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
     * Log a message, but only if debug messages are enabled
     *
     * @param debug The message to log
     */
    void info(String debug);

    /**
     * Retrieve the Plugin that owns this controller.
     *
     * <p>This will generally be the MagicPlugin, but.. maybe not always?
     *
     * @return The owning plugin instance
     */
    Plugin getPlugin();
    MagicAPI getAPI();
    File getConfigFolder();
    File getDataFolder();

    /**
     * Return a list of all known LostWand records.
     *
     * @return A list of all known LostWand data.
     */
    Collection<LostWand> getLostWands();

    /**
     * Creates a copy of the given item and turns it into a Wand.
     *
     * @param item The item to get a wand of.
     * @return The wand of this item.
     */
    @Nonnull
    Wand createWand(@Nonnull ItemStack item);

    /**
     * Create a new Wand from a template.
     *
     * <p>Once created, a Wand is a unique item. It "remembers" which template
     * it was created from, but this is currently not used for anything.
     *
     * @param wandKey The template key, or blank for a default wand.
     * @return A new Wand instance, with a useable ItemStack.
     */
    @Nullable
    Wand createWand(String wandKey);
    @Nullable
    ItemStack createItem(String magicItemKey);
    @Nullable
    ItemStack createItem(String magicItemKey, boolean brief);
    @Nullable
    ItemStack createItem(String magicItemKey, Mage mage, boolean brief, ItemUpdatedCallback callback);
    @Nullable
    ItemStack createGenericItem(String itemKey);
    Wand createUpgrade(String wandKey);
    @Nullable
    ItemStack createSpellItem(String spellKey);
    @Nullable
    ItemStack createSpellItem(String spellKey, boolean brief);
    @Nullable
    ItemStack createBrushItem(String brushKey);

    /**
     * Only returns a Wand instance of the item is already a wand.
     */
    @Nullable
    Wand getIfWand(ItemStack item);

    /**
     * Obtains a wand from an item stack. (No copying is done)
     *
     * @param item The item to get a wand of.
     * @return The wand of this item.
     * @throws IllegalArgumentException If the item is not a wand.
     */
    Wand getWand(ItemStack item);
    Wand getWand(ConfigurationSection config);
    @Nullable
    WandTemplate getWandTemplate(String key);
    Collection<String> getWandTemplateKeys();
    Collection<WandTemplate> getWandTemplates();
    void loadWandTemplate(String key, ConfigurationSection wandNode);
    void unloadWandTemplate(String key);

    String describeItem(ItemStack item);
    String getItemKey(ItemStack item);
    @Nullable
    String getWandKey(ItemStack item);
    boolean takeItem(Player player, ItemStack item);
    boolean hasItem(Player player, ItemStack item);

    @Nullable
    SpellCategory getCategory(String key);
    Collection<SpellCategory> getCategories();
    Collection<String> getSpellTemplateKeys();
    Collection<SpellTemplate> getSpellTemplates();
    Collection<SpellTemplate> getSpellTemplates(boolean showHidden);
    Collection<String> getAutomatonTemplateKeys();
    Collection<String> getRecipeKeys();
    Collection<String> getAutoDiscoverRecipeKeys();
    @Nullable
    SpellTemplate getSpellTemplate(String key);
    Set<String> getWandPathKeys();
    @Nullable
    WandUpgradePath getPath(String key);

    void updateBlock(Block block);
    void updateBlock(String worldName, int x, int y, int z);
    void updateVolume(String worldName, int minx, int miny, int minz, int maxx, int maxy, int maxz);
    void update(BlockList blockList);

    boolean canCreateWorlds();
    int getMaxUndoPersistSize();

    @Nullable
    Schematic loadSchematic(String name);

    /**
     * @return Manager of material sets.
     */
    MaterialSetManager getMaterialSetManager();
    @Deprecated
    Collection<String> getMaterialSets();
    @Nullable
    @Deprecated
    Set<Material> getMaterialSet(String string);

    void sendToMages(String message, Location location);
    Collection<Mage> getMages();
    Collection<Mage> getMobMages();
    Collection<Entity> getActiveMobs();
    void removeMage(Mage mage);
    void removeMage(String id);

    /**
     * This will remove a Mage after their undo queue and pending spell casts have completed.
     */
    void forgetMage(Mage mage);

    String getMessagePrefix();

    @Nullable
    Set<String> getSpellOverrides(Mage mage, Location location);
    @Nullable
    String getReflectiveMaterials(Mage mage, Location location);

    @Nullable
    String getDestructibleMaterials(Mage mage, Location location);
    Set<Material> getDestructibleMaterials();

    Set<Material> getBuildingMaterials();
    Set<Material> getRestrictedMaterials();

    MaterialSet getDestructibleMaterialSet();
    MaterialSet getBuildingMaterialSet();
    MaterialSet getRestrictedMaterialSet();

    Collection<String> getPlayerNames();

    int getMessageThrottle();
    boolean isMage(@Nonnull Entity entity);
    @Nonnull
    Mage getMage(@Nonnull CommandSender sender);
    @Nonnull
    Mage getMage(@Nonnull Player player);
    @Nonnull
    Mage getMage(@Nonnull Entity entity);
    @Nonnull
    Mage getMage(@Nonnull String id, @Nonnull String name);
    @Nullable
    Mage getRegisteredMage(@Nonnull String mageId);
    @Nullable
    Mage getRegisteredMage(@Nonnull Entity entity);
    @Nonnull
    Mage getAutomaton(@Nonnull String id, @Nonnull String name);
    @Nonnull
    Mage getConsoleMage();
    boolean showConsoleCastFeedback();

    Collection<String> getBrushKeys();

    boolean commitAll();

    void disablePhysics(int interval);

    boolean hasPermission(CommandSender sender, String pNode, boolean defaultValue);
    boolean hasPermission(CommandSender sender, String pNode);
    boolean hasCastPermission(CommandSender sender, SpellTemplate spell);
    @Nullable
    Boolean getRegionCastPermission(Player player, SpellTemplate spell, Location location);
    @Nullable
    Boolean getPersonalCastPermission(Player player, SpellTemplate spell, Location location);
    boolean isPVPAllowed(Player player, Location location);
    boolean isExitAllowed(Player player, Location location);
    boolean hasBypassPermission(CommandSender sender);

    /**
     * Check whether or not one entity is allowed to target another.
     *
     * <p>This takes into account region-based PVP permissions, mob protection, scoreboard teams and parties
     * via other plugins, depending on global settings.
     *
     * <p>PvP and friendly fire checks can be overridden on a per-spell or global basis.
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
     * <p>This is primarily used for "only_friendly: true" spells to disallow targeting non-friend players.
     *
     * @param source The source entity
     * @param target The target entity
     * @return true if "source" and "target" are considered friendly.
     */
    boolean isFriendly(Entity source, Entity target);

    boolean isUrlIconsEnabled();
    boolean isLegacyIconsEnabled();
    Set<EntityType> getUndoEntityTypes();

    boolean sendMail(CommandSender sender, String fromPlayer, String toPlayer, String message);

    @Nullable
    Location getWarp(String warpName);
    @Nullable
    Location getTownLocation(Player player);
    @Nullable
    Map<String, Location> getHomeLocations(Player player);

    void giveItemToPlayer(Player player, ItemStack itemStack);

    @Nullable
    UndoList undoAny(Block target);
    @Nullable
    UndoList undoRecent(Block target, int timeout);
    void scheduleUndo(UndoList undoList);
    void cancelScheduledUndo(UndoList undoList);

    String getEntityName(Entity entity);
    String getEntityDisplayName(Entity entity);

    int getMaxWandFillLevel();
    double getWorthBase();
    double getWorthXP();
    double getWorthSkillPoints();
    @Nullable
    ItemStack getWorthItem();
    double getWorthItemAmount();
    @Nonnull
    Set<String> getCurrencyKeys();
    @Deprecated
    CurrencyItem getCurrency();
    Currency getCurrency(String key);
    boolean itemsAreEqual(ItemStack first, ItemStack second);
    boolean itemsAreEqual(ItemStack first, ItemStack second, boolean ignoreDamage);

    void addFlightExemption(Player player, int duration);
    void addFlightExemption(Player player);
    void removeFlightExemption(Player player);

    /**
     * Check to see if the Elementals plugin is present an enabled.
     *
     * <p>If so, Magic will work as a passthrough API.
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
     * <p>This currently integrates with Citizens.
     */
    boolean isNPC(Entity entity);
    boolean isStaticNPC(Entity entity);
    boolean isPet(Entity entity);
    boolean isMagicNPC(Entity entity);
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
     * <p>Elementals will limit their own size within sane limits. (0.1 - 5.0 for instance).
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
     */
    @Nullable
    ItemStack deserialize(ConfigurationSection root, String key);

    /**
     * Use this to safely save a Magic ItemStack to a config.
     */
    void serialize(ConfigurationSection root, String key, ItemStack item);

    boolean isLocked(Block block);
    void sendPlayerToServer(final Player player, final String server);
    void warpPlayerToServer(final Player player, final String server, final String warp);
    boolean isDisguised(Entity entity);
    boolean hasDisguises();
    boolean isPathUpgradingEnabled();
    boolean isSpellUpgradingEnabled();
    boolean isSpellProgressionEnabled();
    boolean isSPEnabled();
    boolean isSPEarnEnabled();
    boolean isVaultCurrencyEnabled();
    void depositVaultCurrency(OfflinePlayer player, double amount);
    int getSPMaximum();
    void deleteMage(final String id);
    void disableItemSpawn();
    void enableItemSpawn();
    void setForceSpawn(boolean force);
    @Nullable
    String getSpell(ItemStack item);
    @Nullable
    String getSpellArgs(ItemStack item);

    Set<String> getNPCKeys();

    /**
     * Will not return hidden mobs
     * @return
     */
    Set<String> getMobKeys();
    Set<String> getMobKeys(boolean includeHidden);
    @Nullable
    Entity spawnMob(String key, Location location);
    @Nullable
    EntityData getMob(String key);
    @Nullable
    EntityData getMob(ConfigurationSection parameters);
    @Nullable
    EntityData getMob(Entity entity);
    @Nullable
    Entity replaceMob(Entity targetEntity, EntityData replaceType, boolean force, CreatureSpawnEvent.SpawnReason reason);
    @Nullable
    EntityData getMobByName(String name);
    EntityData loadMob(ConfigurationSection configuration);
    @Nullable
    String getBlockSkin(Material blockType);
    @Nullable
    String getMobSkin(EntityType mobType);
    @Nullable
    String getPlayerSkin(Player player);
    @Nullable
    Material getMobEgg(EntityType mobType);
    @Nonnull
    @Deprecated
    ItemStack getSkull(Entity entity, String itemName);
    @Nonnull
    ItemStack getSkull(Entity entity, String itemName, ItemUpdatedCallback callback);
    @Nonnull
    ItemStack getSkull(Player player, String itemName);
    @Nonnull
    @Deprecated
    ItemStack getSkull(String ownerName, String itemName);
    @Nonnull
    ItemStack getSkull(String ownerName, String itemName, ItemUpdatedCallback callback);
    @Nonnull
    ItemStack getSkull(UUID uuid, String itemName, ItemUpdatedCallback callback);
    @Nonnull
    ItemStack getMap(int mapId);
    void setSkullOwner(Skull skull, String ownerName);
    void setSkullOwner(Skull block, UUID uuid);
    @Nonnull
    ItemStack getURLSkull(String url);
    void checkResourcePack(CommandSender sender);
    boolean sendResourcePackToAllPlayers(CommandSender sender);
    boolean sendResourcePack(Player player);
    boolean promptResourcePack(Player player);
    boolean promptNoResourcePack(Player player);
    @Nullable
    String getResourcePackURL();
    @Nullable
    String getResourcePackURL(CommandSender sender);
    boolean isResourcePackEnabledByDefault();
    boolean isResourcePackEnabled();
    Collection<String> getAlternateResourcePacks();
    boolean commitOnQuit();
    int getMaxHeight(World world);

    Set<String> getItemKeys();
    @Nullable
    ItemData getItem(String key);
    @Nullable
    ItemData getItem(ItemStack match);
    @Nullable
    ItemData createItemData(ItemStack match);
    @Nullable
    ItemData getOrCreateItem(String key);

    /**
     * Redirects to getOrCreateItem
     */
    @Nullable
    @Deprecated
    ItemData getOrCreateItemOrWand(String key);

    /**
     * Redirects to getOrCreateItem
     */
    @Nullable
    @Deprecated
    ItemData getOrCreateMagicItem(String key);
    void unloadItemTemplate(String key);
    void loadItemTemplate(String key, ConfigurationSection itemNode);
    @Nullable
    Double getWorth(ItemStack item);
    @Nullable
    Double getEarns(ItemStack item);
    boolean disguise(Entity entity, ConfigurationSection configuration);
    void managePlayerData(boolean external, boolean backupInventories);
    String getDefaultWandTemplate();
    String getHeroesSkillPrefix();

    @Nullable
    Object getWandProperty(ItemStack itemStack, String key);
    @Nonnull
    <T> T getWandProperty(ItemStack itemStack, @Nonnull String key, @Nonnull T defaultValue);

    /**
     * Remove all custom Magic data from an item, but leave other metadata intact.
     */
    void cleanItem(ItemStack itemStack);

    Set<String> getMageClassKeys();
    MageClassTemplate getMageClassTemplate(String key);

    @Nullable
    ModifierTemplate getModifierTemplate(String key);
    @Nonnull
    Collection<String> getModifierTemplateKeys();

    // LightAPI integration
    boolean createLight(Location location, int lightlevel, boolean async);
    boolean deleteLight(Location location, boolean async);
    boolean updateLight(Location location, boolean force);
    boolean updateLight(Location location);
    boolean isLightingAvailable();
    int getLightCount();

    @Nullable
    String checkRequirements(@Nonnull CastContext context, @Nullable Collection<Requirement> requirements);
    @Nonnull
    Set<String> getDamageTypes();

    /**
     * Get all attributes, including those registered by external plugins.
     * Note that this does not include the "bowpull" or other specialty attributes.
     *
     * @return a Set of all attributes registered.
     */
    @Nonnull
    Set<String> getAttributes();

    /**
     * Get all attributes defined in attributes.yml or other configs.
     *
     * @return a Set of all attributes configured in Magic configs.
     */
    @Nonnull
    Set<String> getInternalAttributes();

    boolean isWand(ItemStack item);
    boolean isWandUpgrade(ItemStack item);
    boolean isSkill(ItemStack item);
    boolean isMagic(ItemStack item);

    /**
     * Return all example configs that have been loaded via example: or add_examples: in config.yml
     *
     * @return A list, possibly empty, of examples loaded
     */
    @Nonnull
    Collection<String> getLoadedExamples();

    /**
     * Get the primary example loaded via "example: ..." in config.yml
     *
     * @return the primary example, or null if none is enabled
     */
    @Nullable
    String getExample();

    /**
     * Return all example configs that are available built into the plugin
     *
     * @return A list of all builtin examples
     */
    @Nonnull
    Collection<String> getExamples();

    /**
     * Return all external example configs that are referenced by the builtin configs
     * or have been loaded with the mconfig command
     */
    @Nonnull
    Collection<String> getExternalExamples();

    /**
     * Return the URL for an existing loaded example
     * This may be from the builtin configs, or one that was previously fetched via the mconfig command
     */
    @Nullable
    String getExternalExampleURL(String exampleKey);

    /**
     * Return all builtin localizations. These are represented as ISO language codes.
     *
     * @return A list of all builtin localizations
     */
    @Nonnull
    Collection<String> getLocalizations();

    double getBlockDurability(@Nonnull Block block);

    /**
     * Get the spell that should be cast when a player uses the /mskills command.
     *
     * @return The key of the skill selector spell, "skills" by default.
     */
    @Nonnull
    String getSkillsSpell();

    /**
     * Retrieve a static Random instance to use for randomization.
     *
     * @return An instance of Random
     */
    @Nonnull
    Random getRandom();

    /**
     * Retrieve a list of effects defined in effects.yml
     *
     * @param effectKey The effect key
     * @return A collection of effect players
     */
    @Nonnull
    Collection<EffectPlayer> getEffects(@Nonnull String effectKey);
    @Nonnull
    Collection<String> getEffectKeys();

    void playEffects(@Nonnull String effectKey, @Nonnull Location sourceLocation, @Nonnull Location targetLocation);
    void playEffects(@Nonnull String effectKey, @Nonnull EffectContext context);

    @Nullable
    Collection<EffectPlayer> loadEffects(ConfigurationSection configuration, String effectKey);

    @Nullable
    Collection<EffectPlayer> loadEffects(ConfigurationSection configuration, String effectKey, String logContext);

    void logBlockChange(@Nonnull Mage mage, @Nonnull BlockState priorState, @Nonnull BlockState newState);

    boolean inTaggedRegion(Location location, Set<String> tags);

    boolean isFileLockingEnabled();

    @Nullable
    String getPlaceholder(Player player, String namespace, String placeholder);

    @Nonnull
    String setPlaceholders(Player player, String message);

    @Nullable
    MagicAttribute getAttribute(String attributeKey);
    boolean isUndoable(@Nonnull Material material);

    @Nonnull
    Collection<MagicNPC> getNPCs();
    void removeNPC(MagicNPC npc);
    @Nullable
    MagicNPC addNPC(Mage creator, String name);
    @Nullable
    MagicNPC getNPC(@Nullable Entity entity);
    @Nullable
    MagicNPC getNPC(UUID id);
    void registerMob(@Nonnull Entity entity, @Nonnull EntityData entityData);
    @Nonnull
    Collection<Chunk> getLockedChunks();
    void lockChunk(Chunk chunk);
    void unlockChunk(Chunk chunk);
    @Nullable
    String getAutoWandKey(@Nonnull Material material);
    @Nonnull
    Set<String> getPlayerWarpProviderKeys();
    @Nullable
    Collection<PlayerWarp> getPlayerWarps(Player player, String key);

    /**
     * Register an implementation of one or more MageProvider interfaces.
     * @param provider The provider to register
     * @return false if none of the implemented interfaces were recognized
     */
    boolean register(MagicProvider provider);
    void loadConfigurationQuietly(CommandSender sender);
    @Nullable
    List<DeathLocation> getDeathLocations(Player player);

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
}
