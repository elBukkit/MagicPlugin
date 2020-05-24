package com.elmakers.mine.bukkit.magic;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;

public abstract class TemplateProperties extends BaseMagicProperties {
    private final String key;

    protected TemplateProperties(@Nonnull MageController controller, @Nonnull String key) {
        super(controller);
        checkNotNull(key, "key");
        this.key = key;
    }

    protected TemplateProperties(@Nonnull MageController controller, String key, ConfigurationSection configuration) {
        super(controller, configuration);
        checkNotNull(key, "key");
        this.key = key;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nullable
    public abstract String getName();

    @Nullable
    public TemplateProperties getParent() {
        return null;
    }
}
