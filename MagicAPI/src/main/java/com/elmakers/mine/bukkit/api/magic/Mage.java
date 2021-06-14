package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.batch.UndoBatch;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.block.UndoQueue;
import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.effect.SoundEffect;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.integration.ClientPlatform;
import com.elmakers.mine.bukkit.api.spell.CastParameter;
import com.elmakers.mine.bukkit.api.spell.CooldownReducer;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;

/**
 * A Mage represents any entity that may cast spells. This can include:
 * <ul>
 * <li>A Player. Mages for Players will be persisted and destroyed if a Player
 * logs out. By default a Mage will be kept around if a Player has anything in
 * their undo queue, so that an admin may Rewind their constructions after logout.
 *
 * <li>A CommandBlockSender. A Command block will have a Mage if it uses /cast.
 * Each Command block will have a unique Mage for its assigned name (assign a name to
 * a Command block using an Anvil). More than one Command block with the same name
 * (so mapping to the same Mage) may cause overlap issues with cooldowns or other
 * persistent Spell data.
 *
 * <li>A CommandSender. Any other CommandSender, such as from the server console,
 * will map to a global "COMMAND" mage.
 * </ul>
 *
 * <p>The COMMAND Mage has no Location, so is generally
 * limited in what it can cast unless the "p" location parameters are used, e.g.
 *
 * <code>cast blast pworld world px 0 py 70 pz 0</code>
 *
 * <p>This will case "blast" in the center of the world "world" using the "COMMAND" Mage.
 *
 * <p>Some Spell implementations will absolutely require a Player (such as StashSpell),
 * and so will always fail unless cast by a player or with "castp".
 */
public interface Mage extends CostReducer, CooldownReducer {
    /**
     * Return the list of pending construction batches for this Mage.
     *
     * @return Pending construction batches
     */
    Collection<Batch> getPendingBatches();

    /**
     * Get the name of this mage. This may be the Player's display name, or
     * the name of a command block (for Automata)
     *
     * @return String the display name of this mage
     */
    String getName();
    String getDisplayName();

    /**
     * Get the unique id of this mage. May be a UUID for a player, or
     * a command block name for Automata.
     *
     * @return String the unique id of this Mage
     */
    String getId();

    /**
     * Get the location of this Mage. This may be a Player location,
     * a Command block location, or null for console casting.
     *
     * @return Location the location of this Mage.
     */
    Location getLocation();

    /**
     * Get the eye location of this Mage. May be the Player eye
     * location, or the Location of the Mage plus 1.5 y.
     *
     * @return Location the location of the Mage's eyes, used for targeting.
     */
    Location getEyeLocation();

    /**
     * Gets the source location of spells cast by this mage, which were
     * not cast with a wand.
     * @return the location
     */
    @Nullable
    Location getCastLocation();

    /**
     * Currently returns the cast location, even when not holding a wand,
     * but this behavior is subject to change and in the future this may
     * return null if the Mage is not holding a Wand.
     * @return the location
     */
    @Nullable
    Location getWandLocation();
    @Nullable
    Location getOffhandWandLocation();

    /**
     * Get the direction this Mage is facing.
     *
     * @return Vector the facing direction
     */
    Vector getDirection();

    /**
     * Get the Player instance backed by this Mage.
     *
     * <p>This may return null for Command block or console-based mages.
     *
     * <p>A Spell should detect this and, if the Spell absolutely requires a
     * Player, should return SpellResult.PLAYER_REQUIRED.
     *
     * <p>Spells should attempt to avoid requiring a player, do not use the
     * Player object to get a Location or Vector direction, generally
     * try to use the Spell or Mage methods for that. This will make
     * sure that parameter overrides and command-block usage function properly.
     *
     * <p>A Player should only be needed if a Spell does something very Player-specific,
     * such as open an inventory to show the Player in-game, like StashSpell does.
     *
     * @return The player backed by this Mage, or null for Automaton Mages.
     */
    @Nullable
    Player getPlayer();

    /**
     * Get the Entity instance backed by this Mage.
     *
     * <p>This may be a Player or other Entity, or null in the case of a
     * CommandSender-based Mage, like an Automaton.
     *
     * @return The Entity represented by this Mage
     */
    Entity getEntity();

    /**
     * Get the LivingEntity instance backed by this Mage.
     *
     * <p>This is basically a helper wrapper for getEntity that does a typecheck
     * for you.
     *
     * @return The LivingEntity represented by this Mage
     */
    @Nullable
    LivingEntity getLivingEntity();

