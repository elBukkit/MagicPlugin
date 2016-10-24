package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionContext;
import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedList;
import java.util.List;

public class RandomAction extends CompoundAction
{
    private LinkedList<WeightedPair<ActionContext>> actionProbability;
    private ActionContext currentAction = null;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        mapActions();
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        if (actionProbability.size() > 0) {
            currentAction = RandomUtils.weightedRandom(actionProbability);
            currentAction.getAction().reset(context);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (currentAction == null) {
            return SpellResult.FAIL;
        }
        return currentAction.perform(context);
    }

    @Override
    public void finish(CastContext context) {
        if (currentAction != null) {
            currentAction.finish(context);
        }
    }

    protected void mapActions() {
        actionProbability = new LinkedList<>();
        ActionHandler actions = getHandler("actions");
        if (actions != null)
        {
            List<ActionContext> options = actions.getActions();
            float totalWeight = 0;
            for (ActionContext option : options)
            {
                float weight = 1;
                ConfigurationSection actionParameters = option.getActionParameters();
                if (actionParameters != null)
                {
                    weight = (float)actionParameters.getDouble("weight", weight);
                }
                totalWeight += weight;
                actionProbability.add(new WeightedPair<>(totalWeight, weight, option));
            }
        }
    }

    @Override
    public Object clone()
    {
        RandomAction action = (RandomAction)super.clone();
        action.mapActions();
        return action;
    }
}
