package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.BlockBatch;
import com.elmakers.mine.bukkit.api.block.BlockList;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
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
	public Collection<BlockBatch> getPendingBatches();
	
	/**
	 * Get the name of this mage. This may be the Player's display name, or
	 * the name of a command block (for Automata)
	 * 
	 * @return String the display name of this mage
	 */
	public String getName();
	
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
	 * @return true if something was cancelled
	 */
	public boolean cancelPending();
	
	/**
	 * Undo the last construction performed by this Mage.
	 * 
	 * This will restore anything changed by the last-cast
	 * construction spell, and remove that construction from
	 * the Mage's UndoQueue.
	 * 
	 * @return True if anything was undone, false if the Mage has no undo queue.
	 */
	public boolean undo();
	
	/**
	 * Undo the last construction performed by this Mage against the
	 * given Block
	 * 
	 * This will restore anything changed by the last-cast
	 * construction spell by this Mage that targeted the specific Block,
	 * even if it was not the most recent Spell cast by that Mage.
	 * 
	 * @param block The block to check for modifications.
	 * @return True if anything was undone, false if the Mage has no constructions for the given Block.
	 */
	public boolean undo(Block block);
	
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
	
	/**
	 * Return a Spell for this Mage, which can be used to programatically
	 * cast or modify a Spell on behalf of this Mage.
	 * 
	 * @param key The key of the Spell to retrieve.
	 * @return The Spell instance for this Mage, or null if the Mage does not have access to this Spell.
	 */
	public Spell getSpell(String key);
	
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
	public float getPower();
	
	public boolean isPlayer();
	public boolean isOnline();
	public boolean isDead();
	public boolean hasLocation();
	
	public void setLocation(Location location);
	
	/**
	 * This should be called by a Spell upon
	 * completion, to notify the Mage that it cast a spell.
	 * 
	 * @param result The result of the cast.
	 */
	public void onCast(Spell spell, SpellResult result);
	
	public boolean isRestricted(Material material);
	public Set<Material> getRestrictedMaterials();
	
	public MageController getController();
	public boolean hasBuildPermission(Block block);
	public boolean isIndestructible(Block block);
	public boolean isDestructible(Block block);
	
	public boolean registerForUndo(BlockList blocks);
	
	public Inventory getInventory();
	
	public MaterialBrush getBrush();
	
	public void removeExperience(int xp);
	public int getExperience();
	
	public boolean addPendingBlockBatch(BlockBatch batch);
	
	public void registerEvent(SpellEventType type, Listener spell);
	public void unregisterEvent(SpellEventType type, Listener spell);
	
	public UndoQueue getUndoQueue();
	public List<LostWand> getLostWands();
	public Location getLastDeathLocation();
}
