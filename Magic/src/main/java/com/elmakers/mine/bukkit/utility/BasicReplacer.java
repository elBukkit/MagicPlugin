package com.elmakers.mine.bukkit.utility;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BasicReplacer implements Replacer {
    private final Location location;
    private final Player player;

    public BasicReplacer(Location location, Player player) {
        this.location = location;
        this.player = player;
    }

    @Nullable
    @Override
    public String getReplacement(String line, boolean integerValues) {
        switch (line) {
            case "pd": return player.getDisplayName();
            case "pn":
            case "p":
                return player.getName();
            case "uuid": return player.getUniqueId().toString();
            case "world": return location.getWorld().getName();
            case "x": return Double.toString(location.getX());
            case "y": return Double.toString(location.getY());
            case "z": return Double.toString(location.getZ());
        }
        return null;
    }
}
