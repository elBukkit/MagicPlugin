package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;

public class EquationVolumeAction extends VolumeAction
{
    private EquationTransform equation;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        equation = EquationStore.getInstance().getTransform(parameters.getString("equation"), "x", "y", "z");
    }

    @Override
    protected boolean containsPoint(int x, int y, int z)
    {
        double value = equation.get(x, y, z);
        return value > 0;
    }
}
