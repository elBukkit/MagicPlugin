package com.elmakers.mine.bukkit.protection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class CitadelManager implements BlockBreakManager {
    private boolean indestructiblePlayer;
    private boolean indestructibleOther;
    private boolean durabilityCheck;

    public CitadelManager(MageController controller, ConfigurationSection configuration) {
        controller.getLogger().info("Citadel found, integrating");
        indestructibleOther = configuration.getBoolean("reinforcements_indestructible");
        indestructiblePlayer = configuration.getBoolean("player_reinforcements_indestructible");
        durabilityCheck = configuration.getBoolean("reinforcement_durability");
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        Reinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(block.getLocation());
        if (reinforcement == null) return true;
        if (reinforcement instanceof PlayerReinforcement) {
            return indestructiblePlayer;
        }
        return indestructibleOther;
    }

    @Nullable
    public Integer getDurability(@Nonnull Location location) {
        if (!durabilityCheck) return null;
        Reinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(location);
        if (reinforcement == null) return null;
        return reinforcement.getDurability();
    }
}
