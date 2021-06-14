package com.elmakers.mine.bukkit.utility;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.google.gson.JsonElement;

public class ProfileResponse {
    private final SkinUtils skinUtils;
    private final UUID uuid;
    private final String playerName;
    private final String skinURL;
    private final String profileJSON;

    public ProfileResponse(SkinUtils skinUtils, UUID uuid, String playerName, String skinURL, String profileJSON) {
        this.skinUtils = skinUtils;
        this.uuid = uuid;
        this.playerName = playerName;
        this.skinURL = skinURL;
        this.profileJSON = profileJSON;
    }

    public ProfileResponse(SkinUtils skinUtils, ConfigurationSection configuration) {
        this.skinUtils = skinUtils;
        this.uuid = UUID.fromString(configuration.getString("uuid"));
        this.playerName = configuration.getString("name");
        this.skinURL = configuration.getString("skin");
        this.profileJSON = configuration.getString("profile");
    }

    public ProfileResponse(SkinUtils skinUtils, Logger logger, Player onlinePlayer) {
        this.skinUtils = skinUtils;
        this.uuid = onlinePlayer.getUniqueId();
        Object gameProfile = null;
        JsonElement profileJson = null;
        try {
            gameProfile = skinUtils.getProfile(onlinePlayer);
            profileJson = skinUtils.getProfileJson(gameProfile);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error serializing profile for " + onlinePlayer.getName(), ex);
        }
        this.profileJSON = skinUtils.getGson().toJson(profileJson);
        this.skinURL = skinUtils.getProfileURL(gameProfile);
        this.playerName = onlinePlayer.getName();
    }

    public void save(ConfigurationSection configuration) {
        configuration.set("uuid", uuid.toString());
        configuration.set("skin", skinURL);
        configuration.set("profile", profileJSON);
        configuration.set("name", playerName);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getSkinURL() {
        return skinURL;
    }

    public String getProfileJSON() {
        return profileJSON;
    }

    public Object getGameProfile() {
        return skinUtils.getGameProfile(uuid, playerName, profileJSON);
    }
}
