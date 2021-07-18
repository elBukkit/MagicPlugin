package com.elmakers.mine.bukkit.api.maps;

import org.bukkit.World;

public interface URLMap {
    int getId();
    String getName();
    void setName(String name);
    String getURL();
    boolean matches(String keyword);
    boolean isEnabled();
    boolean fix(World world, int maxIds);
    int getX();
    int getY();
    boolean isSlice();
}
