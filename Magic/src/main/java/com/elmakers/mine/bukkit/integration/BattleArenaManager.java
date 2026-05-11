package com.elmakers.mine.bukkit.integration;

import org.battleplugins.arena.ArenaPlayer;
import org.battleplugins.arena.team.ArenaTeam;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.entity.TeamProvider;

public class BattleArenaManager implements TeamProvider {
    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (!(attacker instanceof Player) || ! (entity instanceof Player)) return false;
        ArenaPlayer attackerPlayer = ArenaPlayer.getArenaPlayer((Player)attacker);
        ArenaPlayer targetPlayer = ArenaPlayer.getArenaPlayer((Player)entity);
        if (attackerPlayer == null || targetPlayer == null) return false;
        ArenaTeam attackerTeam = attackerPlayer.getTeam();
        ArenaTeam targetTeam = targetPlayer.getTeam();
        if (attackerTeam == null || targetPlayer == null) return false;
        return !attackerTeam.isHostileTo(targetTeam);
    }
}
