package com.elmakers.mine.bukkit.utility;

import org.bukkit.Chunk;
import org.bukkit.World;

public class LoadingChunk {
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;

    public LoadingChunk(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getX());
    }

    public LoadingChunk(World world, int chunkX, int chunkZ) {
        this(world.getName(), chunkX, chunkZ);
    }

    public LoadingChunk(String worldName, int chunkX, int chunkZ) {
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public int hashCode() {
        int worldHashCode = worldName.hashCode();
        return ((worldHashCode & 0xFFF) << 48)
                | ((chunkX & 0xFFFFFF) << 24)
                | (chunkX & 0xFFFFFF);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoadingChunk)) return false;
        LoadingChunk other = (LoadingChunk) o;
        ;
        return worldName.equals(other.worldName) && chunkX == other.chunkX && chunkZ == other.chunkZ;
    }

    @Override
    public String toString() {
        return worldName + ":" + chunkX + "," + chunkZ;
    }
}
