package com.elmakers.mine.bukkit.configuration;

import static org.bukkit.util.NumberConversions.toLong;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.util.NumberConversions;

import com.elmakers.mine.bukkit.utility.NMSUtils;

public class ParameterizedConfigurationSection extends MemorySection {
    private static Logger logger;

    public static void setLogger(Logger log) {
        logger = log;
    }

    protected static void warn(String message) {
        if (logger != null) {
            logger.warning(message);
        }
    }

    protected ParameterizedConfigurationSection() {
        super();
    }

    protected ParameterizedConfigurationSection(ConfigurationSection parent, String path) {
        super(parent, path);
    }

    public ParameterizedConfigurationSection(ParameterizedConfigurationSection copy) {
        super(copy.getParent(), copy.getName());
    }

    public void wrap(ConfigurationSection wrap) {
        Map<String, Object> data = NMSUtils.getMap(wrap);
        if (data != null) {
            NMSUtils.setMap(this, data);
        }
    }

    /**
     * Borrowed from Bukkit's MemorySection
     */
    @Override
    public ConfigurationSection createSection(String path) {
        Validate.notEmpty(path, "Cannot create section at empty path");
        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create section without a root");
        }

        final char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1;
        int i2;
        ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            ConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            ConfigurationSection result = new ParameterizedConfigurationSection(this, key);
            map.put(key, result);
            return result;
        }
        return section.createSection(key);
    }

    @Override
    public int getInt(String path, int def) {
        Object val = get(path, def);
        if (val instanceof String && getRoot() instanceof ParameterizedConfiguration) {
            val = ((ParameterizedConfiguration)getRoot()).evaluate((String)val);
        }
        return (val instanceof Number) ? NumberConversions.toInt(val) : def;
    }

    @Override
    public double getDouble(String path, double def) {
        Object val = get(path, def);
        if (val instanceof String && getRoot() instanceof ParameterizedConfiguration) {
            val = ((ParameterizedConfiguration)getRoot()).evaluate((String)val);
        }
        return (val instanceof Number) ? NumberConversions.toDouble(val) : def;
    }

    @Override
    public long getLong(String path, long def) {
        Object val = get(path, def);
        if (val instanceof String && getRoot() instanceof ParameterizedConfiguration) {
            val = ((ParameterizedConfiguration)getRoot()).evaluate((String)val);
        }
        return (val instanceof Number) ? toLong(val) : def;
    }
}
