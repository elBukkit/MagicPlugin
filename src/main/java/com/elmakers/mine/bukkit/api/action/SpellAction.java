package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.configuration.ConfigurationSection;

public interface SpellAction
{
    public void load(Spell spell, ConfigurationSection parameters);
    public ConfigurationSection getParameters(ConfigurationSection baseParameters);
}
