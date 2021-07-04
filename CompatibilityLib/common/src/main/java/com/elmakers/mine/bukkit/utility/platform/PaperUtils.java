package com.elmakers.mine.bukkit.utility.platform;

import java.util.function.Consumer;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;

public interface PaperUtils {
    void registerEvents(MageController controller, PluginManager pm);
    void loadChunk(World world, int x, int z, boolean generate, Consumer<Chunk> consumer);
}
