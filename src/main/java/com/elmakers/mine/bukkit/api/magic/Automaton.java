package com.elmakers.mine.bukkit.api.magic;

import com.elmakers.mine.bukkit.api.block.BlockData;

/**
 * Represents an Automaton, which is generally a command block
 * coupled with a redstone block.
 * 
 * This class handles tracking these "Entities", and will toggle
 * the redstone block on chunk reload, re-enabling the Automaton.
 */
public interface Automaton extends BlockData {
    /**
     * Get the name of this Automaton. This most likely
     * matches the name of the command block and Mage
     * that control this Automaton.
     *
     * @return String name
     */
    public String getName();

    public long getCreatedTime();
}
