package com.elmakers.mine.bukkit.api.maps;

public interface URLMap {
    public short getId();
    public String getName();
    public String getURL();
    public boolean matches(String keyword);
}
