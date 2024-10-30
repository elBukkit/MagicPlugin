package com.elmakers.mine.bukkit.protection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;
import com.elmakers.mine.bukkit.api.protection.PlayerWarp;
import com.elmakers.mine.bukkit.api.protection.PlayerWarpManager;
import com.elmakers.mine.bukkit.magic.MagicPlugin;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.land.LandWorld;
import me.angeschossen.lands.api.player.LandPlayer;

public class LandsManager implements PVPManager, BlockBreakManager, BlockBuildManager,
        EntityTargetingManager, PlayerWarpManager {
    private final MageController controller;
    private final LandsIntegration lands;

    public LandsManager(MageController controller, ConfigurationSection configuration) {
        this.controller = controller;
        this.lands = LandsIntegration.of(MagicPlugin.getAPI().getPlugin());
    }

    protected boolean hasPermission(Player player, Location location, RoleFlag flag, boolean sendMessage) {
        LandWorld landWorld = lands.getWorld(player.getWorld());
        LandPlayer landPlayer = lands.getLandPlayer(player.getUniqueId());

        if (landWorld != null) {
            return landWorld.hasRoleFlag(landPlayer, location, flag, null, sendMessage);
        }

        return true;
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        try {
            return hasPermission(player, location, Flags.ATTACK_PLAYER, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Lands pvp checks", ex);
        }

        return true;
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        try {
            return hasPermission(player, block.getLocation(), Flags.BLOCK_PLACE, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Lands build checks", ex);
        }

        return true;
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        try {
            return hasPermission(player, block.getLocation(), Flags.BLOCK_BREAK, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Lands break checks", ex);
        }

        return true;
    }

    @Override
    public boolean canTarget(Entity source, Entity target) {
        if (!(source instanceof Player)) {
            return true;
        }
        try {
            Player sourcePlayer = (Player) source;

            if (target instanceof Monster) {
                return hasPermission(sourcePlayer, target.getLocation(), Flags.ATTACK_MONSTER, true);
            } else if (target instanceof Animals) {
                return hasPermission(sourcePlayer, target.getLocation(), Flags.ATTACK_ANIMAL, true);
            } else {
                return hasPermission(sourcePlayer, target.getLocation(), Flags.INTERACT_GENERAL, false);
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Lands entity targeting checks", ex);
        }

        return true;
    }

    @Nullable
    @Override
    public Collection<PlayerWarp> getWarps(@Nonnull Player player) {
        LandPlayer landPlayer = lands.getLandPlayer(player.getUniqueId());

        Collection<? extends Land> joinedLands = landPlayer.getLands();

        if (joinedLands.isEmpty()) {
            return null;
        }
        Collection<PlayerWarp> warps = new ArrayList<>();
        for (Land joinedLand : joinedLands) {
            Location location = joinedLand.getSpawn();
            if (location != null) {
                PlayerWarp warp = new PlayerWarp(joinedLand.getName(), location);
                warps.add(warp);
            }
        }
        return warps;
    }
}
