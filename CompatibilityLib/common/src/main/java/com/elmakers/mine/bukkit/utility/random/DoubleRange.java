package com.elmakers.mine.bukkit.utility.random;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.StringUtils;

public class DoubleRange {
    final double min;
    final double max;

    public DoubleRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getRandom(Random random) {
        return RandomUtils.range(random, min, max);
    }

    public double lerp(double value) {
        return RandomUtils.lerp(min, max, value);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public static DoubleRange fromConfig(Logger logger, ConfigurationSection config, String key, double defaultMin, double defaultMax) {
        return fromConfig(logger, config, key, defaultMin, defaultMax, null, null);
    }


    public static DoubleRange fromConfig(Logger logger, ConfigurationSection config, String key, double defaultMin, double defaultMax, Double singleMin, Double singleMax) {
        DoubleRange value = null;
        if (config.contains(key)) {
            try {
                if (config.isConfigurationSection(key)) {
                    ConfigurationSection valueConfig = config.getConfigurationSection(key);
                    value = new DoubleRange(valueConfig.getDouble("min"), valueConfig.getDouble("max"));
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
                    double min = Double.parseDouble(pieces[0].trim());
                    double max = pieces.length > 1 ? Double.parseDouble(pieces[1].trim()) : min;
                    value = new DoubleRange(min, max);
                } else if (config.isDouble(key)) {
                    double configValue = config.getDouble(key);
                    value = new DoubleRange(singleMin == null ? configValue : singleMin, singleMax == null ? configValue : singleMax);
                }
            } catch (Exception ex) {
                logger.warning("Invalid randomized config value for " + key + ": " + config.get(key));
            }
        }
        return value != null ? value : new DoubleRange(defaultMin, defaultMax);
    }
}
