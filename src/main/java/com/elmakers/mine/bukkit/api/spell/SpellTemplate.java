package com.elmakers.mine.bukkit.api.spell;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;

/**
 * A Spell template, as defined in the spells configuration files.
 * 
 * Every spell uses a specific Class that must extend from
 * com.elmakers.mine.bukkit.plugins.magic.spell.Spell. Implemeting the Spell or SpellTemplate
 * interface is not sufficient for making a custom Spell, you must link to MagicPlugin directly.
 */
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
	public Collection<EffectPlayer> getEffects(SpellResult result);
	public void getParameters(Collection<String> parameters);
	public void getParameterOptions(Collection<String> examples, String parameterKey);
	public long getDuration();
	public long getCooldown();
}
