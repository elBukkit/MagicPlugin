package com.elmakers.mine.bukkit.warp;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class WarpController {
    private CommandBookWarps commandBook;
    private EssentialsWarps essentials;

    @Nullable
    public Location getWarp(String warpName) {
        Location warp = null;
        if (commandBook != null) {
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
}
