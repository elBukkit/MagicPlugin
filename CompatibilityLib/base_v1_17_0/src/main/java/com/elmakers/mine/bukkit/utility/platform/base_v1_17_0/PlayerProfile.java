package com.elmakers.mine.bukkit.utility.platform.base_v1_17_0;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.google.gson.JsonElement;

public class PlayerProfile extends com.elmakers.mine.bukkit.utility.PlayerProfile {
    private static String CONFIG_KEY = "profile";

    private String profileJSON;
    private Object profileObject;

    public PlayerProfile(Platform platform, UUID uniqueId, String name, String skinURL, String profileJSON) {
        super(platform, uniqueId, name, skinURL);
        this.profileJSON = profileJSON;
    }

    public PlayerProfile(Platform platform, ConfigurationSection config) {
        super(platform, config);
        this.profileJSON = config.getString(CONFIG_KEY);
    }

    public PlayerProfile(Platform platform, Player onlinePlayer) {
        super(platform, onlinePlayer);
        Logger logger = platform.getLogger();
        try {
            SkinUtilsBase skinUtils = (SkinUtilsBase)platform.getSkinUtils();
            Object gameProfile = skinUtils.getProfile(onlinePlayer);
            loadProfile(gameProfile, onlinePlayer.getName());
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error serializing profile for " + onlinePlayer.getName(), ex);
        }
    }

    public PlayerProfile(Platform platform, Object gameProfile, String playerName) {
        super(platform, null, playerName, null);
        loadProfile(gameProfile, playerName);
    }

    private void loadProfile(Object gameProfile, String playerName) {
        SkinUtilsBase skinUtils = (SkinUtilsBase)platform.getSkinUtils();
        this.profileObject = gameProfile;
        JsonElement profileJson = null;
        Logger logger = skinUtils.getPlatform().getLogger();
        try {
            profileJson = skinUtils.getProfileJson(gameProfile);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading player profile for " + playerName, ex);
        }
        this.profileJSON = skinUtils.getGson().toJson(profileJson);
        this.skinURL = skinUtils.getProfileURL(gameProfile);
    }

    @Override
    public void save(ConfigurationSection config) {
        super.save(config);
        config.set(CONFIG_KEY, profileJSON);
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getDisguiseFormat() {
        return profileJSON;
    }

    @Override
    public PlayerProfile update() throws ExecutionException, InterruptedException {
        return this;
    }

    @Override
    public void update(Skull skull) {
        if (profileObject != null) {
            InventoryUtilsBase inventoryUtils = (InventoryUtilsBase)platform.getInventoryUtils();
            inventoryUtils.setSkullProfile(skull, profileObject);
        }
    }

    @Override
    public void update(SkullMeta skullMeta) {
        if (profileObject != null) {
            InventoryUtilsBase inventoryUtils = (InventoryUtilsBase)platform.getInventoryUtils();
            inventoryUtils.setSkullProfile(skullMeta, profileObject);
        }
    }
}
