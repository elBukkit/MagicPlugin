package com.elmakers.mine.bukkit.api.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

public class Requirement {
    public static final String DEFAULT_TYPE = "magic";
    private final @Nonnull String type;
    private final @Nonnull ConfigurationSection configuration;

    public Requirement(@Nonnull ConfigurationSection configuration) {
        this.configuration = configuration;
        this.type = configuration.getString("type", DEFAULT_TYPE);
    }

    public @Nonnull String getType() {
        return type;
    }

    public @Nonnull ConfigurationSection getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        List<String> pairs = new ArrayList<>();
        Set<String> keys = configuration.getKeys(true);
        for (String key : keys) {
           pairs.add(key + ":" + configuration.getString(key));
        }

        return "{" + StringUtils.join(pairs, ",") + "}";
    }
}
