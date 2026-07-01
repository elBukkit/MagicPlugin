package com.elmakers.mine.bukkit.utility.random;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.StringUtils;

public class IntegerRange {
    final int min;
    final int max;

    public IntegerRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getRandom(Random random) {
        return RandomUtils.range(random, min, max);
    }

    public int lerp(double value) {
        return RandomUtils.lerp(min, max, value);
    }

    public double getFactor(int value) {
        if (value < min) return 0;
        if (value > max) return 1;
        return (double)(value - min) / (double)(max - min);
    }

    public LongRange squared() {
        return new LongRange((long)min * min, (long)max * max);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int clip(int value, int minValue) {
        if (max > minValue) {
            value = Math.min(value, max);
        }
        if (min > minValue) {
            value = Math.max(minValue, value - min);
        }
        return value;
    }

    public int clip(int value) {
        return Math.max(Math.min(value, max), min);
    }

    public static IntegerRange fromOptionalConfig(Logger logger, ConfigurationSection config, String key) {
        if (!config.contains(key)) {
            return null;
        }
        return IntegerRange.fromConfig(logger, config, key, 0, 0);
    }

    public static IntegerRange fromConfig(Logger logger, ConfigurationSection config, String key, int defaultMin, int defaultMax) {
        IntegerRange value = null;
        if (config.contains(key)) {
            try {
                if (config.isConfigurationSection(key)) {
                    ConfigurationSection valueConfig = config.getConfigurationSection(key);
                    value = new IntegerRange(valueConfig.getInt("min"), valueConfig.getInt("max"));
                } else if (config.isString(key)) {
                    String stringValue = config.getString(key);
                    // Handle rand() format by stripping out before/after parenthesis
                    stringValue = stringValue.substring(stringValue.indexOf('(') + 1);
                    if (stringValue.contains(")")) {
                        stringValue = stringValue.substring(0, stringValue.indexOf(')'));
                    }
                    // Big note here! There is some weird yaml syntax for things like "1:22" which will parse
                    // So we should not use : as a delimiter.
                    char separator = (stringValue.contains(",") ? ',' : '|');
                    String[] pieces = StringUtils.split(stringValue, separator);
                    int min = Integer.parseInt(pieces[0].trim());
                    int max = pieces.length > 1 ? Integer.parseInt(pieces[1].trim()) : min;
                    value = new IntegerRange(min, max);
                } else if (config.isInt(key)) {
                    value = new IntegerRange(config.getInt(key), config.getInt(key));
                }
            } catch (Exception ex) {
                logger.warning("Invalid randomized config value for " + key + ": " + config.get(key));
            }
        }
        return value != null ? value : new IntegerRange(defaultMin, defaultMax);
    }
}
