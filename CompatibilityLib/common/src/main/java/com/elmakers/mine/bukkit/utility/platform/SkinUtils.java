package com.elmakers.mine.bukkit.utility.platform;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.ProfileCallback;

public interface SkinUtils {
    String getOnlineSkinURL(Player player);

    String getOnlineSkinURL(String playerName);

    void fetchProfile(String playerName, ProfileCallback callback);

    void fetchProfile(UUID uuid, ProfileCallback callback);
}
