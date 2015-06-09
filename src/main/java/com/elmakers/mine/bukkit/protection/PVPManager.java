package com.elmakers.mine.bukkit.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PVPManager {
    public boolean isPVPAllowed(Player player, Location location);
}
