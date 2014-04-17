package com.elmakers.mine.bukkit.api.spell;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.block.MaterialAndData;

public interface SpellTemplate extends Comparable<SpellTemplate>, CostReducer {
	public String getName();
	public String getDescription();
	public String getKey();
	public String getCategory();
	public String getUsage();
	public MaterialAndData getIcon();
	public boolean hasSpellPermission(CommandSender sender);
	public Collection<CastingCost> getCosts();
	public Collection<CastingCost> getActiveCosts();
	public void getParameters(Collection<String> parameters);
	public void getParameterOptions(Collection<String> examples, String parameterKey);
}
