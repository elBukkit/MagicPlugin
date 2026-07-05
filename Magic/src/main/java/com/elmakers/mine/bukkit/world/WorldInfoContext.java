package com.elmakers.mine.bukkit.world;

import java.util.Random;

import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

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

    public WorldInfoContext(@NotNull WorldInfo worldInfo) {
        super();
        this.worldInfo = worldInfo;
    }

    public long getSeed() {
        return worldInfo.getSeed();
    }

    public int getMaxHeight() {
        return worldInfo.getMaxHeight();
    }

    public int getMinHeight() {
        return worldInfo.getMinHeight();
    }
}
