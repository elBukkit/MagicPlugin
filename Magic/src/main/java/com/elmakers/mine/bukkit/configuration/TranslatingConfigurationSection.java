package com.elmakers.mine.bukkit.configuration;

import static org.bukkit.util.NumberConversions.toLong;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.util.NumberConversions;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class TranslatingConfigurationSection extends MemorySection {
    private static Logger logger;
    private ConfigurationSection parameterMap;

    public static void setLogger(Logger log) {
        logger = log;
    }

    protected static void warn(String message) {
        if (logger != null) {
            logger.warning(message);
        }
    }

    protected TranslatingConfigurationSection() {
        super();
    }

    public TranslatingConfigurationSection(ConfigurationSection parent, String path) {
        super(parent, path);
    }

    public TranslatingConfigurationSection(ConfigurationSection copy) {
        super(copy.getParent(), copy.getName());
    }

    public static TranslatingConfigurationSection getWrapped(ConfigurationSection wrap) {
        TranslatingConfigurationSection newSection = new TranslatingConfigurationSection(wrap);
        newSection.wrap(wrap);
        return newSection;
    }

    public void wrap(ConfigurationSection wrap) {
        Map<String, Object> data = NMSUtils.getMap(wrap);
        if (data != null) {
            NMSUtils.setMap(this, data);
        }
    }

    public TranslatingConfigurationSection newSection(String key) {
        return createSection(this, key);
    }

    protected TranslatingConfigurationSection createSection(TranslatingConfigurationSection parent, String key) {
        return new TranslatingConfigurationSection(parent, key);
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
            ConfigurationSection result = createSection(this, key);
            map.put(key, result);
            return result;
        }
        return section.createSection(key);
    }

    public Object get(String path, Object def) {
        if (parameterMap != null) {
            Object rawValue = super.get(path, def);
            if (rawValue != null && rawValue instanceof String) {
                String stringValue = (String)rawValue;
                if (stringValue.startsWith("$")) {
                    stringValue = stringValue.substring(1);
                    if (parameterMap.contains(stringValue)) {
                        return parameterMap.get(stringValue);
                    }
                }
            }
        }
        return super.get(path, def);
    }

    @Override
    public int getInt(String path, int def) {
        Object val = get(path, def);
        if (val instanceof String) {
            try {
                return Integer.parseInt((String)val);
            } catch (Exception ignore) {
            }
        }
        return (val instanceof Number) ? NumberConversions.toInt(val) : def;
    }

    @Override
    public boolean isInt(String path) {
        Object val = get(path);
        if (val instanceof String) {
            try {
                Integer.parseInt((String)val);
                return true;
            } catch (Exception ignore) {
            }
        }
        return val instanceof Integer;
    }

    @Override
    public double getDouble(String path, double def) {
        Object val = get(path, def);
        if (val instanceof String) {
            try {
                return Double.parseDouble((String)val);
            } catch (Exception ignore) {
            }
        }
        return (val instanceof Number) ? NumberConversions.toDouble(val) : def;
    }

    @Override
    public boolean isDouble(String path) {
        Object val = get(path);
        if (val instanceof String) {
            try {
                Double.parseDouble((String)val);
                return true;
            } catch (Exception ignore) {
            }
        }
        return val instanceof Double;
    }

    @Override
    public long getLong(String path, long def) {
        Object val = get(path, def);
        if (val instanceof String) {
            try {
                return Long.parseLong((String)val);
            } catch (Exception ignore) {
            }
        }
        return (val instanceof Number) ? toLong(val) : def;
    }

    @Override
    public boolean isLong(String path) {
        Object val = get(path);
        if (val instanceof String) {
            try {
                Long.parseLong((String)val);
                return true;
            } catch (Exception ignore) {
            }
        }
        return val instanceof Long;
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Object val = get(path, def);
        if (val instanceof String) {
            String s = (String)val;
            if (s.equalsIgnoreCase("true")) {
                return true;
            }
            if (s.equalsIgnoreCase("false")) {
                return false;
            }
        }
        return (val instanceof Boolean) ? (Boolean) val : def;
    }

    @Override
    public boolean isBoolean(String path) {
        Object val = get(path);
        if (val == null) {
            return false;
        }
        if (val instanceof String) {
            String s = (String)val;
            return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");
        }
        return val instanceof Boolean;
    }

    @Nullable
    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        Object val = get(path, null);
        if (val != null) {
            if (val instanceof Map) {
                return ConfigurationUtils.toConfigurationSection(this, path, (Map<?, ?>)val);
            }
            return (val instanceof ConfigurationSection) ? (ConfigurationSection) val : null;
        }

        // Default should never be a map since this only happens with lists-of-sections
        val = get(path, getDefault(path));
        return (val instanceof ConfigurationSection) ? createSection(path) : null;
    }

    @Override
    public boolean isConfigurationSection(String path) {
        Object val = get(path);
        return val instanceof ConfigurationSection || val instanceof Map;
    }

    @Override
    public void set(String key, Object value) {
        if (value instanceof Map) {
            value = ConfigurationUtils.toConfigurationSection(this, key, (Map<?,?>)value);
        }
        super.set(key, value);
    }

    @Override
    public List<String> getStringList(String path) {
        if (isString(path)) {
            return Arrays.asList(StringUtils.split(getString(path), ','));
        }
        return super.getStringList(path);
    }

    public void setParameterMap(ConfigurationSection parameterMap) {
        this.parameterMap = parameterMap;
    }
}
