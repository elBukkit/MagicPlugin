package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.effect.SoundEffect;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.batch.UndoBatch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.block.UndoQueue;

import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;

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
    public Collection<Batch> getPendingBatches();

    /**
     * Get the name of this mage. This may be the Player's display name, or
     * the name of a command block (for Automata)
     *
     * @return String the display name of this mage
     */
    public String getName();
    public String getDisplayName();

    /**
     * Get the unique id of this mage. May be a UUID for a player, or
     * a command block name for Automata.
     *
     * @return String the unique id of this Mage
     */
    public String getId();

    /**
     * Get the location of this Mage. This may be a Player location,
     * a Command block location, or null for console casting.
     *
     * @return Location the location of this Mage.
     */
    public Location getLocation();

    /**
     * Get the eye location of this Mage. May be the Player eye
     * location, or the Location of the Mage plus 1.5 y.
     *
     * @return Location the location of the Mage's eyes, used for targeting.
     */
    public Location getEyeLocation();
    public Location getWandLocation();

    /**
     * Get the direction this Mage is facing.
     *
     * @return Vector the facing direction
     */
    public Vector getDirection();

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
    public Player getPlayer();

    /**
     * Get the Entity instance backed by this Mage.
     *
     * This may be a Player or other Entity, or null in the case of a
     * CommandSender-based Mage, like an Automaton.
     *
     * @return The Entity represented by this Mage
     */
    public Entity getEntity();

    /**
     * Get the LivingEntity instance backed by this Mage.
     *
     * This is basically a helper wrapper for getEntity that does a typecheck
     * for you.
     *
     * @return The LivingEntity represented by this Mage
     */
    public LivingEntity getLivingEntity();

    /**
     * Get the CommandSender backed by this Mage.
     *
     * This should generally always be non-null, and can be used to send a message
     * to the Mage. This may show in the server console, Player chat, or get
     * eaten by a Command block.
     *
     * @return CommandSender The sender driving this Mage.
     */
    public CommandSender getCommandSender();

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
    public void sendMessage(String message);
    public void sendDebugMessage(String message);
    public void sendDebugMessage(String message, int level);

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
    public void castMessage(String message);

    /**
     * Cancel any pending construction batches.
     *
     * @return The batch that was cancelled, or null if nothing was pending.
     */
    public Batch cancelPending();
    public Batch cancelPending(String spellKey);
    public Batch cancelPending(boolean force);

    /**
     * Undo the last construction performed by this Mage.
     *
     * This will restore anything changed by the last-cast
     * construction spell, and remove that construction from
     * the Mage's UndoQueue.
     *
     * @return The UndoList that was undone, or null if none.
     */
    public UndoList undo();

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
    public UndoList undo(Block block);

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
    public boolean commit();

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
    public Wand getActiveWand();
    public GUIAction getActiveGUI();

    /**
     * Return a Spell for this Mage, which can be used to programatically
     * cast or modify a Spell on behalf of this Mage.
     *
     * @param key The key of the Spell to retrieve.
     * @return The Spell instance for this Mage, or null if the Mage does not have access to this Spell.
     */
    public MageSpell getSpell(String key);

    /**
     * Return all of the Spell objects registered to this Mage.
     *
     * This will generally be any Spells the Mage has ever cast.
     *
     * @return A Collection of Spell objects this Mage has cast.
     */
    public Collection<Spell> getSpells();

    /**
     * Set a Spell as "active". An "active" spell is generally a toggleable on/off
     * spell. These spells may be draining mana/xp while they are active, and
     * may self-deactivate after a specific duration, or if their resources deplete.
     *
     * @param spell The spell to activate.
     */
    public void activateSpell(Spell spell);

    /**
     * Deactivate a currently active spell. A spell may call this to deactivate
     * itself.
     *
     * If the given spell is not currently active, nothing will happen.
     *
     * @param spell The spell to deactivate
     */
    public void deactivateSpell(Spell spell);

    /**
     * Deactivate all active spells for this Mage.
     */
    public void deactivateAllSpells();
    public void deactivateAllSpells(boolean force, boolean quiet);

    public ConfigurationSection getData();

    public boolean isCooldownFree();
    public float getCooldownReduction();
    public boolean isCostFree();
    public float getCostReduction();

    public boolean isSuperPowered();
    public boolean isSuperProtected();

    public float getRangeMultiplier();
    public float getDamageMultiplier();
    public float getRadiusMultiplier();
    public float getConstructionMultiplier();

    public Color getEffectColor();
    public String getEffectParticleName();
    public float getPower();

    public boolean isPlayer();
    public boolean isOnline();
    public boolean isDead();
    public boolean isLoading();
    public boolean hasLocation();

    public void setLocation(Location location);

    /**
     * This should be called by a Spell upon
     * completion, to notify the Mage that it cast a spell.
     *
     * @param spell The Spell that was cast
     * @param result The result of the cast.
     */
    public void onCast(Spell spell, SpellResult result);

    public boolean isRestricted(Material material);
    public Set<Material> getRestrictedMaterials();

    public MageController getController();
    public boolean hasCastPermission(Spell spell);
    public boolean hasBuildPermission(Block block);
    public boolean hasBreakPermission(Block block);
    public boolean isPVPAllowed(Location location);
    public boolean isIndestructible(Block block);
    public boolean isDestructible(Block block);

    public boolean registerForUndo(UndoList blocks);
    public boolean prepareForUndo(UndoList blocks);

    public Inventory getInventory();

    public MaterialBrush getBrush();

    public void removeExperience(int xp);
    public int getExperience();
    public void giveExperience(int xp);

    public void removeMana(float mana);
    public float getMana();

    public int getLevel();
    public void setLevel(int level);

    public boolean addBatch(Batch batch);
    public void addUndoBatch(UndoBatch batch);

    public void registerEvent(SpellEventType type, Listener spell);
    public void unregisterEvent(SpellEventType type, Listener spell);

    public UndoQueue getUndoQueue();
    public List<LostWand> getLostWands();
    public Location getLastDeathLocation();

    public boolean hasStoredInventory();

    public Set<Spell> getActiveSpells();
    public void enableFallProtection(int ms);
    public void enableFallProtection(int ms, Spell protector);
    public void enableFallProtection(int ms, int count, Spell protector);

    public boolean save(ConfigurationSection configuration);
    public void activateWand();
    public void deactivate();
    public boolean isValid();
    public boolean restoreWand();
    public UndoList getLastUndoList();
    public boolean isStealth();
    public boolean isSneaking();

    public void activateGUI(GUIAction action, Inventory inventory);
    public void deactivateGUI();
    public void playSoundEffect(SoundEffect sound);

    public void showHoloText(Location location, String text, int duration);
    public int getDebugLevel();
    public void setDebugLevel(int level);
    public void giveItem(ItemStack item);
    public void removeItemsWithTag(String tag);
    public void setTrackCasts(boolean track);
    public boolean getTrackCasts();
    public void setQuiet(boolean quiet);
    public boolean isQuiet();
}
