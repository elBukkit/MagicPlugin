package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.action.builtin.AbsorbAction;
import com.elmakers.mine.bukkit.spell.ActionHandler;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BrushSpell;

@Deprecated
public class AbsorbSpell extends BrushSpell 
{   
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		ActionHandler handler = new ActionHandler(this);
		handler.loadAction(new AbsorbAction());
		return handler.perform(parameters);
	}
	
	@Override
	public boolean hasBrushOverride() 
	{
		return true;
	}
	
	@Override
	public boolean isUndoable()
	{
		return false;
	}
}
