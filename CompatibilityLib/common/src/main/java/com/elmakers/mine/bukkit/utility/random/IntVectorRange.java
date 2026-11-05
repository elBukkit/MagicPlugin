package com.elmakers.mine.bukkit.utility.random;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.IntVector;

public class IntVectorRange {
    private final IntegerRange x;
    private final IntegerRange y;
    private final IntegerRange z;

    private IntVectorRange(IntegerRange x, IntegerRange y, IntegerRange z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static IntVectorRange fromConfig(Logger logger, ConfigurationSection config, int defaultMin, int defaultMax, String key) {
        IntegerRange x;
        IntegerRange y;
        IntegerRange z;
        if (config.contains(key)) {
            x = IntegerRange.fromConfig(logger, config, key, defaultMin, defaultMax);
            z = x;
        } else {
            x = IntegerRange.fromConfig(logger, config, key + "_x", defaultMin, defaultMax);
            z = IntegerRange.fromConfig(logger, config, key + "_z", defaultMin, defaultMax);
        }
        // Special-case for consistency with other uses of y_offset
        if (config.contains(key + "_y")) {
            y = IntegerRange.fromConfig(logger, config, key + "_y", 0, 0);
        } else {
            y = IntegerRange.fromConfig(logger, config, "y_" + key, 0, 0);
        }

        return new IntVectorRange(x, y, z);
    }

    public IntVector getRandom(Random random) {
        return new IntVector(x.getRandom(random), y.getRandom(random), z.getRandom(random));
    }
}
