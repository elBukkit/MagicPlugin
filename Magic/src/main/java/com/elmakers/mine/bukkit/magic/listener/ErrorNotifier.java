package com.elmakers.mine.bukkit.magic.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ErrorNotifier implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (player.isOp()) {
            player.sendMessage(ChatColor.RED + "There are errors in your Magic configs!");
            player.sendMessage(ChatColor.RED + "Please check your logs, fix the issue and restart the server");
            player.sendMessage(ChatColor.RED + "All Magic items and features will be broken in the meantime");
        } else {
            player.sendMessage(ChatColor.YELLOW + "All magic items and abilities are temporarily disabled!");
            player.sendMessage(ChatColor.YELLOW + "Please take care not to drop or destroy your magic items");
        }
    }
}
