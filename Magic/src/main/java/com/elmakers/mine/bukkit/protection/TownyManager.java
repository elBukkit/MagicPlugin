package com.elmakers.mine.bukkit.protection;

import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TownyManager implements PVPManager, BlockBreakManager, BlockBuildManager {
    private boolean enabled = false;
    private TownyAPI towny = null;
    protected boolean wildernessBypass;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && towny != null;
    }

    public void initialize(Plugin plugin) {
        if (enabled) {
            try {
                Plugin townyPlugin = plugin.getServer().getPluginManager()
                        .getPlugin("Towny");
                if (townyPlugin != null) {
                    towny = new TownyAPI(this, townyPlugin);
                }
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.WARNING, "Error initializing Towny integration", ex);
            }

            if (towny == null) {
                plugin.getLogger()
                        .info("Towny not found, region protection and pvp checks will not be used.");
            } else {
                plugin.getLogger().info("Towny found, but integration is not currently working due to 1.13 updates. If Towny has fixed their reliance on block ids, please let me know!");
                //plugin.getLogger().info("Towny found, will respect build permissions for construction spells");
            }
        } else {
            plugin.getLogger()
                    .info("Towny manager disabled, region protection and pvp checks will not be used.");
            towny = null;
        }
    }

    public void setWildernessBypass(boolean bypass) {
        wildernessBypass = bypass;
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        if (enabled && block != null && towny != null) {
            return towny.hasBuildPermission(player, block);
        }
        return true;
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        if (enabled && block != null && towny != null) {
            return towny.hasBreakPermission(player, block);
        }
        return true;
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        if (!enabled || towny == null || location == null)
            return true;
        return towny.isPVPAllowed(location);
    }

    public boolean canTarget(Entity entity, Entity target) {
        if (!enabled || towny == null || entity == null || target == null)
            return true;
        return towny.canTarget(entity, target);
    }

    @Nullable
    public Location getTownLocation(Player player) {
        if (!enabled || towny == null || player == null)
            return null;

        return towny.getTownLocation(player);
    }

    public boolean createPlot(Location center, Double price) {
        if (!enabled || towny == null || center == null)
            return false;

        return towny.createPlot(center, price);
    }
}
