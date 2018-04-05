package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.CombatUtil;

public class TownyAPI
{
    private final Towny towny;
    private final TownyManager controller;
    private Method getPlayerByNameMethod;
    private Method getPlayerByUUIDMethod;

    public TownyAPI(TownyManager manager, Plugin plugin) throws IllegalArgumentException, NoSuchMethodException {
        this.controller = manager;
        if (!(plugin instanceof Towny)) {
            throw new IllegalArgumentException("Towny plugin not an instance of Towny class");
        }
        towny = (Towny) plugin;
        try {
            getPlayerByNameMethod = TownyDataSource.class.getMethod("getResident", String.class);
        } catch (Exception ex) {
            getPlayerByUUIDMethod = TownyDataSource.class.getMethod("getResident", UUID.class);
        }
    }

    public boolean isPVPAllowed(Location location) {
        if (towny == null || location == null) {
            return true;
        }

        if (controller.wildernessBypass && TownyUniverse.isWilderness(location.getBlock())) {
            return true;
        }

        TownBlock townBlock = TownyUniverse.getTownBlock(location);
        if (townBlock == null) return true;

        TownyWorld world = townBlock.getWorld();
        Coord coord = Coord.parseCoord(location);
        if (world.isWarZone(coord)) {
            return true;
        }

        if (townBlock.getType().equals(TownBlockType.ARENA)) {
            return true;
        }

        if (townBlock.getWorld().isForcePVP()) {
            return true;
        }

        Town town = null;
        try {
            if (townBlock.hasTown()) {
                town = townBlock.getTown();
            }
        } catch (NotRegisteredException ignored) {
        }

        if (town == null) return true;

        return !town.isAdminDisabledPVP() && town.isPVP();
    }

    public boolean hasBuildPermission(Player player, Block block) {
        if (block != null && towny != null && player != null) {
            if (controller.wildernessBypass && TownyUniverse.isWilderness(block)) {
                return true;
            }
            //return PlayerCacheUtil.getCachePermission(player, block.getLocation(), DeprecatedUtils.getTypeId(block), DeprecatedUtils.getData(block), TownyPermission.ActionType.BUILD);
        }
        return true;
    }

    public boolean hasBreakPermission(Player player, Block block) {
        if (block != null && towny != null && player != null) {
            if (controller.wildernessBypass && TownyUniverse.isWilderness(block)) {
                return true;
            }
            //return PlayerCacheUtil.getCachePermission(player, block.getLocation(), DeprecatedUtils.getTypeId(block), DeprecatedUtils.getData(block), TownyPermission.ActionType.DESTROY);
        }
        return true;
    }

    public boolean canTarget(Entity entity, Entity target) {
        if (towny != null && target != null && entity != null) {
            // TODO: Handle non-entity casts (automata...)?
            return !CombatUtil.preventDamageCall(towny, entity, target);
        }

        return true;
    }

    @Nullable
    protected Resident getResident(Player player) {
        try {
            if (getPlayerByNameMethod != null) {
                return (Resident) getPlayerByNameMethod.invoke(TownyUniverse.getDataSource(), player.getName());
            }
            if (getPlayerByUUIDMethod != null) {
                return (Resident) getPlayerByUUIDMethod.invoke(TownyUniverse.getDataSource(), player.getUniqueId());
            }
        } catch (Exception ex) {
            if (!(ex instanceof TownyException)) {
                Bukkit.getLogger().log(Level.WARNING, "Error getting Towny Resident", ex);
            }
        }
        return null;
    }

    @Nullable
    public Location getTownLocation(Player player) {
        if (towny == null || player == null) {
            return null;
        }

        Resident resident = getResident(player);
        if (resident == null) {
            return null;
        }

        Town town = null;
        try {
            town = resident.getTown();
        } catch (Exception ignored) {
        }

        if (town == null) {
            return null;
        }

        Location location = null;
        try {
            location = town.getSpawn();
        } catch (Exception ignored) {

        }
        return location;
    }

    public boolean createPlot(Location center, Double price) {
        WorldCoord worldCoord = new WorldCoord(center.getWorld().getName(), Coord.parseCoord(center));
        try {
            TownBlock townBlock = worldCoord.getTownBlock();
            if (price == null) {
                price = worldCoord.getTownBlock().getTown()
                        .getPlotTypePrice(worldCoord.getTownBlock().getType());
            }
            townBlock.setPlotPrice(price);
        } catch (Exception ex) {
            return false;
        }

        return true;
    }
}
