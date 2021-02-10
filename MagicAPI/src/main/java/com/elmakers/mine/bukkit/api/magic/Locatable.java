package com.elmakers.mine.bukkit.api.magic;

import org.bukkit.Location;

public interface Locatable {
    String getName();
    Location getLocation();
    boolean isActive();
    default boolean isEnabled() {
        return true;
    }
}
