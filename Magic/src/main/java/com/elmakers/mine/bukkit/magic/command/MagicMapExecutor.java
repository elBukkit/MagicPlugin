package com.elmakers.mine.bukkit.magic.command;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.maps.URLMap;

public abstract class MagicMapExecutor extends MagicTabExecutor {
    public MagicMapExecutor(MagicAPI api, String command) {
        super(api, command);
    }

    protected void onMapList(CommandSender sender, String keyword)
    {
        Pattern pattern = null;
        boolean positive = true;
        if (!keyword.isEmpty()) {
            if (keyword.startsWith("-")) {
                keyword = keyword.substring(1);
                positive = false;
            }
            pattern = Pattern.compile(keyword);
        }
        int shown = 0;
        boolean limited = false;
        List<URLMap> maps = api.getController().getMaps().getAll();
        Collections.reverse(maps);
        for (URLMap map : maps) {
            int mapId = map.getId();
            String source = map.getName() + " " + map.getURL() + " " + map.getId();
            Matcher matcher = pattern == null ? null : pattern.matcher(source);
            if (matcher == null || matcher.find() == positive) {
                shown++;
                String name = map.getName();
                name = (name == null ? "(None)" : name);
                sender.sendMessage(ChatColor.AQUA + "" + mapId + ChatColor.WHITE + ": "
                        + name + " => " + ChatColor.GRAY + map.getURL());
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