    /**
     * Get the CommandSender backed by this Mage.
     *
     * <p>This should generally always be non-null, and can be used to send a message
     * to the Mage. This may show in the server console, Player chat, or get
     * eaten by a Command block.
     *
     * @return CommandSender The sender driving this Mage.
     */
    CommandSender getCommandSender();

    /**
     * Send a message to this Mage.
     *
     * <p>This will respect the global plugin message cooldown, message
     * display settings and the Mage's active Wand "quiet" setting.
     *
     * <p>A Wand "quiet" setting of 2 will disable these messages.
     *
     * @param message The message to send.
     */
    void sendMessage(String message);
    void sendDebugMessage(String message);
    void sendDebugMessage(String message, int level);

    /**
     * Send a message to this Mage.
     *
     * <p>This will respect the global plugin "cast" message cooldown, message
     * display settings and the Mage's active Wand "quiet" setting.
     *
     * <p>A Wand "quiet" setting of 1 will disable these messages.
     *
     * @param message The message to send.
     */
    void castMessage(String message);

    /**
     * Cancel any pending construction batches.
     *
     * @return The batch that was cancelled, or null if nothing was pending.
     */
    @Nullable
    Batch cancelPending();
    @Nullable
    Batch cancelPending(String spellKey);
    @Nullable
    Batch cancelPending(boolean force);
    @Nullable
    Batch cancelPending(String spellKey, boolean force);
    @Nullable
    Batch cancelPending(String spellKey, boolean force, boolean nonBatched);

    /**
     * Undo the last construction performed by this Mage.
     *
     * <p>This will restore anything changed by the last-cast
     * construction spell, and remove that construction from
     * the Mage's UndoQueue.
     *
     * @return The UndoList that was undone, or null if none.
     */
    @Nullable
    UndoList undo();

    /**
     * Undo the last construction performed by this Mage against the
     * given Block
     *
     * <p>This will restore anything changed by the last-cast
     * construction spell by this Mage that targeted the specific Block,
     * even if it was not the most recent Spell cast by that Mage.
     *
     * @param block The block to check for modifications.
     * @return The UndoList that was undone, or null if the Mage has no constructions for the given Block.
     */
    @Nullable
    UndoList undo(Block block);

    /**
     * Commit this Mage's UndoQueue.
     *
     * <p>This will cause anything in the undo queue to become permanent-
     * meaning other overlapping spells won't undo this construction, even
     * if they were cast before the spells in this undo queue.
     *
     * <p>This also clears the Mage's undo queue, which may allow them to be
     * destroyed if they are no longer active (e.g. the Player logged out).
     *
     * <p>This has no effect on the Mage's scheduled undo batches.
     *
     * @return True if anything was commited, false if the Mage has no undo queue.
     */
    boolean commit();

    /**
     * Get the active Wand being used by this Mage.
     *
     * <p>This will generally be the Wand represented by the ItemStack held by the
     * Player this Mage represents.
     *
     * <p>Automata and other non-Player Mages generally do not have Wands.
     *
     * @return The Mage's active Wand.
     */
    Wand getActiveWand();
    Wand getOffhandWand();
    GUIAction getActiveGUI();

    /**
     * Find a wand with a particular template in the Mage's inventory.
     * If the Mage has more than one of this type of wand, only the first will be returned.
     *
     * @return A wand found in the player's inventory that matches template, or null if none found.
     */
    @Nullable
    Wand findWand(@Nonnull String template);

    /**
     * Get a bound wand. Bound wands are stored based on their template,
     * only one wand per template type is stored.
     */
    Wand getBoundWand(String template);

    /**
     * @return An immutable list of bound wands.
     */
    List<? extends Wand> getBoundWands();

    /**
     * Force a re-check of the current active wand vs the item the player is holding.
     *
     * <p>This may activate or deactivate a wand, or both.
     *
     * @return The current active wand, after checking
     */
    @Nullable
    Wand checkWand();

    /**
     * Return a Spell for this Mage, which can be used to programatically
     * cast or modify a Spell on behalf of this Mage.
     *
     * <p>This will create and register the spell if the Mage has never cast it, but has
     * permission to do so.
     *
     * @param key The key of the Spell to retrieve.
     * @return The Spell instance for this Mage, or null if the Mage does not have access to this Spell.
     */
    @Nullable
    MageSpell getSpell(String key);

    /**
     * Check to see if this Mage has a certain spell, generally meaning they
     * have cast it at least once.
     *
     * @param key The spell key
     * @return True if the Mage has ever cast this spell
     */
    boolean hasSpell(String key);

