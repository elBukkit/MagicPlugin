package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;

public interface MagicProperties {
    Object getProperty(String key);
    @Nonnull <T> T getProperty(@Nonnull String key, @Nonnull T defaultValue);
}
