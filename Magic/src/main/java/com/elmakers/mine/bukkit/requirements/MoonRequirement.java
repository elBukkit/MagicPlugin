package com.elmakers.mine.bukkit.requirements;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

class MoonRequirement extends RangedRequirement {
    public MoonRequirement(String value, Logger logger) {
        try {
            if (value.startsWith("<")) {
                if (value.startsWith("<=")) {
                    max = (double)ConfigurationUtils.parseMoonPhase(value.substring(2), logger, "moon requirement");
                    inclusive = true;
                } else {
                    max = (double)ConfigurationUtils.parseMoonPhase(value.substring(1), logger, "moon requirement");
                }
            } else if (value.startsWith(">")) {
                if (value.startsWith(">=")) {
                    min = (double)ConfigurationUtils.parseMoonPhase(value.substring(2), logger, "moon requirement");
                    inclusive = true;
                } else {
                    min = (double)ConfigurationUtils.parseMoonPhase(value.substring(1), logger, "moon requirement");
                }
            } else if (value.startsWith("=")) {
                this.value = (double)ConfigurationUtils.parseMoonPhase(value.substring(1), logger, "moon requirement");
            } else {
                // Default to = which is what we normally mean for phase of moon
                this.value = (double)ConfigurationUtils.parseMoonPhase(value, logger, "moon requirement");
            }
        } catch (Exception ignore) {
        }
    }

    public MoonRequirement(ConfigurationSection configuration, Logger logger) {
        if (configuration.contains("min")) {
            min = (double)ConfigurationUtils.parseMoonPhase(configuration.getString("min"), logger, "moon requirement");
        }
        if (configuration.contains("max")) {
            max = (double)ConfigurationUtils.parseMoonPhase(configuration.getString("max"), logger, "moon requirement");
        }
        if (configuration.contains("value")) {
            value = (double)ConfigurationUtils.parseMoonPhase(configuration.getString("value"), logger, "moon requirement");
        }
        inclusive = configuration.getBoolean("inclusive");
    }
}
