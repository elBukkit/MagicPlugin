package com.elmakers.mine.bukkit.world.generator;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.StringUtils;

public class DistanceWeighted<T> {
    private final T value;
    private final int minDistanceSquared;
    private final int maxDistanceSquared;
    private final double weight;

    public static <T> DistanceWeighted<T> fromConfig(T value, ConfigurationSection config) {
        return new DistanceWeighted<>(value, config);
    }

    public static <T> DistanceWeighted<T> fromString(Logger logger, T value, String stringConfig) {
        return new DistanceWeighted<>(logger, value, stringConfig);
    }

    private DistanceWeighted(T value, ConfigurationSection config) {
        this.value = value;
        int minDistance = config.getInt("min_distance");
        minDistanceSquared = minDistance * minDistance;
        int maxDistance = config.getInt("max_distance");
        maxDistanceSquared = maxDistance * maxDistance;
        weight = config.getDouble("weight");
    }

    private DistanceWeighted(Logger logger, T value, String weightConfig) {
        this.value = value;
        String[] pieces = StringUtils.split(weightConfig, ",");
        double parsedWeight = 0;
        int minDistance = 0;
        int maxDistance = 0;
        try {
            parsedWeight = Double.parseDouble(pieces[0]);
            if (pieces.length == 3) {
                minDistance = Integer.parseInt(pieces[1]);
                maxDistance = Integer.parseInt(pieces[2]);
            } else if (pieces.length == 2) {
                maxDistance = Integer.parseInt(pieces[1]);
            }
        } catch (Exception ex) {
            logger.warning("Invalid distance weighted config: " + weightConfig);
        }
        weight = parsedWeight;
        minDistanceSquared = minDistance * minDistance;
        maxDistanceSquared = maxDistance * maxDistance;
    }

    public double getWeight(long x, long z) {
        if (minDistanceSquared >= maxDistanceSquared) {
            return weight;
        }
        final long distanceSquared = x * x + z * z;
        if (distanceSquared < minDistanceSquared) {
            return 0;
        }
        return weight * Math.min(1.0, ((double)distanceSquared - minDistanceSquared) / (maxDistanceSquared - minDistanceSquared));
    }

    public T getValue() {
        return value;
    }
}
