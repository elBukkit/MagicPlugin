package com.elmakers.mine.bukkit.entity;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.entity.TeamProvider;

public class PermissionsTeamProvider implements TeamProvider {
    private final List<List<String>> permissionGroups;

    public PermissionsTeamProvider(List<List<String>> permissionGroups) {
        this.permissionGroups = permissionGroups;
    }

    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (attacker instanceof Player && entity instanceof Player)
        {
            Player player1 = (Player)attacker;
            Player player2 = (Player)entity;

            for (List<String> groups : permissionGroups) {
                boolean isInGroup = false;
                for (String permission : groups) {
                    if (player1.hasPermission(permission)) {
                        isInGroup = true;
                        break;
                    }
                }
                if (isInGroup) {
                    for (String permission : groups) {
                        if (player2.hasPermission(permission)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
