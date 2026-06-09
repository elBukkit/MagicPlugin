package com.elmakers.mine.bukkit.world.populator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.world.MagicWorld;

public class MagicChunkHandler {

    private final MagicWorld world;
    private List<MagicBlockPopulator> chunkPopulators = new ArrayList<>();

    public MagicChunkHandler(MagicWorld world) {
        this.world = world;
    }

    public void load(MagicWorld world, ConfigurationSection config) {
        chunkPopulators = MagicBlockPopulator.loadPopulators(world, config);
    }

    public Collection<MagicBlockPopulator> getPopulators() {
        return chunkPopulators;
    }

    public boolean isEmpty() {
        return chunkPopulators.size() == 0;
    }

    public MagicController getController() {
        return world.getController();
    }
}
