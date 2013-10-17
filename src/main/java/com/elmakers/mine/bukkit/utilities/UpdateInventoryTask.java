package com.elmakers.mine.bukkit.utilities;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateInventoryTask extends BukkitRunnable {
    private final Player player;
 
    public UpdateInventoryTask(Player player) {
        this.player = player;
    }
 
    public void run() {
        player.updateInventory();
    }
}