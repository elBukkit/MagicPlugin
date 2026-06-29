package com.elmakers.mine.bukkit.utility.random;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

public class DistanceWeightedValue<T> extends DistanceWeighted {
    private final T value;

    public static <T> DistanceWeightedValue<T> fromConfig(Logger logger, T value, ConfigurationSection config) {
        return new DistanceWeightedValue<>(logger, value, config);
    }

    public static <T> DistanceWeightedValue<T> fromString(Logger logger, T value, String stringConfig) {
        return new DistanceWeightedValue<>(logger, value, stringConfig);
    }

    protected DistanceWeightedValue(Logger logger, T value, ConfigurationSection config) {
        super(logger, config);
        this.value = value;
    }

    protected DistanceWeightedValue(Logger logger, T value, String weightConfig) {
        super(logger, weightConfig);
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
