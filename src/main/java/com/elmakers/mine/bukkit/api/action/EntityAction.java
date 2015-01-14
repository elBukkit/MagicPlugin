package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public interface EntityAction extends SpellAction
{
    public SpellResult perform(ConfigurationSection parameters, Entity entity);
}
