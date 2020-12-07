package com.elmakers.mine.bukkit.protection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

import us.ajg0702.parkour.api.events.PrePlayerStartParkourEvent;

public class AJParkourManager implements Listener {
    private final MageController controller;

    public AJParkourManager(MageController controller) {
        this.controller = controller;
        controller.getLogger().info("ajParkour found, wands will close when entering parkour");
        controller.getPlugin().getServer().getPluginManager().registerEvents(this, controller.getPlugin());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onStartParkour(PrePlayerStartParkourEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        mage.deactivate();
    }
}
