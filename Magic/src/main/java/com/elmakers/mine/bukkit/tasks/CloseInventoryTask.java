package com.elmakers.mine.bukkit.tasks;

import org.bukkit.entity.Player;

public class CloseInventoryTask implements Runnable {
    private final Player player;

    public CloseInventoryTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        player.closeInventory();
    }
}
