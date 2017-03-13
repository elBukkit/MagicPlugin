package com.elmakers.mine.bukkit.api.block;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public interface BoundingBox {
    BlockVector getMin();
    BlockVector getMax();
    BlockVector getCenter();
    boolean contains(Vector p);
    boolean contains(Vector p, int threshold);
}
