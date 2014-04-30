package com.elmakers.mine.bukkit.api.block;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public interface BoundingBox {
	public BlockVector getMin();
	public BlockVector getMax();
	public BlockVector getCenter();
	public boolean contains(Vector p);
	public boolean contains(Vector p, int threshold);
}
