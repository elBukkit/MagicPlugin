package com.elmakers.mine.bukkit.api.block;

import org.bukkit.util.BlockVector;

public interface Automaton {
	public BlockVector getLocation();
	public String getName();
	public String getWorldName();
}
