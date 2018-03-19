package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.wand.Wand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class MageClass extends TemplatedProperties implements com.elmakers.mine.bukkit.api.magic.MageClass  {
    protected final MageClassTemplate template;
    protected final MageProperties mageProperties;
    protected final Mage mage;
    private MageClass parent;

    public MageClass(@Nonnull Mage mage, @Nonnull MageClassTemplate template) {
        super(template.hasParent() ? MagicPropertyType.SUBCLASS : MagicPropertyType.CLASS, mage.getController());
        this.template = template;
        this.mageProperties = mage.getProperties();
        this.mage = mage;
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
        return hasOwnProperty(key) || template.hasProperty(key);
    }

    @Override
    public Object getProperty(String key) {
        Object value = null;
        BaseMagicProperties storage = getStorage(key);
        if (storage != null && storage != this) {
            value = storage.getProperty(key);
        }
        if (value == null) {
            value = super.getProperty(key);
        }
        if (value == null) {
            value = template.getProperty(key);
        }
        return value;
    }

    @Override
    public Object getInheritedProperty(String key) {
        Object value = super.getProperty(key);
        if (value == null) {
            value = template.getProperty(key);
        }
        if (value == null && parent != null) {
            value = parent.getInheritedProperty(key);
        }
        return value;
    }

    @Override
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
    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties, @Nullable Set<String> overriddenProperties) {
        super.describe(sender, ignoreProperties, overriddenProperties);
        if (overriddenProperties == null) {
            overriddenProperties = new HashSet<>();
        }
        Set<String> ownKeys = getConfiguration().getKeys(false);
        overriddenProperties.addAll(ownKeys);
        sender.sendMessage("" + ChatColor.BOLD + ChatColor.GREEN + "Template Configuration for (" + ChatColor.DARK_GREEN + getKey() + ChatColor.GREEN + "):");
        template.describe(sender, ignoreProperties, overriddenProperties);
        overriddenProperties.addAll(template.getConfiguration().getKeys(false));

        MageClass parent = getParent();
        if (parent != null) {
            sender.sendMessage(ChatColor.AQUA + "Parent Class: " + ChatColor.GREEN + parent.getTemplate().getKey());
            parent.describe(sender, ignoreProperties, overriddenProperties);
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
            ConfigurationSection mageConfiguration = mageProperties.getConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, mageConfiguration);
        }
        return effectiveConfiguration;
    }

    @Override
    protected BaseMagicConfigurable getStorage(MagicPropertyType propertyType) {
        switch (propertyType) {
            case SUBCLASS: return this;
            case CLASS: return getRoot();
            case MAGE: return mageProperties;
            default: return null;
        }
    }

    @Override
    public boolean tickMana() {
        if (!hasOwnMana() && parent != null) {
            return parent.tickMana();
        }

        return super.tickMana();
    }

    @Override
    public Mage getMage() {
        return mage;
    }

    @Override
    public boolean isPlayer() {
        return mageProperties.isPlayer();
    }

    @Override
    public Player getPlayer() {
        return mageProperties.getPlayer();
    }

    @Override
    public void loadProperties() {
        if (parent != null) {
            parent.loadProperties();
        }
        super.loadProperties();
        armorUpdated();
    }

    @Override
    public String getKey() {
        return template.getKey();
    }

    public void armorUpdated() {
        if (hasOwnMana()) {
            updateMaxMana(mage);
        }
    }

    public boolean updateMaxMana(Mage mage) {
        if (!hasOwnMana()) {
            boolean modified = false;
            if (parent != null) {
                modified = parent.updateMaxMana(mage);
                effectiveManaMax = parent.getEffectiveManaMax();
                effectiveManaRegeneration = parent.getEffectiveManaRegeneration();
            }
            return modified;
        }

        return super.updateMaxMana(mage);
    }

    public void updated() {
        updateMaxMana(mage);
        Wand activeWand = mage.getActiveWand();
        if (activeWand != null) {
            activeWand.updated();
        }
    }

    @Override
    public ProgressionPath getPath() {
        String pathKey = getString("path");
        if (pathKey == null || pathKey.length() == 0) {
            pathKey = controller.getDefaultWandPath();
        }
        return controller.getPath(pathKey);
    }

    @Override
    public String getName() {
        return template.getName();
    }

    public boolean isLocked() {
        if (super.getProperty("locked", false)) return true;
        if (parent != null) return parent.isLocked();
        return false;
    }

    public void unlock() {
        configuration.set("locked", null);
        if (parent != null) parent.unlock();
    }

    public void lock() {
        configuration.set("locked", true);
    }
}
