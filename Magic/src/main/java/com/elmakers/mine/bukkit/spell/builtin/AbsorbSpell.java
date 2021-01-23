package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.builtin.AbsorbAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BrushSpell;

@Deprecated
public class AbsorbSpell extends BrushSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        target();
        ActionHandler handler = new ActionHandler();
        handler.loadAction(new AbsorbAction());
        handler.initialize(this, parameters);
        return handler.cast(getCurrentCast(), parameters);
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
