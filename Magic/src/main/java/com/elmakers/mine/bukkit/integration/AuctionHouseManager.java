package com.elmakers.mine.bukkit.integration;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;

import fr.maxlego08.zauctionhouse.api.event.events.AuctionPreSellEvent;

public class AuctionHouseManager implements Listener {
    private final MageController controller;
    private boolean enabled;
    private boolean registered;

    public AuctionHouseManager(MageController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection configuration) {
        enabled = configuration.getBoolean("enabled", true);
        String statusString;
        if (!enabled) {
            statusString = " but is disabled in Magic's configs";
        } else {
            statusString = ", will prevent auctioning bound wands";
        }
        if (!registered && enabled) {
            register();
        }
        controller.getLogger().info("zAuctionHouse found" + statusString);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, controller.getPlugin());
        registered = true;
    }

    @EventHandler
    public void onSell(AuctionPreSellEvent event) {
        if (!enabled) return;
        ItemStack item = event.getItemStack();
        if (controller.isSkill(item)) {
            event.setCancelled(true);
            return;
        }
        Wand wand = controller.getIfWand(item);
        if (wand != null && wand.isBound()) {
            event.setCancelled(true);
        }
    }
}
