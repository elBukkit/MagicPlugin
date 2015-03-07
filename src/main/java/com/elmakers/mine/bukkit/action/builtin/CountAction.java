package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.TreeMap;

public class CountAction extends BaseSpellAction
{
	private Map<String, Integer> counts = new TreeMap<String, Integer>();
	private int totalCount = 0;

	@Override
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getTargetEntity();
		String typeString = entity.getType().toString().toLowerCase();
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
			sender.sendMessage(ChatColor.DARK_AQUA + "Found " + ChatColor.AQUA + totalCount + ChatColor.DARK_AQUA + " entities in the area");
			for (Map.Entry<String, Integer> entry : counts.entrySet())
			{
				sender.sendMessage(ChatColor.AQUA + entry.getKey() + ChatColor.WHITE + ": " + ChatColor.GOLD + entry.getValue());
			}
		}

		totalCount = 0;
		counts.clear();
	}

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		totalCount = 0;
		counts.clear();
	}

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
