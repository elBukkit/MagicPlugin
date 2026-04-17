package com.elmakers.mine.bukkit.utility.platform;

import java.util.UUID;

import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.utility.PlayerProfile;
import com.elmakers.mine.bukkit.utility.ProfileCallback;

public interface SkinUtils {
    String getOnlineSkinURL(Player player);

    String getOnlineSkinURL(String playerName);

    void fetchProfile(String playerName, ProfileCallback callback);

    void fetchProfile(UUID uuid, ProfileCallback callback);

    PlayerProfile parsePlayerProfile(ConfigurationSection config);

    PlayerProfile getPlayerProfile(SkullMeta meta);

    PlayerProfile getPlayerProfile(Skull block);
}
