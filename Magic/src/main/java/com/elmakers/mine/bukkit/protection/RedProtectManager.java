package com.elmakers.mine.bukkit.protection;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;

public class RedProtectManager implements BlockBreakManager, BlockBuildManager, EntityTargetingManager, PVPManager {
    private final MageController controller;
    private RedProtectAPI redProtect;
    private boolean allowNonPlayerBuild;
    private boolean allowNonPlayerBreak;

    @SuppressWarnings({ "unchecked" })
    public RedProtectManager(Plugin residencePlugin, MageController controller, ConfigurationSection configuration) {
        this.controller = controller;
        this.redProtect = RedProtect.get().getAPI();
        allowNonPlayerBuild = configuration.getBoolean("allow_non_player_build", false);
        allowNonPlayerBreak = configuration.getBoolean("allow_non_player_break", false);
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        try {
            Region region = redProtect.getRegion(block.getLocation());
            if (region == null) {
                return true;
            }
            if (player == null) {
                return allowNonPlayerBuild;
            }

            return region.canBuild(player);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with RedProtect build checks", ex);
            return true;
        }
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        try {
            Region region = redProtect.getRegion(block.getLocation());
            if (region == null) {
                return true;
            }
            if (player == null) {
                return allowNonPlayerBreak;
            }

            return region.canBuild(player);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with RedProtect break checks", ex);
            return true;
        }
    }

    @Override
    public boolean canTarget(Entity source, Entity target) {
        try {
            Region region = redProtect.getRegion(source.getLocation());
            if (region == null) {
                return true;
            }
            if (source instanceof Player && target instanceof Player) {
                return region.canPVP((Player)source, (Player)target);
            }

            return true;
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with RedProtect targeting checks", ex);
            return true;
        }
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        try {
            Region region = redProtect.getRegion(location);
            if (region == null) {
                return true;
            }
            return region.getFlagBool("pvp");
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with RedProtect pvp checks", ex);
            return true;
        }
    }
}
