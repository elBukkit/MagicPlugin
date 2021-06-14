package com.elmakers.mine.bukkit.utility.platform;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.ProfileCallback;
import com.elmakers.mine.bukkit.utility.UUIDCallback;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public interface SkinUtils {
    Gson getGson();

    String getTextureURL(String texturesJson);

    String getProfileURL(Object profile);

    Object getProfile(Player player);

    JsonElement getProfileJson(Object gameProfile) throws IllegalAccessException;

    String getOnlineSkinURL(Player player);

    String getOnlineSkinURL(String playerName);

    void fetchUUID(String playerName, UUIDCallback callback);

    void fetchProfile(String playerName, ProfileCallback callback);

    void fetchProfile(UUID uuid, ProfileCallback callback);

    Object getGameProfile(UUID uuid, String playerName, String profileJSON);
}
