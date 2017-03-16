package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MageClassTemplate extends BaseMagicProperties {
    private MageClassTemplate parent;
    private final String key;

    public MageClassTemplate(@Nonnull String key, @Nonnull MageController controller) {
        super(controller);
        this.key = key;
    }

    public @Nonnull String getKey() {
        return key;
    }

    public @Nullable MageClassTemplate getParent() {
        return parent;
    }

    public void setParent(@Nonnull  MageClassTemplate parent) {
        this.parent = parent;
    }
}
