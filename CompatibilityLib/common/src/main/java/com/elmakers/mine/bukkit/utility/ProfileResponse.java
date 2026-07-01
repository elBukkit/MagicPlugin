package com.elmakers.mine.bukkit.utility;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public class ProfileResponse {
    private final SkinUtils skinUtils;
    private final PlayerProfile playerProfile;

    public ProfileResponse(SkinUtils skinUtils, ConfigurationSection configuration) {
        this.skinUtils = skinUtils;
        this.playerProfile = skinUtils.parsePlayerProfile(configuration);
    }

    public ProfileResponse(SkinUtils skinUtils, PlayerProfile playerProfile) {
        this.skinUtils = skinUtils;
        this.playerProfile = playerProfile;
    }

    public void save(ConfigurationSection configuration) {
        playerProfile.save(configuration);
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }
}
