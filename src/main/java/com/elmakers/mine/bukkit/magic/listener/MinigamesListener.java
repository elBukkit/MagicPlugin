package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import au.com.mineauz.minigames.events.JoinMinigameEvent;

public class MinigamesListener implements Listener {

    private MageController controller;

    public MinigamesListener(MageController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onJoinMinigame(JoinMinigameEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        Mage mage = controller.getRegisteredMage(player.getUniqueId().toString());
        if (mage == null) return;

        mage.deactivate();
    }
}
