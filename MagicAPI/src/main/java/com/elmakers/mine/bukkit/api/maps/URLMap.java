package com.elmakers.mine.bukkit.api.maps;

import org.bukkit.World;

public interface URLMap {
    public short getId();
    public String getName();
    public String getURL();
    public boolean matches(String keyword);
    public boolean isEnabled();
    public boolean fix(World world, int maxIds);
}
