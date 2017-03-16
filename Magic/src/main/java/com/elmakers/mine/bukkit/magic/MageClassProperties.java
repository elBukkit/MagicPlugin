package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MageClassProperties extends BaseMagicConfigurable {
    private MageClassProperties parent;
    private final MageProperties mage;

    public MageClassProperties(@Nonnull MageProperties mage, @Nonnull MageController controller) {
        super(MagicPropertyType.CLASS, controller);
        parent = null;
        this.mage = mage;
    }

    @Override
    protected void addEffectiveConfiguration(@Nonnull ConfigurationSection effectiveConfiguration) {
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
