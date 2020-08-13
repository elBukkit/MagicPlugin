package com.elmakers.mine.bukkit.protection;

import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.PlayerManager;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;

public class ResidenceManager implements PVPManager, BlockBreakManager, BlockBuildManager, EntityTargetingManager {
    private final MageController controller;
    private final Residence residence;

    @SuppressWarnings({ "unchecked" })
    public ResidenceManager(Plugin residencePlugin, MageController controller, ConfigurationSection configuration) {
        this.controller = controller;
        this.residence = (Residence)residencePlugin;
    }

    @Nullable
    protected ResidencePlayer getResidencePlayer(Player player) {
        PlayerManager playerManager = residence.getPlayerManager();
        if (playerManager == null) {
            return null;
        }
        return playerManager.getResidencePlayer(player);
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        try {
            FlagPermissions permissions = residence.getPermsByLoc(location);
            if (permissions == null) {
                return true;
            }
            return permissions.has(Flags.pvp, FlagPermissions.FlagCombo.TrueOrNone);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Residence pvp checks", ex);
        }

        return true;
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        try {
            ResidencePlayer residencePlayer = getResidencePlayer(player);
            if (residencePlayer == null) {
                return true;
            }
            return residencePlayer.canPlaceBlock(block, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Residence build checks", ex);
        }

        return true;
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        try {
            ResidencePlayer residencePlayer = getResidencePlayer(player);
            if (residencePlayer == null) {
                return true;
            }
            return residencePlayer.canBreakBlock(block, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Residence break checks", ex);
        }

        return true;
    }

    @Override
    public boolean canTarget(Entity source, Entity target) {
        if (!(source instanceof Player)) {
            return true;
        }
        try {
            ResidencePlayer residencePlayer = getResidencePlayer((Player)source);
            if (residencePlayer == null) {
                return true;
            }
            return residencePlayer.canDamageEntity(target, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Residence entity targeting checks", ex);
        }

        return true;
    }
}
