package com.elmakers.mine.bukkit.utility;

import java.net.URL;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.profile.PlayerProfile;

public class ProfileResponse {
    private final PlayerProfile playerProfile;
    private final UUID uuid;
    private final String playerName;
    private final String skinURL;

    public ProfileResponse(ConfigurationSection configuration) {
        this.uuid = UUID.fromString(configuration.getString("uuid"));
        this.playerName = configuration.getString("name");
        this.skinURL = configuration.getString("skin");
        this.playerProfile = configuration.getSerializable("data", PlayerProfile.class);
    }

    public ProfileResponse(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
        this.uuid = playerProfile.getUniqueId();
        this.playerName = playerProfile.getName();
        URL skinURL = playerProfile.getTextures().getSkin();
        this.skinURL = skinURL == null ? null : skinURL.toString();
    }

    public void save(ConfigurationSection configuration) {
        configuration.set("data", playerProfile);
        configuration.set("uuid", uuid.toString());
        configuration.set("skin", skinURL);
        configuration.set("name", playerName);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getSkinURL() {
        return skinURL;
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }
}
