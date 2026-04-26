package com.elmakers.mine.bukkit.utility.platform.base_v1_17_0;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonElement;

public class PlayerProfile extends com.elmakers.mine.bukkit.utility.PlayerProfile {
    private static String CONFIG_KEY = "profile";

    private final InventoryUtilsBase inventoryUtils;
    private String profileJSON;
    private Object profileObject;

    public PlayerProfile(InventoryUtilsBase inventoryUtils, UUID uniqueId, String name, String skinURL, String profileJSON) {
        super(uniqueId, name, skinURL);
        this.inventoryUtils = inventoryUtils;
        this.profileJSON = profileJSON;
    }

    public PlayerProfile(InventoryUtilsBase inventoryUtils, SkinUtilsBase skinUtils, Player onlinePlayer) {
        super(onlinePlayer);
        this.inventoryUtils = inventoryUtils;
        Object gameProfile = null;
        JsonElement profileJson = null;
        Logger logger = skinUtils.getPlatform().getLogger();
        try {
            gameProfile = skinUtils.getProfile(onlinePlayer);
            profileJson = skinUtils.getProfileJson(gameProfile);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error serializing profile for " + onlinePlayer.getName(), ex);
        }
        this.profileJSON = skinUtils.getGson().toJson(profileJson);
        this.skinURL = skinUtils.getProfileURL(gameProfile);
    }

    public PlayerProfile(InventoryUtilsBase inventoryUtils, ConfigurationSection config) {
        super(config);
        this.profileJSON = config.getString(CONFIG_KEY);
        this.inventoryUtils = inventoryUtils;
    }

    public PlayerProfile(InventoryUtilsBase inventoryUtils, Object profileObject) {
        super(null, null, null);
        this.profileObject = profileObject;
        this.inventoryUtils = inventoryUtils;
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
        inventoryUtils.setSkullProfile(skull, profileObject);
    }

    @Override
    public void update(SkullMeta skullMeta) {
        inventoryUtils.setSkullProfile(skullMeta, profileObject);
    }
}
