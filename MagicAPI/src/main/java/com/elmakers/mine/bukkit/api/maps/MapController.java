package com.elmakers.mine.bukkit.api.maps;

import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;

public interface MapController {
    List<URLMap> getAll();
    URLMap getMap(int id);
    void loadMap(String world, int id, String url, String name, int x, int y, int width, int height, Integer priority);
    ItemStack getURLItem(String world, String url, String name, int x, int y, int width, int height, Integer priority);
    List<ItemStack> getURLSlices(String world, String url, String name, int xSlices, int ySlices, Integer priority);
    void forceReloadPlayerPortrait(String worldName, String playerName);
    @Nullable
    ItemStack getPlayerPortrait(String worldName, String playerName, Integer priority, String photoName);
    ItemStack getMapItem(int id);
    ItemStack getMapItem(int id, boolean named);
    boolean hasMap(int id);
    boolean remove(int id);
    void save();
    int getURLMapId(String world, String url, String name, int x, int y, int width, int height, Integer priority);
    int getURLMapId(String world, String url);
}
