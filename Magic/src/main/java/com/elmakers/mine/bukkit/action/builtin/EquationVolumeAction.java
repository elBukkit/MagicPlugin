package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;

public class EquationVolumeAction extends VolumeAction
{
    private static class BrushPair implements Comparable<BrushPair> {
        public String brush;
        public double value;

        public BrushPair(String brush, double value) {
            this.brush = brush;
            this.value = value;
        }

        @Override
        public int compareTo(BrushPair o) {
            return Double.compare(value, o.value);
        }
    }

    private EquationTransform equation;
    private List<BrushPair> brushes;
    private String brushMod;
    private MaterialBrush originalBrush;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        equation = EquationStore.getInstance().getTransform(parameters.getString("equation"), "x", "y", "z");
        brushMod = parameters.getString("brushmod");
        ConfigurationSection brushConfig = ConfigurationUtils.getConfigurationSection(parameters, "brushes");
        if (brushConfig != null) {
            Set<String> brushKeys = brushConfig.getKeys(false);
            if (!brushKeys.isEmpty()) {
                brushes = new ArrayList<>();
                for (String key : brushKeys) {
                    try {
                        double value = Double.parseDouble(key);
                        brushes.add(new BrushPair(brushConfig.getString(key), value));
                    } catch (Exception ex) {
                        context.getLogger().warning("Spell " + context.getSpell().getKey() + ": Brush keys should be numbers");
                    }
                }
                Collections.sort(brushes);
            }
        }
    }

    @Override
    protected boolean containsPoint(CastContext context, int y, int z, int x)
    {
        double value = equation.get(x, y, z);
        if (brushes == null || actionContext == null) {
            return value > 0;
        }
        String brushKey = "none";
        for (BrushPair pair : brushes) {
            if (value < pair.value) break;
            brushKey = pair.brush;
        }
        if (brushKey.equalsIgnoreCase("none")) {
            return false;
        }
        if (originalBrush == null) {
            originalBrush = actionContext.getBrush();
        }

        MaterialBrush brush = null;
        if (brushKey.equalsIgnoreCase("original")) {
            brush = originalBrush;
        } else {
            if (brushMod != null) {
                brush = new com.elmakers.mine.bukkit.block.MaterialBrush(context.getMage(), context.getLocation(), brushMod);
                brush.update(brushKey);
            } else {
                brush = new com.elmakers.mine.bukkit.block.MaterialBrush(context.getMage(), context.getLocation(), brushKey);
            }
        }
        actionContext.setBrush(brush);
        return true;
    }
}
