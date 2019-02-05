package com.elmakers.mine.bukkit.action;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public abstract class BaseSpellAction implements SpellAction
{
    private boolean requiresTarget;
    private boolean requiresTargetEntity;
    private boolean ignoreResult;

    @Override
    public void load(Mage mage, ConfigurationSection data)
    {

    }

    @Override
    public void save(Mage mage, ConfigurationSection data)
    {

    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection actionParameters) {
        requiresTarget = actionParameters.getBoolean("requires_target", false);
        requiresTargetEntity = actionParameters.getBoolean("requires_entity_target", false);
        ignoreResult = actionParameters.getBoolean("ignore_result", false);
    }

    @Override
    public void finish(CastContext context) {

    }

    @Override
    public boolean requiresTarget()
    {
        return requiresTarget;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return requiresTargetEntity;
    }

    @Override
    public boolean usesBrush()
    {
        return false;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresBuildPermission()
    {
        return false;
    }

    @Override
    public boolean requiresBreakPermission()
    {
        return false;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        parameters.add("requires_target");
        parameters.add("requires_entity_target");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("requires_target") || parameterKey.equals("requires_entity_target"))
        {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        }
    }

    @Override
    public String transformMessage(String message) {
        return message;
    }

    @Override
    public int getActionCount() {
        return 1;
    }

    @Override
    @Nullable
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            return null;
        }
    }

    @Override
    public void reset(CastContext context)
    {

    }

    @Override
    public boolean ignoreResult()
    {
        return ignoreResult;
    }
}
