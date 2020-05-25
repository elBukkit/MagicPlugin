package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

public interface MageModifier {
    @Nonnull
    String getKey();
    @Nonnull
    String getName();
    @Nullable
    String getDescription();
    int getDuration();
    int getTimeRemaining();
    boolean hasDuration();
    @Nullable
    ModifierTemplate getTemplate();
    @Nonnull
    ConfigurationSection getConfiguration();
}
