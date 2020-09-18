package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class ParentedProperties extends TemplatedProperties {
    @Nullable
    private ParentedProperties parent;

    public ParentedProperties(MagicPropertyType type, MageController controller, @Nullable TemplateProperties template) {
        super(type, controller, template);
    }

    public @Nullable ParentedProperties getParent() {
        return parent;
    }

    public ParentedProperties getRoot() {
        if (parent == null) return this;
        return parent.getRoot();
    }

    public void setParent(@Nonnull ParentedProperties parent) {
        this.parent = parent;
    }

    @Override
    @Nullable
    public Object getInheritedProperty(String key) {
        Object value = super.getInheritedProperty(key);
        if (value == null && parent != null) {
            value = parent.getInheritedProperty(key);
        }
        return value;
    }

    public ConfigurationSection getEffectiveConfiguration() {
        ConfigurationSection effectiveConfiguration = ConfigurationUtils.cloneConfiguration(getConfiguration());
        BaseMagicProperties template = getTemplate();
        if (template != null) {
            ConfigurationSection templateConfiguration = ConfigurationUtils.cloneConfiguration(template.getConfiguration());
            for (String key : templateConfiguration.getKeys(false)) {
                MagicPropertyType propertyRoute = propertyRoutes.get(key);
                if (propertyRoute != null && propertyRoute != type) {
                    templateConfiguration.set(key, null);
                }
            }
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, templateConfiguration);
        }

        ParentedProperties parent = getParent();
        if (parent != null) {
            ConfigurationSection parentConfiguration = parent.getEffectiveConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, parentConfiguration);
        }
        return effectiveConfiguration;
    }

    @Override
    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties, @Nullable Set<String> overriddenProperties) {
        super.describe(sender, ignoreProperties, overriddenProperties);
        if (overriddenProperties == null) {
            overriddenProperties = new HashSet<>();
        }
        Set<String> ownKeys = getConfiguration().getKeys(false);
        overriddenProperties.addAll(ownKeys);
        sender.sendMessage("" + ChatColor.BOLD + ChatColor.GREEN + "Template Configuration for (" + ChatColor.DARK_GREEN + getKey() + ChatColor.GREEN + "):");

        BaseMagicProperties template = getTemplate();
        Set<String> overriddenTemplateProperties = new HashSet<>(overriddenProperties);
        for (String key : template.getConfiguration().getKeys(false)) {
            MagicPropertyType propertyRoute = propertyRoutes.get(key);
            if (propertyRoute == null || propertyRoute == type) {
                overriddenProperties.add(key);
            } else {
                overriddenTemplateProperties.add(key);
            }
        }

        template.describe(sender, ignoreProperties, overriddenTemplateProperties);

        ParentedProperties parent = getParent();
        if (parent != null) {
            sender.sendMessage(ChatColor.AQUA + "Parent: " + ChatColor.GREEN + parent.getKey());
            parent.describe(sender, ignoreProperties, overriddenProperties);
        }
    }

    @Override
    public boolean tickMana() {
        ParentedProperties parent = getParent();
        if (!hasOwnMana() && parent != null) {
            return parent.tickMana();
        }

        return super.tickMana();
    }

    @Override
    public void loadProperties() {
        ParentedProperties parent = getParent();
        if (parent != null) {
            parent.loadProperties();
        }
        super.loadProperties();
        passiveEffectsUpdated();
    }

    @Override
    public void passiveEffectsUpdated() {
        if (hasOwnMana()) {
            updateMaxMana(getMage());
        }
    }

    public boolean isLocked() {
        if (super.getProperty("locked", false)) {
            return true;
        }
        ParentedProperties parent = getParent();
        if (parent != null) {
            return parent.isLocked();
        }
        return false;
    }

    public void unlock() {
        configuration.set("locked", null);
        ParentedProperties parent = getParent();
        if (parent != null) {
            parent.unlock();
        }
        onUnlocked();
    }

    protected void onUnlocked() {
    }

    public void lock() {
        configuration.set("locked", true);
        onLocked();
    }

    protected void onLocked() {
    }

    @Override
    public boolean updateMaxMana(Mage mage) {
        if (!hasOwnMana()) {
            boolean modified = false;
            ParentedProperties parent = getParent();
            if (parent != null) {
                modified = parent.updateMaxMana(mage);
                effectiveManaMax = parent.getEffectiveManaMax();
                effectiveManaRegeneration = parent.getEffectiveManaRegeneration();
            }
            return modified;
        }

        return super.updateMaxMana(mage);
    }
}
