package com.elmakers.mine.bukkit.api.warp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;

public interface Warp {
    @Nonnull
    String getKey();
    @Nonnull
    String getName();
    @Nullable
    String getDescription();
    @Nullable
    Location getLocation();
    @Nullable
    String getIcon();
    @Nullable
    String getWorldName();
}
