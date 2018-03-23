package com.elmakers.mine.bukkit.api.maps;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public interface MapController {
    List<URLMap> getAll();
    URLMap getMap(short id);
    void loadMap(String world, short id, String url, String name, int x, int y, int width, int height, Integer priority);
    ItemStack getURLItem(String world, String url, String name, int x, int y, int width, int height, Integer priority);
    void forceReloadPlayerPortrait(String worldName, String playerName);
    ItemStack getPlayerPortrait(String worldName, String playerName, Integer priority, String photoName);
    ItemStack getMapItem(short id);
    boolean hasMap(short id);
    boolean remove(short id);
    void save();
    short getURLMapId(String world, String url, String name, int x, int y, int width, int height, Integer priority);
    short getURLMapId(String world, String url);
}
