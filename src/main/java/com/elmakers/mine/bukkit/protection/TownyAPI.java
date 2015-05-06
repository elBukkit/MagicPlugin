package com.elmakers.mine.bukkit.protection;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TownyAPI
{
    private final Towny towny;

    public TownyAPI(Plugin plugin) throws IllegalArgumentException {

        if (!(plugin instanceof Towny)) {
            throw new IllegalArgumentException("Towny plugin not an instance of Towny class");
        }
        towny = (Towny) plugin;
    }

    public boolean isPVPAllowed(Location location) {
        if (towny == null || location == null)
            return true;
        TownBlock townBlock = TownyUniverse.getTownBlock(location);
        Town town = null;
        try {
            if (townBlock.hasTown()) {
                town = townBlock.getTown();
            }
        } catch (NotRegisteredException ex){

        }
        if (town == null) return true;

        return town.isPVP();
    }

    public boolean hasBuildPermission(Player player, Block block) {
        if (block != null && towny != null) {
            if (TownyUniverse.isWilderness(block)) {
                return true;
            }
            try {
                TownBlockOwner owner = TownyUniverse.getDataSource()
                        .getResident(player.getName());
                return TownyUniverse.getTownBlock(block.getLocation()).isOwner(
                        owner);
            } catch (NotRegisteredException e) {
                return false;
            }
        }
        return true;
    }
}
