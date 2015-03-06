package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public abstract class BaseSpellAction implements SpellAction
{
    private boolean requiresTarget;
    private boolean requiresTargetEntity;

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
    public void initialize(ConfigurationSection actionParameters) {
        requiresTarget = actionParameters.getBoolean("requires_target", false);
        requiresTargetEntity = actionParameters.getBoolean("requires_entity_target", false);
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
    public void getParameterNames(Collection<String> parameters) {
        parameters.add("requires_target");
        parameters.add("requires_entity_target");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("requires_target") || parameterKey.equals("requires_entity_target"))
        {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        }
    }

    public String transformMessage(String message) {
        return message;
    }
}
