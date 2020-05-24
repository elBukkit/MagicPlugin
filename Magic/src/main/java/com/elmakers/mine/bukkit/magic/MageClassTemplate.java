package com.elmakers.mine.bukkit.magic;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MageClassTemplate extends TemplateProperties implements com.elmakers.mine.bukkit.api.magic.MageClassTemplate {
    private MageClassTemplate parent;
    private boolean isLocked = false;
    private String name;
    private String description;

    public MageClassTemplate(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        super(controller, key, configuration);
        checkNotNull(configuration, "configuration");

        isLocked = getProperty("locked", false);

        // Clear properties we don't want to pass along
        clearProperty("locked");
        clearProperty("parent");
        clearProperty("path_start");
        clearProperty("hidden");
        clearProperty("enabled");
        clearProperty("inherit");

        name = controller.getMessages().get("classes." + key + ".name", "");
        description = controller.getMessages().get("classes." + key + ".description", "");

        name = configuration.getString("name", name);
        description = configuration.getString("description", description);
    }

    private MageClassTemplate(MageClassTemplate copy, ConfigurationSection configuration) {
        super(copy.controller, copy.getKey(), configuration);
        this.isLocked = copy.isLocked;
        this.name = copy.name;
        this.description = copy.description;
        this.parent = copy.parent;
    }

    public MageClassTemplate getMageTemplate(Mage mage) {
        MageParameters parameters = new MageParameters(mage, "Mage class " + getKey());
        ConfigurationUtils.addConfigurations(parameters, configuration);
        return new MageClassTemplate(this, parameters);
    }

    @Override
    public @Nullable MageClassTemplate getParent() {
        return parent;
    }

    public void setParent(@Nullable  MageClassTemplate parent) {
        this.parent = parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean isLocked() {
        if (isLocked) return true;
        if (parent != null) return parent.isLocked();
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public @Nullable String getIconKey() {
        return getString("icon");
    }

    @Override
    public @Nullable String getIconDisabledKey() {
        return getString("icon_disabled");
    }
}
