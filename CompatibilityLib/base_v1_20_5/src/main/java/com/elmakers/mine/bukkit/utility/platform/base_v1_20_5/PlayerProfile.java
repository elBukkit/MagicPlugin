package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class PlayerProfile extends com.elmakers.mine.bukkit.utility.PlayerProfile {
    private static Gson gson;
    private static String CONFIG_KEY = "data";

    protected static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    private org.bukkit.profile.PlayerProfile playerProfile;

    public PlayerProfile(org.bukkit.profile.PlayerProfile profile) {
        super(profile.getUniqueId(), profile.getName(), profile.getTextures().getSkin() == null ? null : profile.getTextures().getSkin().toString());
        this.playerProfile = profile;
    }

    public PlayerProfile(OfflinePlayer player) {
        this(player.getPlayerProfile());
    }

    public PlayerProfile(ConfigurationSection configuration) {
        super(configuration);
        playerProfile = configuration.getSerializable(CONFIG_KEY, org.bukkit.profile.PlayerProfile.class);
        if (playerProfile != null) {
            name = playerProfile.getName();
            uniqueId = playerProfile.getUniqueId();
            URL skinURL = playerProfile == null ? null : playerProfile.getTextures().getSkin();
            this.skinURL = skinURL == null ? null : skinURL.toString();
        }
    }

    @Override
    public void save(ConfigurationSection configuration) {
        super.save(configuration);
        configuration.set(CONFIG_KEY, playerProfile);
    }

    @Override
    public boolean isComplete() {
        // The Spigot implementation of this looks correct, but when running on Paper
        // it seems to do something different and will return true for an unloaded profile.
        // return playerProfile.isComplete()
        return playerProfile != null && playerProfile.getUniqueId() != null
                && playerProfile.getName() != null && !playerProfile.getTextures().isEmpty();
    }

    @Override
    public PlayerProfile update() throws ExecutionException, InterruptedException {
        return new PlayerProfile(playerProfile.update().get());
    }

    @Override
    public void update(Skull skull) {
        skull.setOwnerProfile(playerProfile);
    }

    @Override
    public void update(SkullMeta skullMeta) {
        skullMeta.setOwnerProfile(playerProfile);
    }

    @Override
    public String getDisguiseFormat() {
        Gson gson = getGson();
        String bukkitFormat = gson.toJson(playerProfile);
        Type objectMapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> bukkitMap = gson.fromJson(bukkitFormat, objectMapType);
        Map<String, Object> disguiseMap = new HashMap<>();
        disguiseMap.put("uuid", playerProfile.getUniqueId());
        disguiseMap.put("name", playerProfile.getName());
        disguiseMap.put("textureProperties", bukkitMap.get("properties"));
        return gson.toJson(disguiseMap);
    }
}
