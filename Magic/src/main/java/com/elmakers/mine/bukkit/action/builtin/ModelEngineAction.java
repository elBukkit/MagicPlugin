package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.integration.ModelEngine;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModelEngineAction extends BaseSpellAction
{
    private enum ActionType {
        ADD_STATE, REMOVE_STATE
    }

    private ActionType actionType;
    private String state;
    private String model;
    private boolean ignoreLerp;
    private int lerpIn;
    private int lerpOut;
    private double lerpSpeed;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        model = parameters.getString("model_id");
        state = parameters.getString("model_state");
        ignoreLerp = parameters.getBoolean("ignore_lerp", false);
        lerpIn = parameters.getInt("lerp_in", 0);
        lerpOut = parameters.getInt("lerp_out", 1);
        lerpSpeed = parameters.getDouble("lerp_speed", 1);

        String actionTypeString = parameters.getString("model_action", "add");
        if (actionTypeString != null && !actionTypeString.isEmpty()) {
            try {
                actionType = ActionType.valueOf(actionTypeString.toUpperCase());
            } catch (Exception ex) {
                context.getLogger().warning("Invalid model_action in ModelEngine action: " + actionTypeString);
            }
        } else {
            context.getLogger().warning("Missing required model_action in ModelEngine action");
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        MageController controller = context.getController();
        ModelEngine modelEngine = controller.getModelEngine();
        if (modelEngine == null) {
            context.getMage().sendMessage("This spell requires ModelEngine");
            return SpellResult.FAIL;
        }
        Entity target = context.getTargetEntity();
        boolean result = false;
        switch (actionType) {
            case ADD_STATE:
                result = modelEngine.addModelState(target, model, state, lerpIn, lerpOut, lerpSpeed);
                break;
            case REMOVE_STATE:
                result = modelEngine.removeModelState(target, model, state, ignoreLerp);
                break;
            default:
        }

        return result ? SpellResult.CAST : SpellResult.NO_TARGET;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
