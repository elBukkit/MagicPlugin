package com.elmakers.mine.bukkit.api.maps;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface MapController {
    public Collection<URLMap> getAll();
    public void loadMap(String world, short id, String url, String name, int x, int y, int width, int height, Integer priority);
    public ItemStack getURLItem(String world, String url, String name, int x, int y, int width, int height, Integer priority);
    public void forceReloadPlayerPortrait(String worldName, String playerName);
    public ItemStack getPlayerPortrait(String worldName, String playerName, Integer priority, String photoName);
    public ItemStack getMapItem(short id);
    public boolean hasMap(short id);
}
