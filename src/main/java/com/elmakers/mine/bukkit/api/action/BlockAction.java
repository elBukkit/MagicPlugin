package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public interface BlockAction extends SpellAction
{
    public SpellResult perform(ConfigurationSection parameters, Block block);
}