    /**
     * Return all of the Spell objects registered to this Mage.
     *
     * <p>This will generally be any Spells the Mage has ever cast.
     *
     * @return A Collection of Spell objects this Mage has cast.
     */
    Collection<Spell> getSpells();

    /**
     * Set a Spell as "active". An "active" spell is generally a toggleable on/off
     * spell. These spells may be draining mana/xp while they are active, and
     * may self-deactivate after a specific duration, or if their resources deplete.
     *
     * @param spell The spell to activate.
     */
    void activateSpell(Spell spell);

    /**
     * Deactivate a currently active spell. A spell may call this to deactivate
     * itself.
     *
     * <p>If the given spell is not currently active, nothing will happen.
     *
     * @param spell The spell to deactivate
     */
    void deactivateSpell(Spell spell);

    /**
     * Deactivate all active spells for this Mage.
     */
    void deactivateAllSpells();
    void deactivateAllSpells(boolean force, boolean quiet);
    void deactivateAllSpells(boolean force, boolean quiet, String exceptSpellKey);

    ConfigurationSection getData();

    @Override
    boolean isCooldownFree();
    @Override
    float getCooldownReduction();
    boolean isCostFree();
    boolean isConsumeFree();
    void setCostFree(boolean costFree);
    void setCooldownFree(boolean cooldownFree);
    @Override
    float getCostReduction();
    long getRemainingCooldown();
    void setRemainingCooldown(long ms);
    void reduceRemainingCooldown(long ms);
    void clearCooldown();

    boolean isSuperPowered();
    boolean isSuperProtected();
    boolean isIgnoredByMobs();

    float getRangeMultiplier();
    float getRadiusMultiplier();
    float getConstructionMultiplier();

    /**
     * Returns the combination of power-based damage multiplier, overall damage multiplier, and type-specific damage
     * multiplier.
     * @return the multiplier
     */
    double getDamageMultiplier(String damageType);

    /**
     * Returns power-based damage combined with overall damage multipliers.
     * @return the multiplier
     */
    float getDamageMultiplier();

    @Nullable
    Color getEffectColor();
    @Nullable
    String getEffectParticleName();
    float getPower();
    float getPowerMultiplier();
    void setPowerMultiplier(float power);
    float getMagePowerBonus();
    void setMagePowerBonus(float magePowerBonus);

    boolean isPlayer();
    boolean isOnline();
    boolean isDead();
    boolean isLoading();
    boolean hasLocation();

    void disable();

    void setLocation(Location location);

    /**
     * This should be called by a Spell upon
     * casting, to notify the Mage that it cast a spell.
     *
     * @param spell The Spell that was cast
     * @param result The result of the cast.
     */
    void onCast(Spell spell, SpellResult result);

    boolean isRestricted(Material material);
    // Used for checking if a brush is restricted
    // TODO: Pass some kind of proper data class instead
    boolean isRestricted(Material material, @Nullable Short data);
    @Nullable
    @Deprecated
    Set<Material> getRestrictedMaterials();
    MaterialSet getRestrictedMaterialSet();

    MageController getController();
    boolean hasCastPermission(Spell spell);
    boolean hasBuildPermission(Block block);
    boolean hasBreakPermission(Block block);
    boolean isPVPAllowed(Location location);
    boolean isIndestructible(Block block);
    boolean isDestructible(Block block);

    boolean registerForUndo(UndoList blocks);
    boolean prepareForUndo(UndoList blocks);

    @Nullable
    Inventory getInventory();
    int removeItem(ItemStack item);
    int removeItem(ItemStack item, boolean allowVariants);
    boolean hasItem(ItemStack item);
    boolean hasItem(ItemStack item, boolean allowVariants);
    boolean consumeBlock(MaterialAndData block, boolean allowVariants);
    void refundBlock(MaterialAndData block);
    int getItemCount(ItemStack item, boolean allowDamaged);
    int getItemCount(ItemStack item);
    @Nullable
    ItemStack getItem(int slot);
    boolean setItem(int slot, ItemStack item);

    MaterialBrush getBrush();

    void removeExperience(int xp);
    int getExperience();
    void giveExperience(int xp);

    void removeMana(float mana);
    float getMana();
    int getManaMax();
    int getEffectiveManaMax();
    void setMana(float mana);
    int getManaRegeneration();
    int getEffectiveManaRegeneration();
    void updateMana();

    int getLevel();
    void setLevel(int level);

    boolean addBatch(Batch batch);
    void addUndoBatch(UndoBatch batch);

