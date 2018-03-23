package com.elmakers.mine.bukkit.api.action;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public interface SpellAction extends Cloneable
{
    SpellResult perform(CastContext context);
    void initialize(Spell spell, ConfigurationSection baseParameters);
    void prepare(CastContext context, ConfigurationSection parameters);
    void finish(CastContext context);
    void reset(CastContext context);
    void getParameterNames(Spell spell, Collection<String> parameters);
    void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples);
    boolean usesBrush();
    boolean isUndoable();
    boolean requiresBuildPermission();
    boolean requiresBreakPermission();
    boolean requiresTarget();
    boolean requiresTargetEntity();
    boolean ignoreResult();
    String transformMessage(String message);
    void load(Mage mage, ConfigurationSection data);
    void save(Mage mage, ConfigurationSection data);
    int getActionCount();
    Object clone();
}
