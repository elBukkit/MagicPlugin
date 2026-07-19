package com.elmakers.mine.bukkit.world;

import java.util.Random;

import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.IntVector;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

public abstract class WorldContext {
    protected final @NotNull Random random;
    protected final int chunkX;
    protected final int chunkZ;
    protected final int worldX;
    protected final int worldZ;
    protected final IntVector offset;

    public WorldContext(@NotNull Random random, int chunkX, int chunkZ, IntVector offset) {
        this.random = random;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.offset = offset;
        this.worldX = (chunkX << 4) + offset.x();
        this.worldZ = (chunkZ << 4) + offset.z();
    }

    public WorldContext(@NotNull Random random, int chunkX, int chunkZ) {
        this(random, chunkX, chunkZ, IntVector.ZERO);
    }

    public WorldContext(int chunkX, int chunkZ) {
        this(RandomUtils.getRandom(), chunkX, chunkZ);
    }

    public WorldContext() {
        this(0, 0);
    }

    public int getWorldX(int dx) {
        return worldX + dx;
    }

    public int getWorldY(int y) {
        return y + offset.y();
    }

    public int getWorldZ(int dz) {
        return worldZ + dz;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public Random getRandom() {
        return random;
    }

    public abstract long getSeed();

    public abstract int getMaxHeight();

    public abstract int getMinHeight();
}