    void registerEvent(SpellEventType type, Listener spell);
    void unregisterEvent(SpellEventType type, Listener spell);

    UndoQueue getUndoQueue();
    int finishPendingUndo();
    List<LostWand> getLostWands();
    Location getLastDeathLocation();

    boolean hasStoredInventory();
    boolean addToStoredInventory(ItemStack itemStack);

    Set<Spell> getActiveSpells();
    void enableFallProtection(int ms);
    void enableFallProtection(int ms, @Nullable Spell protector);
    void enableFallProtection(int ms, int count, @Nullable Spell protector);
    void clearFallProtection();

    void enableSuperProtection(int ms);
    void clearSuperProtection();

    boolean load(MageData data);
    boolean save(MageData data);
    void deactivate();
    boolean isValid();
    boolean hasPending();
    boolean restoreWand();
    @Nullable
    UndoList getLastUndoList();
    boolean isStealth();
    boolean isSneaking();
    boolean isJumping();
    void removed();

    void tick();

    void activateGUI(GUIAction action, Inventory inventory);
    void continueGUI(GUIAction action, Inventory inventory);
    void deactivateGUI();
    void playSoundEffect(SoundEffect sound);

    @Deprecated
    void showHoloText(Location location, String text, int duration);
    int getDebugLevel();
    void setDebugLevel(int level);
    void setDebugger(CommandSender debugger);
    void debugPermissions(CommandSender sender, Spell spell);
    CommandSender getDebugger();
    void giveItem(ItemStack item);
    void giveItem(ItemStack item, boolean putInHand);
    boolean giveItem(ItemStack item, boolean putInHand, boolean allowDropping);
    void setArmorItem(int armorSlot, ItemStack item);

    /**
     * Will not drop the item on the ground, unlike giveItem
     */
    boolean tryGiveItem(ItemStack item);
    boolean tryGiveItem(ItemStack item, boolean putInHand);
    void removeItemsWithTag(String tag);
    void setQuiet(boolean quiet);
    boolean isQuiet();
    int getSkillPoints();
    void addSkillPoints(int delta);
    void setSkillPoints(int sp);
    boolean isAtMaxSkillPoints();

    double getCurrency(String type);
    void addCurrency(String type, double delta);
    void addCurrency(String type, double delta, boolean quiet);
    void removeCurrency(String type, double delta);
    void removeCurrency(String type, double delta, boolean quiet);
    void setCurrency(String type, double amount);
    boolean isAtMaxCurrency(String type);

    @Nullable
    WandUpgradePath getBoundWandPath(String templateKey);
    boolean unbind(String template);
    void unbind(Wand wand);
    void unbindAll();
    void undoScheduled();
    void undoScheduled(String spellKey);
    EntityData getEntityData();
    boolean tryToOwn(Wand wand);
    boolean isReflected(double angle);
    boolean isBlocked(double angle);
    int getLastHeldMapId();
    @Nonnull
    Collection<MageClass> getClasses();
    @Nonnull
    Collection<String> getClassKeys();
    @Nullable
    MageClass getActiveClass();
    @Nullable
    MageClass unlockClass(@Nonnull String key);
    boolean lockClass(@Nonnull String key);
    @Nullable
    MageClass getClass(@Nonnull String key);
    boolean hasClassUnlocked(@Nonnull String key);
    void damagedBy(@Nonnull Entity enity, double damage);
    @Nullable
    Collection<Entity> getDamagers();
    @Nullable
    Entity getLastDamager();
    @Nullable
    Entity getTopDamager();
    @Nullable
    Entity getLastDamageTarget();
    @Nullable
    Long getLastTrigger(String trigger);
    void setTarget(Entity entity);

    /**
     * This will return properties for the mage's active wand, if holding one, or if not then the active class.
     *
     * <p>If the mage has no active class, then mage properties are returned.
     *
     * @return The caster
     */
    @Nonnull
    CasterProperties getActiveProperties();

    // Vault integration
    double getVaultBalance();
    boolean addVaultCurrency(double delta);
    boolean removeVaultCurrency(double delta);

    boolean hasTag(String tag);
    void addTag(String tag);

    /**
     * Sets the active class.
     *
     * @param key The key of the class to set, may be null to clear it.
     * @return True iff this was a valid class key.
     */
    boolean setActiveClass(@Nullable String key);
    boolean removeClass(@Nonnull String key);
    @Nonnull
    CasterProperties getProperties();

    /**
     * This method returns a positive number if the player is moving
     * forward while in a vehicle, negative if moving backward.
     */
    double getVehicleMovementDirection();

