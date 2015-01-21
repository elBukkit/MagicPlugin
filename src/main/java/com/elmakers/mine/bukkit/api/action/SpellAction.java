package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

public interface SpellAction
{
    public void prepare(ConfigurationSection parameters);
    public void load(Spell spell, ConfigurationSection parameters);
    public ConfigurationSection getParameters(ConfigurationSection baseParameters);
    public void getParameterNames(Collection<String> parameters);
    public void getParameterOptions(Collection<String> examples, String parameterKey);
    public boolean usesBrush();
    public boolean isUndoable();
}
