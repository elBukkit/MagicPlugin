package com.elmakers.mine.bukkit.api.magic;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.batch.UndoBatch;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.block.UndoQueue;
import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.effect.SoundEffect;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A Mage represents any entity that may cast spells. This can include:
 * 
 * - A Player. Mages for Players will be persisted and destroyed if a Player
 * logs out. By default a Mage will be kept around if a Player has anything in
 * their undo queue, so that an admin may Rewind their constructions after logout.
 * 
 * - A CommandBlockSender. A Command block will have a Mage if it uses /cast.
 * Each Command block will have a unique Mage for its assigned name (assign a name to 
 * a Command block using an Anvil). More than one Command block with the same name
 * (so mapping to the same Mage) may cause overlap issues with cooldowns or other
 * persistent Spell data.
 * 
 * - A CommandSender. Any other CommandSender, such as from the server console,
 * will map to a global "COMMAND" mage. This Mage has no Location, so is generally
 * limited in what it can cast unless the "p" location parameters are used, e.g.
 * 
 * cast blast pworld world px 0 py 70 pz 0
 * 
 * This will case "blast" in the center of the world "world" using the "COMMAND" Mage.
 * 
 * Some Spell implementations will absolutely require a Player (such as StashSpell),
 * and so will always fail unless cast by a player or with "castp".
 */
public interface Mage extends CostReducer {
    /**
     * Return the list of pending construction batches for this Mage
     *
     * @return Collection<BlockBatch> pending construction batches
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
    Location getOffhandWandLocation();

    /**
     * Gets the source location of spells cast by this mage, which were
     * not cast with a wand.
     * @return
     */
    Location getCastLocation();

    /**
     * Currently returns the cast location, even when not holding a wand,
     * but this behavior is subject to change and in the future this may
     * return null if the Mage is not holding a Wand.
     * @return
     */
    Location getWandLocation();

    /**
     * Get the direction this Mage is facing.
     *
     * @return Vector the facing direction
     */
    Vector getDirection();

    /**
     * Get the Player instance backed by this Mage.
     *
     * This may return null for Command block or console-based mages.
     *
     * A Spell should detect this and, if the Spell absolutely requires a
     * Player, should return SpellResult.PLAYER_REQUIRED.
     *
     * Spells should attempt to avoid requiring a player, do not use the
     * Player object to get a Location or Vector direction, generally
     * try to use the Spell or Mage methods for that. This will make
     * sure that parameter overrides and command-block usage function properly.
     *
     * A Player should only be needed if a Spell does something very Player-specific,
     * such as open an inventory to show the Player in-game, like StashSpell does.
     *
     * @return The player backed by this Mage, or null for Automaton Mages.
     */
    Player getPlayer();

    /**
     * Get the Entity instance backed by this Mage.
     *
     * This may be a Player or other Entity, or null in the case of a
     * CommandSender-based Mage, like an Automaton.
     *
     * @return The Entity represented by this Mage
     */
    Entity getEntity();

    /**
     * Get the LivingEntity instance backed by this Mage.
     *
     * This is basically a helper wrapper for getEntity that does a typecheck
     * for you.
     *
     * @return The LivingEntity represented by this Mage
     */
    LivingEntity getLivingEntity();

    /**
     * Get the CommandSender backed by this Mage.
     *
     * This should generally always be non-null, and can be used to send a message
     * to the Mage. This may show in the server console, Player chat, or get
     * eaten by a Command block.
     *
     * @return CommandSender The sender driving this Mage.
     */
    CommandSender getCommandSender();

    /**
     * Send a message to this Mage.
     *
     * This will respect the global plugin message cooldown, message
     * display settings and the Mage's active Wand "quiet" setting.
     *
     * A Wand "quiet" setting of 2 will disable these messages.
     *
     * @param message The message to send.
     */
    void sendMessage(String message);
    void sendDebugMessage(String message);
    void sendDebugMessage(String message, int level);

    /**
     * Send a message to this Mage.
     *
     * This will respect the global plugin "cast" message cooldown, message
     * display settings and the Mage's active Wand "quiet" setting.
     *
     * A Wand "quiet" setting of 1 will disable these messages.
     *
     * @param message The message to send.
     */
    void castMessage(String message);

    /**
     * Cancel any pending construction batches.
     *
     * @return The batch that was cancelled, or null if nothing was pending.
     */
    Batch cancelPending();
    Batch cancelPending(String spellKey);
    Batch cancelPending(boolean force);
    Batch cancelPending(String spellKey, boolean force);

    /**
     * Undo the last construction performed by this Mage.
     *
     * This will restore anything changed by the last-cast
     * construction spell, and remove that construction from
     * the Mage's UndoQueue.
     *
     * @return The UndoList that was undone, or null if none.
     */
    UndoList undo();

    /**
     * Undo the last construction performed by this Mage against the
     * given Block
     *
     * This will restore anything changed by the last-cast
     * construction spell by this Mage that targeted the specific Block,
     * even if it was not the most recent Spell cast by that Mage.
     *
     * @param block The block to check for modifications.
     * @return The UndoList that was undone, or null if the Mage has no constructions for the given Block.
     */
    UndoList undo(Block block);

