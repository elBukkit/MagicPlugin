package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.BlockBatch;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.Wand;

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
	 * Get the location of this Mage
	 * 
	 * @return
	 */
	public Location getLocation();
	
	/**
	 * Get the eye location of this Mage. May be the Player eye
	 * location, or the Location of the Mage plus 1.5 y.
	 * 
	 * @return
	 */
	public Location getEyeLocation();
	
	/**
	 * Get the direction this Mage is facing.
	 * 
	 * @return
	 */
	public Vector getDirection();
	public Player getPlayer();
	public CommandSender getCommandSender();
	
	public void sendMessage(String message);
	public void castMessage(String message);
	
	/**
	 * Cancel any pending construction batches.
	 * 
	 * @return true if something was cancelled
	 */
	public boolean cancelPending();
	
	public boolean undo();
	public boolean undo(Block block);
	public boolean commit();
	
	public Wand getActiveWand();
	public Spell getSpell(String key);
}
