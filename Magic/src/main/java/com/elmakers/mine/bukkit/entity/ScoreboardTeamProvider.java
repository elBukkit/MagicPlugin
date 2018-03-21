package com.elmakers.mine.bukkit.entity;

import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardTeamProvider implements TeamProvider {
    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (attacker instanceof Player && entity instanceof Player)
        {
            Player player1 = (Player)attacker;
            Player player2 = (Player)entity;

            Scoreboard scoreboard1 = player1.getScoreboard();
            Scoreboard scoreboard2 = player2.getScoreboard();

            if (scoreboard1 != null && scoreboard2 != null)
            {
                Team team1 = scoreboard1.getEntryTeam(player1.getName());
                Team team2 = scoreboard2.getEntryTeam(player2.getName());
                if (team1 != null && team2 != null && team1.equals(team2))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
