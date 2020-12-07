package com.elmakers.mine.bukkit.world.populator;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;

import com.elmakers.mine.bukkit.magic.MagicController;

public abstract class MagicChunkPopulator extends BlockPopulator {
    protected MagicController controller;

    protected void initialize(MagicController controller) {
        this.controller = controller;
    }

    public boolean load(ConfigurationSection config, MagicController controller) {
        initialize(controller);
        return onLoad(config);
    }

    public abstract boolean onLoad(ConfigurationSection config);
}
