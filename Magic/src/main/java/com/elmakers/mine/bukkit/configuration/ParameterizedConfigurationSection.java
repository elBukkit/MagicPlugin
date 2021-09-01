package com.elmakers.mine.bukkit.configuration;

import static org.bukkit.util.NumberConversions.toLong;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;

public class ParameterizedConfigurationSection extends TranslatingConfigurationSection {
    protected ParameterizedConfigurationSection() {
        super();
    }

    protected ParameterizedConfigurationSection(ConfigurationSection parent, String path) {
        super(parent, path);
    }

    public ParameterizedConfigurationSection(ConfigurationSection copy) {
        super(copy.getParent(), copy.getName());
    }

    @Override
    protected TranslatingConfigurationSection createSection(TranslatingConfigurationSection parent, String key) {
        return new ParameterizedConfigurationSection(this, key);
    }

    @Override
    public int getInt(String path, int def) {
        Object val = get(path, def);
        if (val instanceof String && getRoot() instanceof ParameterizedConfiguration) {
            val = ((ParameterizedConfiguration)getRoot()).evaluate((String)val, path);
        }
        return (val instanceof Number) ? NumberConversions.toInt(val) : def;
    }

    @Override
    public double getDouble(String path, double def) {
        Object val = get(path, def);
        if (val instanceof String && getRoot() instanceof ParameterizedConfiguration) {
            val = ((ParameterizedConfiguration)getRoot()).evaluate((String)val, path);
        }
        return (val instanceof Number) ? NumberConversions.toDouble(val) : def;
    }

    @Override
    public long getLong(String path, long def) {
        Object val = get(path, def);
        if (val instanceof String && getRoot() instanceof ParameterizedConfiguration) {
            val = ((ParameterizedConfiguration)getRoot()).evaluate((String)val, path);
        }
        return (val instanceof Number) ? toLong(val) : def;
    }
}
