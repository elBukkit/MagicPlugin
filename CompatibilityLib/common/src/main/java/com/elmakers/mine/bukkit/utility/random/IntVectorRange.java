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

    public static IntVectorRange fromConfig(Logger logger, ConfigurationSection config, int defaultMinX, int defaultMinZ) {
        IntegerRange x;
        IntegerRange y;
        IntegerRange z;
        if (config.contains("position")) {
            x = IntegerRange.fromConfig(logger, config, "position", defaultMinX, defaultMinZ);
            z = x;
        } else {
            x = IntegerRange.fromConfig(logger, config, "position_x", defaultMinX, defaultMinZ);
            z = IntegerRange.fromConfig(logger, config, "position_z", defaultMinX, defaultMinZ);
        }
        if (config.contains("position_y")) {
            y = IntegerRange.fromConfig(logger, config, "position_y", 0, 0);
        } else {
            y = IntegerRange.fromConfig(logger, config, "y_offset", 0, 0);
        }

        return new IntVectorRange(x, y, z);
    }

    public IntVector getRandom(Random random) {
        return new IntVector(x.getRandom(random), y.getRandom(random), z.getRandom(random));
    }
}
