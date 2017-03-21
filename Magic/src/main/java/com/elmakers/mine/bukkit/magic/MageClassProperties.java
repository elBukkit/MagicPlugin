package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MageClassProperties extends BaseMagicConfigurable {
    protected final MageClassTemplate template;
    protected final MageProperties mage;
    private MageClassProperties parent;

    public MageClassProperties(@Nonnull MageProperties mage, @Nonnull MageClassTemplate template, @Nonnull MageController controller) {
        super(template.hasParent() ? MagicPropertyType.SUBCLASS : MagicPropertyType.CLASS, controller);
        this.template = template;
        this.mage = mage;
    }

    @Override
    protected void rebuildEffectiveConfiguration(@Nonnull ConfigurationSection effectiveConfiguration) {
        ConfigurationSection templateConfiguration = template.getEffectiveConfiguration();
        ConfigurationUtils.addConfigurations(effectiveConfiguration, templateConfiguration, false);
        if (parent != null) {
            ConfigurationSection parentConfiguration = parent.getEffectiveConfiguration();
            ConfigurationUtils.addConfigurations(effectiveConfiguration, parentConfiguration, false);
        } else {
            // If we have a parent, it has already incorporated Mage data
            ConfigurationSection mageConfiguration = mage.getEffectiveConfiguration();
            ConfigurationUtils.addConfigurations(effectiveConfiguration, mageConfiguration, false);
        }
    }

    public @Nullable  MageClassProperties getParent() {
        return parent;
    }

    public void setParent(@Nonnull MageClassProperties parent) {
        this.parent = parent;
        this.dirty = true;
    }
}
