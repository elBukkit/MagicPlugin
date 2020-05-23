package com.elmakers.mine.bukkit.magic;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ModifierTemplate extends BaseMagicProperties implements com.elmakers.mine.bukkit.api.magic.ModifierTemplate {
    private ModifierTemplate parent;
    private final @Nonnull String key;
    private String name;
    private String description;

    public ModifierTemplate(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        super(controller);
        checkNotNull(key, "key");
        checkNotNull(configuration, "configuration");

        this.key = key;
        this.load(configuration);

        // Clear properties we don't want to pass along
        clearProperty("parent");
        clearProperty("enabled");
        clearProperty("inherit");

        name = controller.getMessages().get("modifiers." + key + ".name", "");
        description = controller.getMessages().get("modifiers." + key + ".description", "");

        name = configuration.getString("name", name);
        description = configuration.getString("description", description);
    }

    private ModifierTemplate(ModifierTemplate copy, ConfigurationSection configuration) {
        super(copy.controller, configuration);
        this.name = copy.name;
        this.description = copy.description;
        this.key = copy.key;
        this.parent = copy.parent;
    }

    public ModifierTemplate getModifierTemplate(Mage mage) {
        MageParameters parameters = new MageParameters(mage, "Modifier " + getKey());
        ConfigurationUtils.addConfigurations(parameters, configuration);
        return new ModifierTemplate(this, parameters);
    }

    public @Nonnull String getKey() {
        return key;
    }

    public @Nullable
    ModifierTemplate getParent() {
        return parent;
    }

    public void setParent(@Nullable ModifierTemplate parent) {
        this.parent = parent;
    }

    public boolean hasParent() {
        return parent != null;
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
