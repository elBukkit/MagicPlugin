package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.ActionHandlerContext;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MultiplyAction extends CompoundAction
{
    private List<ActionHandlerContext> remaining = null;
    private List<ActionHandler> multiplied = null;
    private int multiply;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        multiply = parameters.getInt("multiply", parameters.getInt("repeat", 2));
        multiplied = new ArrayList<>();
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        ActionHandler base = getHandler("actions");
        if (base != null)
        {
            ConfigurationSection initialParameters = ConfigurationUtils.getConfigurationSection(parameters, "first");
            if (initialParameters != null)
            {
                ConfigurationSection combined = ConfigurationUtils.addConfigurations(new MemoryConfiguration(), parameters);
                initialParameters = ConfigurationUtils.addConfigurations(combined, initialParameters);
            }
            for (int i = 0; i < multiply; i++)
            {
                ActionHandler handler = (ActionHandler)base.clone();
                if (i == 0 && initialParameters != null)
                {
                    handler.prepare(context, initialParameters);
                }
                else
                {
                    handler.prepare(context, parameters);
                }
                multiplied.add(handler);
            }
        }
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        remaining = new ArrayList<>();
        for (ActionHandler handler : multiplied) {
            remaining.add(new ActionHandlerContext(handler, context));
            handler.reset(context);
        }
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        for (ActionHandler handler : multiplied) {
            handler.finish(context);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        SpellResult result = SpellResult.NO_ACTION;
        if (remaining.size() == 0) return result;

        int startingWork = context.getWorkAllowed();
        List<ActionHandlerContext> subActions = new ArrayList<>(remaining);
        remaining.clear();
        context.setWorkAllowed(0);
        int splitWork = Math.max(1, startingWork / subActions.size());
        for (ActionHandlerContext action : subActions) {
            context.setWorkAllowed(context.getWorkAllowed() + splitWork);
            SpellResult actionResult = action.perform();
            context.addResult(actionResult);
            if (actionResult.isStop()) {
                remaining.add(action);
            }
            result = result.min(actionResult);
        }

        return result;
    }
}
