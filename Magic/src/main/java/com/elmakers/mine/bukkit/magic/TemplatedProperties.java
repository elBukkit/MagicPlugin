package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class TemplatedProperties extends CasterProperties {
    @Nullable
    private TemplateProperties template;

    public TemplatedProperties(MagicPropertyType type, MageController controller) {
        this(type, controller, null);
    }

    public TemplatedProperties(MagicPropertyType type, MageController controller, @Nullable TemplateProperties template) {
        super(type, controller);
        this.template = template;
    }

    @Nullable
    @Override
    public ConfigurationSection getConfigurationSection(String key) {
        ConfigurationSection own = super.getConfigurationSection(key);
        BaseMagicProperties template = getTemplate();
        ConfigurationSection fromTemplate = template == null ? null : template.getConfigurationSection(key);

        if (own == null) {
            return fromTemplate;
        }
        if (fromTemplate != null) {
            own = ConfigurationUtils.cloneConfiguration(own);
            own = ConfigurationUtils.overlayConfigurations(own, fromTemplate);
        }
        return own;
    }

    @Override
    protected void migrateProperty(String key, MagicPropertyType propertyType) {
        super.migrateProperty(key, propertyType, template);
    }

    @Override
    public boolean hasProperty(String key) {
        BaseMagicProperties storage = getStorage(key);
        if (storage != null) {
            return storage.hasOwnProperty(key);
        }
        return hasOwnProperty(key) || (template != null && template.hasProperty(key));
    }

    @Override
    @Nullable
    public Object getInheritedProperty(String key) {
        Object value = super.getProperty(key);
        if (value == null && template != null) {
            value = template.getProperty(key);
        }
        return value;
    }

    public void setTemplate(@Nonnull TemplateProperties template) {
        this.template = template;
    }

    @Nullable
    protected TemplateProperties getTemplate() {
        return template;
    }

    @Nonnull
    public String getKey() {
        return template == null ? "?" : template.getKey();
    }

    @Nullable
    @Override
    public String getName() {
        TemplateProperties template = getTemplate();
        return template == null ? "?" : template.getName();
    }

    @Override
    public boolean hasOwnProperty(String key) {
        return super.hasOwnProperty(key) || (template != null && template.hasOwnProperty(key));
    }

    @Override
    @Nonnull
    public ConfigurationSection getPropertyConfiguration(String key) {
        BaseMagicProperties storage = getStorage(key);
        if (storage != null && storage != this) {
            return storage.getPropertyConfiguration(key);
        }

        // Path properties override everything else, though we don't remove the values so
        // They are still there if path properties get removed.
        ConfigurationSection pathConfiguration = getPathPropertyConfiguration(key);
        if (pathConfiguration != null) {
            return pathConfiguration;
        }
        if (configuration.contains(key) || template == null) {
            return configuration;
        }
        return template.getConfiguration();
    }

    @Override
    public void clear() {
        super.clear();
        template = null;
    }

    @Override
    @Nullable
    public String getIcon(boolean legacy, String iconKey) {
        if (super.hasOwnProperty(iconKey)) {
            return getString(iconKey);
        }
        if (legacy) {
            return getString("legacy_" + iconKey, getString(iconKey));
        }
        return getString(iconKey);
    }

    @Override
    public boolean hasIcon(boolean legacy, String iconKey) {
        if (super.hasOwnProperty(iconKey)) {
            return true;
        }
        if (legacy) {
            return hasProperty("legacy_" + iconKey) || hasProperty(iconKey);
        }
        return hasProperty(iconKey);
    }
}
