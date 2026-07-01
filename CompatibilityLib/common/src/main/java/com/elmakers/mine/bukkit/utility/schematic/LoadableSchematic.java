package com.elmakers.mine.bukkit.utility.schematic;

import java.util.Collection;
import java.util.Map;

import org.bukkit.util.Vector;

public interface LoadableSchematic {
    void load(int width, int height, int length, int[] blockTypes, byte[] data, Map<Integer, String> palette, Collection<Object> tileEntityData, Collection<Object> entityData, Vector origin);
}
