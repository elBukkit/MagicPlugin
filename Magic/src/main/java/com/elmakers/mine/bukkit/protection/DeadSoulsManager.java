package com.elmakers.mine.bukkit.protection;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.darkyen.minecraft.DeadSoulsAPI;
import com.elmakers.mine.bukkit.api.magic.DeathLocation;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class DeadSoulsManager implements Listener {
    private final MageController controller;
    private final DeadSoulsAPI api;

    public DeadSoulsManager(MageController controller) {
        this.controller = controller;
        api = DeadSoulsAPI.instance();
        controller.getLogger().info("DeadSouls found, souls available in Recall level 2");
        controller.getLogger().info("Disable warping to souls in recall config with allow_souls: false");
        controller.getPlugin().getServer().getPluginManager().registerEvents(this, controller.getPlugin());
    }

    @EventHandler
    public void onPickupSoul(DeadSoulsAPI.SoulPickupEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (!event.isCancelled()) {
            mage.deactivate();
        }
    }

    public void getSoulLocations(Player player, List<DeathLocation> locations) {
        List<DeadSoulsAPI.Soul> souls = new ArrayList<>();
        api.getSoulsByPlayer(souls, player.getUniqueId());
        for (DeadSoulsAPI.Soul soul : souls) {
            Location location = soul.getLocation();
            if (location != null) {
                locations.add(new DeathLocation(location, soul.getItems(), soul.getExperiencePoints()));
            }
        }
    }
}
