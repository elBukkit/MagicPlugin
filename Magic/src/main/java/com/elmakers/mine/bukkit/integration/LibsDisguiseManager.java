package com.elmakers.mine.bukkit.integration;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class LibsDisguiseManager {
    private final Plugin disguisePlugin;

    public LibsDisguiseManager(Plugin owningPlugin, Plugin disguisePlugin) {
        this.disguisePlugin = disguisePlugin;
    }

    public boolean initialize() {
        return (disguisePlugin != null && disguisePlugin instanceof LibsDisguises);
    }

    public boolean isDisguised(Entity entity) {
        return DisguiseAPI.isDisguised(entity);
    }
    
    public boolean disguise(Entity entity, ConfigurationSection configuration) {
        if (configuration == null) {
            DisguiseAPI.undisguiseToAll(entity);
            return true;
        }
        String disguiseName = configuration.getString("type");
        if (disguiseName == null || disguiseName.isEmpty()) {
            return false;
        }
        try {
            DisguiseType disguiseType = DisguiseType.valueOf(disguiseName.toUpperCase());
            Disguise disguise = null;
            switch (disguiseType) {
                case PLAYER:
                    PlayerDisguise playerDisguise = new PlayerDisguise(configuration.getString("name"));
                    String skin = configuration.getString("skin");
                    if (skin != null) {
                        playerDisguise.setSkin(skin);
                    }
                    disguise = playerDisguise;
                    break;
                default:
                    disguise = new MobDisguise(disguiseType);
            }
            DisguiseAPI.disguiseEntity(entity, disguise);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
