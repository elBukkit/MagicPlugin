package com.elmakers.mine.bukkit.utility.platform;

import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface SpigotUtils {
    void sendMessage(CommandSender sender, String message);

    boolean sendActionBar(Player player, String message);

    boolean sendActionBar(Player player, String message, String font);

    @Nullable
    String serializeBossBar(String title);

    @Nullable
    String serializeBossBar(String title, String font);

    String getHexColor(String hexCode);

    String translateColors(String message);

    String stripColor(String message);

    List<String> serializeLore(List<String> lore);
}