    /**
     * This method returns a positive number if the player is strafing
     * right while in a vehicle, negative if strafing left.
     */
    double getVehicleStrafeDirection();

    /**
     * Check to see if a player is trying to jump while in a vehicle
     * @return true if the player is pressing the spacebar while in a vehicle
     */
    boolean isVehicleJumping();

    /**
     * Return the current movement vector of this entity, in blocks per second.
     *
     * <p>This is a computed value, and is not the same as getEntity().getVelocity.
     */
    Vector getVelocity();

    void setVanished(boolean vanished);
    boolean isVanished();

    void setGlidingAllowed(boolean allow);
    boolean isGlidingAllowed();

    /**
     * This method is deprecated, soul wand functionality was never implemented.
     *
     * <p>Get the Mage's "soul" wand. This represents all of the
     * spells and other properties that are bound to the Mage itself,
     * rather than to a specific wand.
     *
     * <p>This wand never appears as an in-game item.
     */
    @Nullable
    @Deprecated
    Wand getSoulWand();

    boolean isAutomaton();

    @Nullable
    Double getAttribute(String attributeKey);

    /**
     * Notify this Mage that it has had some attributes updated.
     *
     * <p>This will force a rebuild of spell parameters that may appear in lore,
     * so that the lore will correctly reflect the new attribute values.
     *
     * <p>This is called automatically if using setAttribute, and so should only be called if
     * updating attributes via an external AttributeProvider.
     */
    void attributesUpdated();

    /**
     * Set the damage type that was last given to a Mage.
     *
     * <p>Set this prior to damaging a mage for custom weakness/protection to work properly.
     *
     * @param damageType A damage type as defined in config.yml
     */
    void setLastDamageType(String damageType);

    /**
     * Get the last damage type done to this Mage.
     *
     * <p>This can be a custom damage type, or the name of a vanilla type.
     *
     * @return the last damage type this Mage received.
     */
    @Nullable
    String getLastDamageType();

    void setLastDamageDealtType(String damageType);

    @Nullable
    String getLastDamageDealtType();

    double getLastDamage();
    double getLastDamageDealt();
    double getLastBowPull();
    EntityType getLastProjectileType();

    @Nonnull
    MageContext getContext();

    long getCreatedTime();

    boolean trigger(String trigger);

    double getEarnMultiplier(String currency);
    /**
     * @deprecated Specify a currency, use "sp" for spell points
     */
    @Deprecated
    float getEarnMultiplier();

    /**
     * @deprecated Use getEarnMultiplier instead.
     */
    @Deprecated
    float getSPMultiplier();

    /**
     * This will remove any entity attributes added by unlocked classes.
     * Primarily used on shutdown, in case someone removes the plugin.
     */
    void deactivateClasses();

    /**
     * Restores any attributes managed by unlocked classes.
     * Used on login to restore attributes.
     */
    void activateClasses();

    void deactivateModifiers();
    void activateModifiers();

    /**
     * Set this when launching vanilla projectiles, if you don't want the launch intercepted
     * by the bow/wand handler.
     *
     * <p>Always make sure to unset it.
     */
    void setLaunchingProjectile(boolean launching);

    boolean isLaunchingProjectile();

    double getHealth();
    double getMaxHealth();
    boolean toggleSpellEnabled(String spellKey);
    @Nonnull
    ConfigurationSection getVariables();
    void updatePassiveEffects();
    boolean addModifier(@Nonnull String key);
    boolean addModifier(@Nonnull String key, @Nullable ConfigurationSection properties);
    boolean addModifier(@Nonnull String key, int duration);
    boolean addModifier(@Nonnull String key, int duration, @Nullable ConfigurationSection properties);
    MageModifier removeModifier(@Nonnull String key);
    boolean hasModifier(@Nonnull String key);
    @Nonnull
    Set<String> getModifierKeys();
    @Nullable
    MageModifier getModifier(String key);
    boolean isBypassEnabled();
    void setBypassEnabled(boolean enable);
    @Nullable
    List<CastParameter> getOverrides(String spellKey);
    boolean isResourcePackEnabled();
    @Nullable
    String getPreferredResourcePack();
    boolean isUrlIconsEnabled();
    boolean canUse(ItemStack item);
    boolean canCraft(String recipeKey);

    String parameterize(String text);
    @Deprecated
    String parameterize(String command, String prefix);
    @Deprecated
    String parameterizeMessage(String command);
    ClientPlatform getClientPlatform();
    @Nullable
    CasterProperties getCasterProperties(String propertyType);
    boolean allowContainerCopy();
}
