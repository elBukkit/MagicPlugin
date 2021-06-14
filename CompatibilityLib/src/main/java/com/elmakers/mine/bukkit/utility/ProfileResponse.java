package com.elmakers.mine.bukkit.utility;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.google.gson.JsonElement;

public class ProfileResponse {
    private final UUID uuid;
    private final String playerName;
    private final String skinURL;
    private final String profileJSON;

    public ProfileResponse(UUID uuid, String playerName, String skinURL, String profileJSON) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.skinURL = skinURL;
        this.profileJSON = profileJSON;
    }

    public ProfileResponse(ConfigurationSection configuration) {
        this.uuid = UUID.fromString(configuration.getString("uuid"));
        this.playerName = configuration.getString("name");
        this.skinURL = configuration.getString("skin");
        this.profileJSON = configuration.getString("profile");
    }

    public ProfileResponse(Player onlinePlayer) {
        this.uuid = onlinePlayer.getUniqueId();
        Object gameProfile = null;
        JsonElement profileJson = null;
        try {
            gameProfile = CompatibilityLib.getSkinUtils().getProfile(onlinePlayer);
            profileJson = CompatibilityLib.getSkinUtils().getProfileJson(gameProfile);
        } catch (Exception ex) {
            CompatibilityLib.getLogger().log(Level.WARNING, "Error serializing profile for " + onlinePlayer.getName(), ex);
        }
        this.profileJSON = CompatibilityLib.getSkinUtils().getGson().toJson(profileJson);
        this.skinURL = CompatibilityLib.getSkinUtils().getProfileURL(gameProfile);
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
        return CompatibilityLib.getSkinUtils().getGameProfile(uuid, playerName, profileJSON);
    }
}
