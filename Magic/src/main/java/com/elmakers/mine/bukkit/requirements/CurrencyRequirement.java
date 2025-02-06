package com.elmakers.mine.bukkit.requirements;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;

class CurrencyRequirement extends RangedRequirement {
    public final String currencyKey;

    private CurrencyRequirement(ConfigurationSection configuration, String defaultType) {
        super(configuration);
        currencyKey = configuration.getString("currency", defaultType);
    }

    private CurrencyRequirement(String[] configValues, String defaultType) {
        super(configValues[configValues.length - 1]);
        currencyKey = configValues.length > 1 ? configValues[0] : defaultType;

    }

    private CurrencyRequirement(String configValue, String defaultType) {
        this(StringUtils.split(configValue, " "), defaultType);
    }

    @Nullable
    public static CurrencyRequirement parse(ConfigurationSection configuration, String key, String defaultType) {
        ConfigurationSection rangedConfig = ConfigurationUtils.getConfigurationSection(configuration, key);
        if (rangedConfig != null) {
            return new CurrencyRequirement(rangedConfig, defaultType);
        }
        if (configuration.contains(key)) {
            return new CurrencyRequirement(configuration.getString(key), defaultType);
        }
        return null;
    }

    public boolean check(Mage mage) {
        return check(mage.getCurrency(currencyKey));
    }

    public String getKey() {
        return currencyKey;
    }

    @Override
    public String toString() {
        return "[Currency " + currencyKey + "=" + value + " from (" + min + " to " + max + ")]";
    }
}