    /**
     * Commit this Mage's UndoQueue.
     *
     * This will cause anything in the undo queue to become permanent-
     * meaning other overlapping spells won't undo this construction, even
     * if they were cast before the spells in this undo queue.
     *
     * This also clears the Mage's undo queue, which may allow them to be
     * destroyed if they are no longer active (e.g. the Player logged out).
     *
     * This has no effect on the Mage's scheduled undo batches.
     *
     * @return True if anything was commited, false if the Mage has no undo queue.
     */
    boolean commit();

    /**
     * Get the active Wand being used by this Mage.
     *
     * This will generally be the Wand represented by the ItemStack held by the
     * Player this Mage represents.
     *
     * Automata and other non-Player Mages generally do not have Wands.
     *
     * @return The Mage's active Wand.
     */
    Wand getActiveWand();
    Wand getOffhandWand();
    GUIAction getActiveGUI();

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
     * This may activate or deactivate a wand, or both.
     *
     * @return The current active wand, after checking
     */
    Wand checkWand();

    /**
     * Return a Spell for this Mage, which can be used to programatically
     * cast or modify a Spell on behalf of this Mage.
     *
     * This will create and register the spell if the Mage has never cast it, but has
     * permission to do so.
     *
     * @param key The key of the Spell to retrieve.
     * @return The Spell instance for this Mage, or null if the Mage does not have access to this Spell.
     */
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
     * This will generally be any Spells the Mage has ever cast.
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
     * If the given spell is not currently active, nothing will happen.
     *
     * @param spell The spell to deactivate
     */
    void deactivateSpell(Spell spell);

    /**
     * Deactivate all active spells for this Mage.
     */
    void deactivateAllSpells();
    void deactivateAllSpells(boolean force, boolean quiet);

    ConfigurationSection getData();

    boolean isCooldownFree();
    float getCooldownReduction();
    boolean isCostFree();
    boolean isConsumeFree();
    @Override
    float getCostReduction();
    long getRemainingCooldown();
    void setRemainingCooldown(long ms);
    void clearCooldown();

    boolean isSuperPowered();
    boolean isSuperProtected();

    float getRangeMultiplier();
    float getDamageMultiplier();
    float getRadiusMultiplier();
    float getConstructionMultiplier();

    Color getEffectColor();
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
    Set<Material> getRestrictedMaterials();

    MageController getController();
    boolean hasCastPermission(Spell spell);
    boolean hasBuildPermission(Block block);
    boolean hasBreakPermission(Block block);
    boolean isPVPAllowed(Location location);
    boolean isIndestructible(Block block);
    boolean isDestructible(Block block);

    boolean registerForUndo(UndoList blocks);
    boolean prepareForUndo(UndoList blocks);

    Inventory getInventory();
    int removeItem(ItemStack item);
    boolean hasItem(ItemStack item);
    int removeItem(ItemStack item, boolean allowVariants);
    boolean hasItem(ItemStack item, boolean allowVariants);

    MaterialBrush getBrush();

    void removeExperience(int xp);
    int getExperience();
    void giveExperience(int xp);

    void removeMana(float mana);
    float getMana();

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

    Set<Spell> getActiveSpells();
    void enableFallProtection(int ms);
    void enableFallProtection(int ms, Spell protector);
    void enableFallProtection(int ms, int count, Spell protector);

    void enableSuperProtection(int ms);
    void clearSuperProtection();

    boolean load(MageData data);
    boolean save(MageData data);
    void deactivate();
    boolean isValid();
    boolean hasPending();
    boolean restoreWand();
    UndoList getLastUndoList();
    boolean isStealth();
    boolean isSneaking();
    boolean isJumping();

    void tick();

    void activateGUI(GUIAction action, Inventory inventory);
    void continueGUI(GUIAction action, Inventory inventory);
    void deactivateGUI();
    void playSoundEffect(SoundEffect sound);

    void showHoloText(Location location, String text, int duration);
    int getDebugLevel();
    void setDebugLevel(int level);
    void setDebugger(CommandSender debugger);
    void debugPermissions(CommandSender sender, Spell spell);
    CommandSender getDebugger();
    void giveItem(ItemStack item);
    void removeItemsWithTag(String tag);
    void setQuiet(boolean quiet);
    boolean isQuiet();
    int getSkillPoints();
    void addSkillPoints(int delta);
    void setSkillPoints(int sp);
    boolean isAtMaxSkillPoints();
    WandUpgradePath getBoundWandPath(String templateKey);
    boolean unbind(String template);
    void unbind(Wand wand);
    void unbindAll();
    void undoScheduled();
    EntityData getEntityData();
    boolean tryToOwn(Wand wand);
    boolean isReflected(double angle);
    boolean isBlocked(double angle);
    int getLastHeldMapId();
    float getSPMultiplier();
    @Nullable MageClass getActiveClass();
    @Nullable MageClass unlockClass(@Nonnull String key);
    @Nullable MageClass getClass(@Nonnull String key);
    boolean setActiveClass(@Nonnull String key);
    boolean removeClass(@Nonnull String key);
    @Nonnull MagicConfigurable getProperties();

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
     * This is a computed value, and is not the same as getEntity().getVelocity.
     */
    Vector getVelocity();

    /**
     * This method is deprecated, soul wand functionality was never implemented.
     *
     * Get the Mage's "soul" wand. This represents all of the
     * spells and other properties that are bound to the Mage itself,
     * rather than to a specific wand.
     *
     * This wand never appears as an in-game item.
     */
    @Deprecated
    Wand getSoulWand();
}
