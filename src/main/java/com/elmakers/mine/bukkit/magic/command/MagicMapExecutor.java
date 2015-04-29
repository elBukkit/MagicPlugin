package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.maps.URLMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public abstract class MagicMapExecutor extends MagicTabExecutor {
    public MagicMapExecutor(MagicAPI api) {
        super(api);
    }

    protected void onMapList(CommandSender sender, String keyword)
    {
        int shown = 0;
        boolean limited = false;
        Collection<URLMap> maps = api.getController().getMaps().getAll();
        for (URLMap map : maps) {
            Short mapId = map.getId();
            if (map == null || mapId == null) continue;

            if (map.matches(keyword)) {
                shown++;
                String name = map.getName();
                name = (name == null ? "(None)" : name);
                sender.sendMessage(ChatColor.AQUA + "" + mapId + ChatColor.WHITE + ": " +
                        name + " => " + ChatColor.GRAY + map.getURL());
                if (shown > 100) {
                    limited = true;
                    break;
                }
            }
        }
        if (shown == 0) {
            sender.sendMessage("No maps found" + (keyword.length() > 0 ? " matching " + keyword : "") + ", use /mmap load to add more maps");
        } else if (keyword.isEmpty()) {
            if (limited) {
                sender.sendMessage("Results limited to 100, use /mmap list <keyword> to narrow your search");
            } else {
                sender.sendMessage(shown + " maps found");
            }
        } else {
            String limitedMessage = limited ? " (+ more)" : "";
            sender.sendMessage(shown + " maps found matching " + keyword + limitedMessage);
        }
    }
}
