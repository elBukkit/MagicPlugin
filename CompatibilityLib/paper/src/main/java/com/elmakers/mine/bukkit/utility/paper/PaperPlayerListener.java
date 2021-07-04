package com.elmakers.mine.bukkit.utility.paper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class PaperPlayerListener implements Listener {
    private MageController controller;

    public PaperPlayerListener(MageController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        controller.onPlayerJump(event.getPlayer(), event.getTo().toVector().subtract(event.getFrom().toVector()));
    }
}
