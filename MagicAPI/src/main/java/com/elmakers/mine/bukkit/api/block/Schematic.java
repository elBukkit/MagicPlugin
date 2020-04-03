package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.entity.EntityData;

public interface Schematic {
    boolean contains(Vector v);
    @Nullable
    MaterialAndData getBlock(Vector v);
    Collection<EntityData> getEntities(Location center);
    Vector getSize();
    boolean isLoaded();
}
