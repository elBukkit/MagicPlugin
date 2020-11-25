package com.elmakers.mine.bukkit.magic.listener;

import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

import com.elmakers.mine.bukkit.magic.MagicController;

public class JumpController implements Listener {
    private final MagicController controller;

    public JumpController(MagicController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onPlayerStatistic(PlayerStatisticIncrementEvent event) {
        if (event.getStatistic() == Statistic.JUMP) {
            controller.onPlayerJump(event.getPlayer());
        }
    }
}
