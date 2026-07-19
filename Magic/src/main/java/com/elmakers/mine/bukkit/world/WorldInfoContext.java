package com.elmakers.mine.bukkit.world;

import java.util.Random;

import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.IntVector;

public class WorldInfoContext extends WorldContext {
    protected final @NotNull WorldInfo worldInfo;

    public WorldInfoContext(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ
    ) {
        super(random, chunkX, chunkZ);
        this.worldInfo = worldInfo;
    }

    public WorldInfoContext(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            IntVector offset
    ) {
        super(random, chunkX, chunkZ, offset);
        this.worldInfo = worldInfo;
    }

    public WorldInfoContext(@NotNull WorldInfo worldInfo) {
        super();
        this.worldInfo = worldInfo;
    }

    public long getSeed() {
        return worldInfo.getSeed();
    }

    public int getMaxHeight() {
        // Min is the lowest block you can set, but max is one above the highest, for whatever reason
        return worldInfo.getMaxHeight() - 1;
    }

    public int getMinHeight() {
        return worldInfo.getMinHeight();
    }
}
