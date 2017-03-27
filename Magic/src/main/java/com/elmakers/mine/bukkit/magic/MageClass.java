package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MageClass extends BaseMagicConfigurable implements com.elmakers.mine.bukkit.api.magic.MageClass  {
    protected final MageClassTemplate template;
    protected final MageProperties mage;
    private MageClass parent;

    public MageClass(@Nonnull Mage mage, @Nonnull MageClassTemplate template) {
        super(template.hasParent() ? MagicPropertyType.SUBCLASS : MagicPropertyType.CLASS, mage.getController());
        this.template = template;
        this.mage = mage.getProperties();
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

    public @Nonnull MageClassTemplate getTemplate() {
        return template;
    }

    public @Nullable MageClass getParent() {
        return parent;
    }

    public void setParent(@Nonnull MageClass parent) {
        this.parent = parent;
        this.dirty = true;
    }
}
