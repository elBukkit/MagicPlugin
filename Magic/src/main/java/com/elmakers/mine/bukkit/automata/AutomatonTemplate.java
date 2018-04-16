package com.elmakers.mine.bukkit.automata;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class AutomatonTemplate {
    @Nonnull
    private final MageController controller;
    @Nonnull
    private final ConfigurationSection configuration;
    @Nonnull
    private final String key;
    private String name;
    private int interval;
    @Nullable
    private Spawner spawner;

    public AutomatonTemplate(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        this.key = key;
        this.controller = controller;
        this.configuration = configuration;
        name = configuration.getString("name");
        interval = configuration.getInt("interval", 0);

        if (configuration.contains("spawn")) {
            spawner = new Spawner(controller, this, configuration.getConfigurationSection("spawn"));
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
    public List<Entity> spawn(Location location) {
        return spawner == null ? null : spawner.spawn(location);
    }

    public AutomatonTemplate getVariant(ConfigurationSection parameters) {
        ConfigurationSection mergedConfiguration = ConfigurationUtils.cloneConfiguration(configuration);
        mergedConfiguration = ConfigurationUtils.addConfigurations(mergedConfiguration, parameters);
        return new AutomatonTemplate(controller, key, mergedConfiguration);
    }
}