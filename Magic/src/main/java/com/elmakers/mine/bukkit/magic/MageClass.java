package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class MageClass extends CasterProperties implements com.elmakers.mine.bukkit.api.magic.MageClass  {
    protected final MageClassTemplate template;
    protected final MageProperties mage;
    private MageClass parent;

    public MageClass(@Nonnull Mage mage, @Nonnull MageClassTemplate template) {
        super(template.hasParent() ? MagicPropertyType.SUBCLASS : MagicPropertyType.CLASS, mage.getController());
        this.template = template;
        this.mage = mage.getProperties();
    }

    @Override
    public boolean hasProperty(String key) {
        return hasOwnProperty(key) || template.hasProperty(key) || mage.hasProperty(key) || (parent != null && parent.hasProperty(key));
    }

    @Override
    public Object getProperty(String key) {
        Object value = super.getProperty(key);
        if (value == null) {
            value = template.getProperty(key);
        }
        if (value == null && parent != null) {
            value = parent.getProperty(key);
        }
        // Don't need to check the mage if we have a parent, since the parent
        // checks for us.
        if (value == null && parent == null) {
            value = mage.getProperty(key);
        }
        return value;
    }

    public @Nonnull MageClassTemplate getTemplate() {
        return template;
    }

    public @Nullable MageClass getParent() {
        return parent;
    }

    public MageClass getRoot() {
        if (parent == null) return this;
        return parent.getRoot();
    }

    public void setParent(@Nonnull MageClass parent) {
        this.parent = parent;
    }

    @Override
    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties) {
        super.describe(sender, ignoreProperties);
        Set<String> hideKeys = getConfiguration().getKeys(false);
        if (ignoreProperties != null) {
            hideKeys.addAll(ignoreProperties);
        }
        template.describe(sender, hideKeys);
        hideKeys.addAll(template.getConfiguration().getKeys(false));

        MageClass parent = getParent();
        if (parent != null) {
            sender.sendMessage(ChatColor.AQUA + "Parent Class: " + ChatColor.GREEN + parent.getTemplate().getKey());
            parent.describe(sender, hideKeys);
        }
    }

    public ConfigurationSection getEffectiveConfiguration() {
        ConfigurationSection effectiveConfiguration = ConfigurationUtils.cloneConfiguration(getConfiguration());
        ConfigurationSection templateConfiguration = template.getConfiguration();
        ConfigurationUtils.overlayConfigurations(effectiveConfiguration, templateConfiguration);
        if (parent != null) {
            ConfigurationSection parentConfiguration = parent.getEffectiveConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, parentConfiguration);
        } else {
            // If we have a parent, it has already incorporated Mage data
            ConfigurationSection mageConfiguration = mage.getConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, mageConfiguration);
        }
        return effectiveConfiguration;
    }

    protected BaseMagicConfigurable getPropertyHolder(MagicPropertyType propertyType) {
        switch (propertyType) {
            case SUBCLASS: return this;
            case CLASS: return getRoot();
            case MAGE: return mage;
        }
        return null;
    }
}
