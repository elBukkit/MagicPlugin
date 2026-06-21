package com.elmakers.mine.bukkit.utility.random;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.StringUtils;

public class LongRange {
    final long min;
    final long max;

    public LongRange(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public long getRandom(Random random) {
        return RandomUtils.range(random, min, max);
    }

    public long lerp(double value) {
        return RandomUtils.lerp(min, max, value);
    }

    public double getFactor(long value) {
        if (value < min) return 0;
        if (value >= max) return 1;
        return (double)(value - min) / (double)(max - min);
    }

    public LongRange squared() {
        return new LongRange(min * min, max * max);
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public long clip(long value, long minValue) {
        if (max > minValue) {
            value = Math.min(value, max);
        }
        if (min > minValue) {
            value = Math.max(minValue, value - min);
        }
        return value;
    }

    public long clip(long value) {
        return Math.max(Math.min(value, max), min);
    }

    public static LongRange fromOptionalConfig(Logger logger, ConfigurationSection config, String key) {
        if (!config.contains(key)) {
            return null;
        }
        return LongRange.fromConfig(logger, config, key, 0, 0);
    }

    public static LongRange fromConfig(Logger logger, ConfigurationSection config, String key, int defaultMin, int defaultMax) {
        LongRange value = null;
        if (config.contains(key)) {
            try {
                if (config.isConfigurationSection(key)) {
                    ConfigurationSection valueConfig = config.getConfigurationSection(key);
                    value = new LongRange(valueConfig.getLong("min"), valueConfig.getLong("max"));
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
                    long min = Long.parseLong(pieces[0].trim());
                    long max = pieces.length > 1 ? Long.parseLong(pieces[1].trim()) : min;
                    value = new LongRange(min, max);
                } else if (config.isLong(key) || config.isInt(key)) {
                    value = new LongRange(config.getInt(key), config.getInt(key));
                }
            } catch (Exception ex) {
                logger.warning("Invalid randomized config value for " + key + ": " + config.get(key));
            }
        }
        return value != null ? value : new LongRange(defaultMin, defaultMax);
    }
}
