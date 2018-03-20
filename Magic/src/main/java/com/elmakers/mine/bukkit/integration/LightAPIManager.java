package com.elmakers.mine.bukkit.integration;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

import java.util.List;

public class LightAPIManager {
    public LightAPIManager(Plugin plugin) {
        plugin.getLogger().info("LightAPI found, Light action available");
    }

    public boolean updateChunks(Location location) {
        List<ChunkInfo> chunks = LightAPI.collectChunks(location);
        if (chunks == null || chunks.isEmpty()) return false;
        for (ChunkInfo chunk : chunks) {
            LightAPI.updateChunk(chunk);
        }
        return true;
    }

    public boolean createLight(Location location, int lightLevel, boolean async) {
        return LightAPI.createLight(location, lightLevel, async);
    }

    public boolean deleteLight(Location location, boolean async) {
        return LightAPI.deleteLight(location, async);
    }
}
