package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import static com.google.common.base.Preconditions.*;

import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MageClassTemplate extends BaseMagicProperties {
    private MageClassTemplate parent;
    private final String key;
    private boolean isLocked = false;

    public MageClassTemplate(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        super(controller);
        checkNotNull(key, "key");
        checkNotNull(configuration, "configuration");

        this.key = key;
        this.load(configuration);

        isLocked = getProperty("locked", false);

        // Clear properties we don't want to pass along
        clearProperty("locked");
        clearProperty("parent");
        clearProperty("path_start");
        clearProperty("hidden");
        clearProperty("enabled");
        clearProperty("inherit");
    }

    public @Nonnull String getKey() {
        return key;
    }

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
        return isLocked;
    }

    private void clearProperty(String key) {
        configuration.set(key, null);
    }
}
