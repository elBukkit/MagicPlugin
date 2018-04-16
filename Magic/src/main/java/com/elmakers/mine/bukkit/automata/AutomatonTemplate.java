package com.elmakers.mine.bukkit.automata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.magic.MagicController;

public class AutomatonTemplate {
    @Nonnull
    private final String key;
    private String name;
    private int interval;
    @Nullable
    private Spawner spawner;

    public AutomatonTemplate(@Nonnull String key) {
        this.key = key;
    }

    public AutomatonTemplate(@Nonnull String key, @Nonnull ConfigurationSection configuration) {
        this(key);
        name = configuration.getString("name");
        interval = configuration.getInt("interval", 0);

        if (configuration.contains("spawn")) {
            spawner = new Spawner(configuration.getConfigurationSection("spawn"));
        }
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public int getInterval() {
        return interval;
    }

    @Nullable
    public Entity spawn(MagicController controller, Location location) {
        return spawner == null ? null : spawner.spawn(controller, location);
    }
}