package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

public interface GeneralAction extends SpellAction
{
    public SpellResult perform(ConfigurationSection parameters);
}
