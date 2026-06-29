package com.elmakers.mine.bukkit.utility.random;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.StringUtils;

public class DistanceWeighted {
    private final LongRange distanceSquared;
    private final DoubleRange weight;
    private final int frequency;

    public static DistanceWeighted fromConfig(Logger logger, ConfigurationSection config) {
        return new DistanceWeighted(logger, config);
    }

    public static DistanceWeighted fromString(Logger logger, String stringConfig) {
        return new DistanceWeighted(logger, stringConfig);
    }

    protected DistanceWeighted(Logger logger, ConfigurationSection config) {
        IntegerRange distance = IntegerRange.fromConfig(logger, config, "distance", 0, 0);
        distanceSquared = distance.squared();
        weight = DoubleRange.fromConfig(logger, config, "weight", 0, 1, 0.0, null);
        // Frequency is defined in terms of chunks
        frequency = config.getInt("frequency") * 16;
    }

    protected DistanceWeighted(Logger logger, String weightConfig) {
        String[] pieces = StringUtils.split(weightConfig, ",");
        double weight = 1;
        long minDistance = 0;
        long maxDistance = 0;
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
        distanceSquared = new LongRange(minDistance * minDistance, maxDistance * maxDistance);
        this.frequency = frequency;
        this.weight = new DoubleRange(weight, weight);
    }

    public double getWeight(long x, long z) {
        if (frequency > 0 && (x % frequency != 0 || z % frequency != 0)) {
            return 0;
        }
        final long distanceSquared = x * x + z * z;
        return weight.lerp(this.distanceSquared.getFactor(distanceSquared));
    }
}
