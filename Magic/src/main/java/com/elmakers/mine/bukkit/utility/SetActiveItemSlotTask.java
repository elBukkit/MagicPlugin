package com.elmakers.mine.bukkit.utility;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SetActiveItemSlotTask extends BukkitRunnable {
    private final Player player;
    private final int itemSlot;

    public SetActiveItemSlotTask(Player player, int slot) {
        this.player = player;
        this.itemSlot = slot;
    }

    @Override
    public void run() {
        player.getInventory().setHeldItemSlot(itemSlot);
    }
}
