package com.elmakers.mine.bukkit.api.magic;

import org.bukkit.util.BlockVector;

/**
 * Represents an Automaton, which is generally a command block
 * coupled with a redstone block.
 * 
 * This class handles tracking these "Entities", and will toggle
 * the redstone block on chunk reload, re-enabling the Automaton.
 */
public interface Automaton {
	/**
	 * Get the location of this Automaton's redstone block
	 * power source
	 * 
	 * @return BlockVector power location
	 */
	public BlockVector getLocation();

	/**
	 * Get which World this Automaton is in, by name.
	 * 
	 * @return String the name of the world
	 */
	public String getWorldName();
	
	/**
	 * Get the name of this Automaton. This most likely
	 * matches the name of the command block and Mage
	 * that control this Automaton.
	 * 
	 * @return String name
	 */
	public String getName();
}
