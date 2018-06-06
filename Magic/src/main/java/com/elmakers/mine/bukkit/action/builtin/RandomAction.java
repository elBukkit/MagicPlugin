package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.ActionContext;
import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.SpellParameters;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class RandomAction extends CompoundAction
{
    private Deque<WeightedPair<ActionContext>> actionProbability;
    private ActionContext currentAction = null;
    private MageSpell mageSpell;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        if (spell instanceof MageSpell) {
            mageSpell = (MageSpell)spell;
        }
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
        actionProbability = new ArrayDeque<>();
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
                    // To support equations
                    if (mageSpell != null) {
                        actionParameters = new SpellParameters(mageSpell, actionParameters);
                    }
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
