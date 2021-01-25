package com.elmakers.mine.bukkit.magic;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.configuration.MageParameters;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ModifierTemplate extends TemplateProperties implements com.elmakers.mine.bukkit.api.magic.ModifierTemplate {
    private ModifierTemplate parent;
    private String name;
    private String description;

    public ModifierTemplate(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        super(controller, key, configuration);
        checkNotNull(configuration, "configuration");

        // Clear properties we don't want to pass along
        clearProperty("parent");
        clearProperty("enabled");
        clearProperty("inherit");

        name = controller.getMessages().get("modifiers." + key + ".name", key);
        description = controller.getMessages().get("modifiers." + key + ".description", "");

        name = configuration.getString("name", name);
        description = configuration.getString("description", description);
    }

    private ModifierTemplate(ModifierTemplate copy, ConfigurationSection configuration) {
        super(copy.controller, copy.getKey(), configuration);
        this.name = copy.name;
        this.description = copy.description;
        this.parent = copy.parent;
    }

    public ModifierTemplate getModifierTemplate(Mage mage) {
        MageParameters parameters = new MageParameters(mage, "Modifier " + getKey());
        ConfigurationUtils.addConfigurations(parameters, configuration);
        return new ModifierTemplate(this, parameters);
    }

    @Override
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
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public ModifierTemplate getMageTemplate(Mage mage) {
        MageParameters parameters = new MageParameters(mage, "Mage modifier " + getKey());
        ConfigurationUtils.addConfigurations(parameters, configuration);
        return new ModifierTemplate(this, parameters);
    }
}
