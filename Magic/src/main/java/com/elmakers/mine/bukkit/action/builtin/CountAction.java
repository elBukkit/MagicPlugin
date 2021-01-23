package com.elmakers.mine.bukkit.action.builtin;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CountAction extends BaseSpellAction
{
    private Map<String, Integer> counts = new TreeMap<>();
    private int totalCount = 0;

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        String typeString = "";
        if (entity == null)
        {
            Block block = context.getTargetBlock();
            typeString = block.getType().toString().toLowerCase();
        }
        else
        {
            typeString = entity.getType().toString().toLowerCase();
        }
        Integer count = counts.get(typeString);
        count = (count == null) ? 1 : count + 1;
        counts.put(typeString, count);
        totalCount++;

        return SpellResult.CAST;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        CommandSender sender = context.getMage().getCommandSender();
        if (sender != null)
        {
            sender.sendMessage(ChatColor.DARK_AQUA + "Found " + ChatColor.AQUA + totalCount + ChatColor.DARK_AQUA + " targets in the area");
            for (Map.Entry<String, Integer> entry : counts.entrySet())
            {
                sender.sendMessage(ChatColor.AQUA + entry.getKey() + ChatColor.WHITE + ": " + ChatColor.GOLD + entry.getValue());
            }
        }

        totalCount = 0;
        counts.clear();
    }

    @Override
    public void start(CastContext context, ConfigurationSection parameters) {
        super.start(context, parameters);
        totalCount = 0;
        counts.clear();
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
