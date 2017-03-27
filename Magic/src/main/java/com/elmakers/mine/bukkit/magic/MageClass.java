package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

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
}
