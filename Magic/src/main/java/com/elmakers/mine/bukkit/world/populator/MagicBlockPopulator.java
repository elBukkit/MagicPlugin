package com.elmakers.mine.bukkit.world.populator;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.world.MagicWorld;

public abstract class MagicBlockPopulator extends BlockPopulator {
    protected MagicWorld world;

    public boolean load(ConfigurationSection config, MagicWorld world) {
        this.world = world;
        return onLoad(config);
    }

    public abstract boolean onLoad(ConfigurationSection config);

    protected void logBlockRule(String message) {
        getController().info(message);
    }

    public MagicController getController() {
        return world.getController();
    }
}
