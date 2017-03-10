package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

public interface MagicProperties {
    boolean hasProperty(@Nonnull String key);
    @Nonnull Object getProperty(@Nonnull String key);
    @Nonnull <T> Optional<? extends T> getProperty(@Nonnull String key, @Nonnull Class<T> type);
    @Nonnull <T> T getProperty(@Nonnull String key, @Nonnull T defaultValue);
}
