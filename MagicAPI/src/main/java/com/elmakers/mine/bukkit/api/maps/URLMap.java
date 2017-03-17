package com.elmakers.mine.bukkit.api.maps;

import org.bukkit.World;

public interface URLMap {
    short getId();
    String getName();
    void setName(String name);
    String getURL();
    boolean matches(String keyword);
    boolean isEnabled();
    boolean fix(World world, int maxIds);
}
