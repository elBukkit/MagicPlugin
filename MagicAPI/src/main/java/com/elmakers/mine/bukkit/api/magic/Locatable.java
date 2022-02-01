package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;

import org.bukkit.Location;

public interface Locatable {
    String getName();
    @Nonnull
    Location getLocation();
    boolean isActive();
    default boolean isEnabled() {
        return true;
    }
    default boolean isValid() {
        return true;
    }
}
