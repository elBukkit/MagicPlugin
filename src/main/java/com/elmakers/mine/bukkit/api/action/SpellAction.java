package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

public interface SpellAction extends Cloneable
{
    public SpellResult perform(CastContext context);
    public void initialize(ConfigurationSection baseParameters);
    public void prepare(CastContext context, ConfigurationSection parameters);
    public void finish(CastContext context);
    public void reset(CastContext context);
    public void getParameterNames(Collection<String> parameters);
    public void getParameterOptions(Collection<String> examples, String parameterKey);
    public boolean usesBrush();
    public boolean isUndoable();
    public boolean requiresBuildPermission();
    public boolean requiresTarget();
    public boolean requiresTargetEntity();
    public String transformMessage(String message);
    public void load(Mage mage, ConfigurationSection data);
    public void save(Mage mage, ConfigurationSection data);
    public int getActionCount();
    public Object clone();
}
