package com.elmakers.mine.bukkit.configuration;

import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;

public class TranslatingConfiguration extends TranslatingConfigurationSection implements Configuration {
    private static class Options extends ConfigurationOptions {
        protected Options(Configuration configuration) {
            super(configuration);
        }
    }

    private Options options;

    public TranslatingConfiguration() {
        super();
    }

    @Nullable
    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    @Override
    public void addDefaults(Map<String, Object> map) {
    }

    @Override
    public void addDefaults(Configuration configuration) {
    }

    @Override
    public void setDefaults(Configuration configuration) {
    }

    @Nullable
    @Override
    public Configuration getDefaults() {
        return null;
    }

    @Override
    public ConfigurationOptions options() {
        if (options == null) {
            options = new Options(this);
        }

        return options;
    }
}
