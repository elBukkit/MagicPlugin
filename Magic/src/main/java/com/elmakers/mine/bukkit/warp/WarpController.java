package com.elmakers.mine.bukkit.warp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;

public class WarpController {
    private final MagicController controller;
    private CommandBookWarps commandBook;
    private EssentialsWarps essentials;
    private final Map<String, MagicWarp> warps = new HashMap<>();

    public WarpController(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection warpData) {
        warps.clear();
        Set<String> keys = warpData.getKeys(false);
        for (String key : keys) {
            MagicWarp warp = new MagicWarp(key, warpData);
            warps.put(key, warp);
            warp.checkMarker(controller);
        }
    }

    public void save(ConfigurationSection warpData) {
        for (MagicWarp warp : warps.values()) {
            warp.save(warpData);
        }
    }

    public Collection<String> getCustomWarps() {
        return warps.keySet();
    }

    public boolean hasCustomWarp(String warpName) {
        return warps.containsKey(warpName);
    }

    public void setWarp(String warpName, Location location) {
        MagicWarp warp = warps.get(warpName);
        if (warp == null) {
            warps.put(warpName, new MagicWarp(warpName, location));
        } else {
            warp.setLocation(location);
            warp.checkMarker(controller);
        }
    }

    public boolean removeWarp(String warpName) {
        return warps.remove(warpName) != null;
    }

    public int mapWarps(String markerIcon) {
        int count = 0;
        for (MagicWarp warp : warps.values()) {
            // Only do this for warps that show up in Recall
            String baseIcon = warp.getIcon();
            if (baseIcon == null || baseIcon.isEmpty()) continue;
            String existingIcon = warp.getMarkerIcon();
            if (existingIcon == null || existingIcon.isEmpty()) {
                warp.setMarkerIcon(markerIcon);
                warp.checkMarker(controller);
                count++;
            }
        }
        return count;
    }

    public int importWarps(CommandSender sender) {
        if (commandBook != null) {
            for (Map.Entry<String, Location> warpEntry : commandBook.getWarps().entrySet()) {
                String key = warpEntry.getKey();
                warps.put(key, new MagicWarp(key, warpEntry.getValue()));
            }
        }
        if (essentials != null) {
            for (Map.Entry<String, Location> warpEntry : essentials.getWarps().entrySet()) {
                String key = warpEntry.getKey();
                warps.put(key, new MagicWarp(key, warpEntry.getValue()));
            }
        }
        SpellTemplate recallSpell = controller.getSpellTemplate("recall");
        ConfigurationSection parameters = recallSpell.getConfiguration().getConfigurationSection("parameters");
        ConfigurationSection recallWarps = parameters == null ? null : parameters.getConfigurationSection("warps");
        if (recallWarps != null) {
            boolean imported = false;
            for (String key : recallWarps.getKeys(false)) {
                MagicWarp warp = warps.get(key);
                if (warp != null) {
                    ConfigurationSection warpConfig = recallWarps.getConfigurationSection(key);
                    warp.setName(warpConfig.getString("name", warp.getName()));
                    warp.setDescription(warpConfig.getString("description", warp.getDescription()));
                    String icon = warpConfig.getString("icon");
                    if (icon == null || icon.isEmpty()) {
                        String iconUrl = warpConfig.getString("icon_url");
                        if (iconUrl != null && !iconUrl.isEmpty()) {
                            icon = "skull:" + iconUrl;
                        }
                    }
                    if (icon != null && !icon.isEmpty()) {
                        String currentIcon = warp.getIcon();
                        if (currentIcon == null || currentIcon.isEmpty()) {
                            imported = true;
                        }
                        warp.setIcon(icon);
                    }
                }
            }
            if (imported && sender != null) {
                sender.sendMessage(ChatColor.YELLOW + "Imported Recall Warps" + ChatColor.WHITE
                    + ", you may want to remove your Recall customizations with "
                    + ChatColor.GOLD + "/mconfig reset recall");
            }
        }
        return warps.size();
    }

    @Nullable
    public MagicWarp getMagicWarp(String warpName) {
        return warps.get(warpName);
    }

    @Nonnull
    public Collection<MagicWarp> getMagicWarps() {
        return warps.values();
    }

    @Nullable
    public Location getWarp(String warpName) {
        Location warp = null;
        MagicWarp customWarp = warps.get(warpName);
        if (customWarp != null) {
            warp = customWarp.getLocation();
        }
        if (warp == null && commandBook != null) {
            warp = commandBook.getWarp(warpName);
        }
        if (warp == null && essentials != null) {
            warp = essentials.getWarp(warpName);
        }
        return warp;
    }

    public boolean setCommandBook(Plugin plugin) {
        commandBook = CommandBookWarps.create(plugin);
        return (commandBook != null);
    }

    public boolean setEssentials(Plugin plugin) {
        essentials = EssentialsWarps.create(plugin);
        return (essentials != null);
    }

    public List<String> getWarps() {
        return new ArrayList<>(warps.keySet());
    }
}
