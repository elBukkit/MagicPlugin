package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

public interface Action
{
    public SpellResult perform(ConfigurationSection parameters);
}
