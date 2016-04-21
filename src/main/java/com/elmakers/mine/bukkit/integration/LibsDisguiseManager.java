package com.elmakers.mine.bukkit.integration;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class LibsDisguiseManager {
    private final Plugin plugin;
    private final Plugin disguisePlugin;

    public LibsDisguiseManager(Plugin owningPlugin, Plugin disguisePlugin) {
        this.plugin = owningPlugin;
        this.disguisePlugin = disguisePlugin;
    }

    public boolean initialize() {
        return (disguisePlugin != null && disguisePlugin instanceof LibsDisguises);
    }

    public boolean isDisguised(Entity entity) {
        return DisguiseAPI.isDisguised(entity);
    }
    
    public boolean disguise(Entity entity, ConfigurationSection configuration) {
        String disguiseName = configuration.getString("type");
        if (disguiseName == null || disguiseName.isEmpty()) {
            return false;
        }
        try {
            DisguiseType disguiseType = DisguiseType.valueOf(disguiseName.toUpperCase());
            Disguise disguise = null;
            switch (disguiseType) {
                case PLAYER:
                    PlayerDisguise playerDisguise = new PlayerDisguise(configuration.getString("player"));
                    String skin = configuration.getString("skin");
                    String uuid = configuration.getString("uuid");
                    if (skin != null && uuid != null) {
                        WrappedGameProfile profile = new WrappedGameProfile(skin, uuid);
                        playerDisguise.setSkin(profile);
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
