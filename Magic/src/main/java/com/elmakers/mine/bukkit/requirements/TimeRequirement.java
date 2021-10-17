package com.elmakers.mine.bukkit.requirements;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

class TimeRequirement extends RangedRequirement {
    public TimeRequirement(String value, Logger logger) {
        try {
            if (value.startsWith("<")) {
                if (value.startsWith("<=")) {
                    max = (double)ConfigurationUtils.parseTime(value.substring(2), logger, "time requirement");
                    inclusive = true;
                } else {
                    max = (double)ConfigurationUtils.parseTime(value.substring(1), logger, "time requirement");
                }
            } else if (value.startsWith(">")) {
                if (value.startsWith(">=")) {
                    min = (double)ConfigurationUtils.parseTime(value.substring(2), logger, "time requirement");
                    inclusive = true;
                } else {
                    min = (double)ConfigurationUtils.parseTime(value.substring(1), logger, "time requirement");
                }
            } else if (value.startsWith("=")) {
                this.value = (double)ConfigurationUtils.parseTime(value.substring(1), logger, "time requirement");
            } else {
                // Default to >= which is what we normally mean
                min = (double)ConfigurationUtils.parseTime(value, logger, "time requirement");
                this.inclusive = true;
            }
        } catch (Exception ignore) {
        }
    }

    public TimeRequirement(ConfigurationSection configuration, Logger logger) {
        if (configuration.contains("min")) {
            min = (double)ConfigurationUtils.parseTime(configuration.getString("min"), logger, "time requirement");
        }
        if (configuration.contains("max")) {
            max = (double)ConfigurationUtils.parseTime(configuration.getString("max"), logger, "time requirement");
        }
        if (configuration.contains("value")) {
            value = (double)ConfigurationUtils.parseTime(configuration.getString("value"), logger, "time requirement");
        }
        inclusive = configuration.getBoolean("inclusive");
    }
}
