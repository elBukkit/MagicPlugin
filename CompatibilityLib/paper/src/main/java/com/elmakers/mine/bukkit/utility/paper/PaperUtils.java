package com.elmakers.mine.bukkit.utility.paper;

import java.util.function.Consumer;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public class PaperUtils implements com.elmakers.mine.bukkit.utility.platform.PaperUtils {
    private final Platform platform;

    public PaperUtils(Platform platform) {
        this.platform = platform;
    }

    @Override
    public void loadChunk(World world, int x, int z, boolean generate, Consumer<Chunk> consumer) {
        world.getChunkAtAsync(x, z, generate, consumer);
    }

    @Override
    public void registerEvents(MageController controller, PluginManager pm) {
        pm.registerEvents(new PaperPlayerListener(controller), controller.getPlugin());
    }
}
