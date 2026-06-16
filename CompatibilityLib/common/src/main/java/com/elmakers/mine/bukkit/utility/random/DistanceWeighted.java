package com.elmakers.mine.bukkit.utility.random;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.StringUtils;

public class DistanceWeighted<T> {
    private final T value;
    private final int minDistanceSquared;
    private final int maxDistanceSquared;
    private final double weight;
    private final int frequency;

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
        weight = config.getDouble("weight", 1);
        // Frequency is defined in terms of chunks
        frequency = config.getInt("frequency") * 16;
    }

    private DistanceWeighted(Logger logger, T value, String weightConfig) {
        this.value = value;
        String[] pieces = StringUtils.split(weightConfig, ",");
        double weight = 1;
        int minDistance = 0;
        int maxDistance = 0;
        int frequency = 0;
        try {
            weight = Double.parseDouble(pieces[0]);
            if (pieces.length >= 3) {
                minDistance = Integer.parseInt(pieces[1]);
                maxDistance = Integer.parseInt(pieces[2]);
                if (pieces.length > 3) {
                    frequency = Integer.parseInt(pieces[3]);
                }
            } else if (pieces.length == 2) {
                maxDistance = Integer.parseInt(pieces[1]);
            }
        } catch (Exception ex) {
            logger.warning("Invalid distance weighted config: " + weightConfig);
        }
        minDistanceSquared = minDistance * minDistance;
        maxDistanceSquared = maxDistance * maxDistance;
        this.frequency = frequency;
        this.weight = weight;
    }

    public double getWeight(long x, long z) {
        if (frequency > 0 && (x % frequency != 0 || z % frequency != 0)) {
            return 0;
        }
        final long distanceSquared = x * x + z * z;
        if (distanceSquared >= maxDistanceSquared) {
            return weight;
        }
        if (distanceSquared < minDistanceSquared) {
            return 0;
        }
        return weight * Math.min(1.0, ((double)distanceSquared - minDistanceSquared) / (maxDistanceSquared - minDistanceSquared));
    }

    public T getValue() {
        return value;
    }
}
