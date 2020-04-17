package com.elmakers.mine.bukkit.integration;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.entity.TeamProvider;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

public class BattleArenaManager implements TeamProvider {
    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (!(attacker instanceof Player) || ! (entity instanceof Player)) return false;
        ArenaPlayer attackerPlayer = BattleArena.toArenaPlayer((Player)attacker);
        ArenaPlayer targetPlayer = BattleArena.toArenaPlayer((Player)entity);
        if (attackerPlayer == null || targetPlayer == null) return false;
        ArenaTeam attackerTeam = attackerPlayer.getTeam();
        if (attackerTeam == null) return false;
        return attackerTeam.hasMember(targetPlayer);
    }
}
