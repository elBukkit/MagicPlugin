package com.elmakers.mine.bukkit.utility.platform.modern.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class ResourcePackListener implements Listener {
    private final MageController controller;

    public ResourcePackListener(final MageController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        boolean accepted = false;
        boolean failed = false;
        controller.info("Got resource pack status from player " + event.getPlayer().getName() + ": " + event.getStatus(), 15);
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED:
                accepted = true;
                break;
            case DECLINED:
                break;
            case FAILED_DOWNLOAD:
                failed = true;
                break;
            default:
                // Don't notify otherwise
                return;
        }
        controller.onResourcePackStatus(event.getPlayer(), accepted, failed);
    }
}
