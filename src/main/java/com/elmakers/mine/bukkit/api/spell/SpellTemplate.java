package com.elmakers.mine.bukkit.api.spell;

import java.util.Collection;

import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;

/**
 * A Spell template, as defined in the spells configuration files.
 */
public interface SpellTemplate extends Comparable<SpellTemplate>, CostReducer {
    public String getName();
    public String getDescription();
    public String getKey();
    public Color getColor();
    public long getWorth();
    public SpellCategory getCategory();
    public long getCastCount();
    public String getUsage();
    public MaterialAndData getIcon();
    public boolean hasCastPermission(CommandSender sender);
    public Collection<CastingCost> getCosts();
    public Collection<CastingCost> getActiveCosts();
    public Collection<EffectPlayer> getEffects(SpellResult result);
    public Collection<EffectPlayer> getEffects(String effectsKey);
    public void getParameters(Collection<String> parameters);
    public void getParameterOptions(Collection<String> examples, String parameterKey);
    public long getDuration();
    public long getCooldown();
    public Spell createSpell();
    public void loadTemplate(String key, ConfigurationSection node);
}
