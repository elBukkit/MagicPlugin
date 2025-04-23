package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityEndermiteData extends EntityExtraData {
    private Boolean playerSpawned;

    public EntityEndermiteData(ConfigurationSection parameters) {
        playerSpawned = ConfigUtils.getOptionalBoolean(parameters, "player_spawned");
    }

    public EntityEndermiteData(Entity entity) {
        if (entity instanceof Endermite) {
            Endermite endermite = (Endermite)entity;
            playerSpawned = endermite.isPlayerSpawned();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Endermite) {
            Endermite endermite = (Endermite)entity;
            if (playerSpawned != null) {
                endermite.setPlayerSpawned(playerSpawned);
            }
        }
    }
}
