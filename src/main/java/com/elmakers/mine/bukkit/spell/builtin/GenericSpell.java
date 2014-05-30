package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.configuration.ConfigurationSection;

public class GenericSpell extends TargetingSpell
{
    @Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
        if (!target.hasTarget()) {
            return SpellResult.NO_TARGET;
        }

        return SpellResult.CAST;
	}
}
