package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class RetargetAction extends CompoundAction {
    private int range;
    private boolean useHitbox;
    private double fov;
    private double closeRange;
    private double closeFOV;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useHitbox = parameters.getBoolean("hitbox", false);
        range = parameters.getInt("range", 32);
        fov = parameters.getDouble("fov", 0.3);
        closeRange = parameters.getDouble("close_range", 1);
        closeFOV = parameters.getDouble("close_fov", 0.5);
    }

    @Override
    public SpellResult perform(CastContext context) {
        createActionContext(context);
        actionContext.addWork(range);
        actionContext.retarget(range, fov, closeRange, closeFOV, useHitbox);
        return super.perform(actionContext);
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("fov");
        parameters.add("target_count");
        parameters.add("hitbox");
        parameters.add("range");
        parameters.add("close_range");
        parameters.add("close_fov");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("hitbox")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("target_count") || parameterKey.equals("range") || parameterKey.equals("fov")
                || parameterKey.equals("close_range") || parameterKey.equals("close_fov")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
