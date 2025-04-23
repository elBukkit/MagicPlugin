package com.elmakers.mine.bukkit.utility.platform.base.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.rp.ResourcePackStatus;

public class ResourcePackListener implements Listener {
    private final MageController controller;

    public ResourcePackListener(final MageController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        ResourcePackStatus status = ResourcePackStatus.UNKNOWN;
        controller.info("Got resource pack status from player " + event.getPlayer().getName() + ": " + event.getStatus(), 15);
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED:
                status = ResourcePackStatus.LOADED;
                break;
            case DECLINED:
                status = ResourcePackStatus.DECLINED;
                break;
            case FAILED_DOWNLOAD:
                status = ResourcePackStatus.FAILED;
                break;
            default:
                // Don't notify otherwise
                return;
        }
        controller.onResourcePackStatus(event.getPlayer(), status);
    }
}
