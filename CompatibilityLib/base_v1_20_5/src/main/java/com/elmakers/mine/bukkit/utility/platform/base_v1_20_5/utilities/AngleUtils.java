package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.utilities;

import org.bukkit.configuration.ConfigurationSection;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.elmakers.mine.bukkit.utility.StringUtils;

public class AngleUtils {

    public static Vector3f parseVector(ConfigurationSection section, String key, Vector3f defaultValue) {
        ConfigurationSection sectionConfig = section.getConfigurationSection(key);
        if (sectionConfig == null) {
            String stringConfig = section.getString(key);
            if (stringConfig != null) {
                final String[] pieces = StringUtils.split(stringConfig, ",");
                try {
                    if (pieces.length == 3) {
                        return new Vector3f(
                                Float.parseFloat(pieces[0]),
                                Float.parseFloat(pieces[1]),
                                Float.parseFloat(pieces[2])
                        );
                    } else if (pieces.length == 1) {
                        final float value = Float.parseFloat(pieces[0]);
                        return new Vector3f(value, value, value);
                    }
                } catch (Exception ex) {
                    return defaultValue;
                }
            }
        } else {
            return new Vector3f(
                    (float)sectionConfig.getDouble("x", 0),
                    (float)sectionConfig.getDouble("y", 0),
                    (float)sectionConfig.getDouble("z", 0)
            );
        }
        return defaultValue;
    }

    public static AxisAngle4f parseAngle(ConfigurationSection section, String key, AxisAngle4f defaultValue) {
        ConfigurationSection sectionConfig = section.getConfigurationSection(key);
        if (sectionConfig == null) {
            String stringConfig = section.getString(key);
            if (stringConfig != null) {
                final String[] pieces = StringUtils.split(stringConfig, ",");
                try {
                    float angle = (float)Math.toRadians(Float.parseFloat(pieces[0]));
                    if (pieces.length == 4) {
                        return new AxisAngle4f(
                                angle,
                                Float.parseFloat(pieces[1]),
                                Float.parseFloat(pieces[2]),
                                Float.parseFloat(pieces[3])
                        );
                    } else if (pieces.length == 1) {
                        return new AxisAngle4f(angle, 0, 0, 1);
                    }
                } catch (Exception ex) {
                    return defaultValue;
                }
            }
        } else {
            float angle = (float)Math.toRadians(sectionConfig.getDouble("angle", 0));
            return new AxisAngle4f(
                    angle,
                    (float)sectionConfig.getDouble("x", 0),
                    (float)sectionConfig.getDouble("y", 0),
                    (float)sectionConfig.getDouble("z", 1)
            );
        }
        return defaultValue;
    }
}
