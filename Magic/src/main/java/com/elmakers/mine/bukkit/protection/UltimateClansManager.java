package com.elmakers.mine.bukkit.protection;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.magic.MagicController;

import me.ulrich.clans.api.ClanAPI;

public class UltimateClansManager implements TeamProvider {
    public UltimateClansManager(MagicController controller) {
    }

    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (!(attacker instanceof Player) || !(entity instanceof Player)) return false;
        Player player1 = (Player)attacker;
        Player player2 = (Player)entity;
        // Is this by name or UUID-as-string?
        return ClanAPI.getInstance().isAlly(player1.getName(), player2.getName());
    }
}
