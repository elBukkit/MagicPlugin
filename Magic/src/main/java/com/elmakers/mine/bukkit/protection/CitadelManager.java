package com.elmakers.mine.bukkit.protection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class CitadelManager implements BlockBreakManager {
    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        Reinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(block.getLocation());
        return reinforcement == null || !(reinforcement instanceof PlayerReinforcement);
    }
}
