package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;
import de.slikey.effectlib.util.VectorUtils;

public class EquationAction extends CompoundAction
{
    // Parameters
    private EquationTransform xTransform;
    private EquationTransform yTransform;
    private EquationTransform zTransform;

    private EquationTransform x2Transform;
    private EquationTransform y2Transform;
    private EquationTransform z2Transform;

    private boolean orient;
    private boolean orientPitch;
    private boolean reorient;
    private boolean useTargetLocation;
    private int iterations;
    private int iterations2;

    public String variable = "t";
    public String variable2 = "t2";

    // State
    private int iteration;
    private int iteration2;
    private Vector direction;
    private Location startLocation;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        reorient = parameters.getBoolean("reorient", false);
        orient = parameters.getBoolean("orient", false);
        orientPitch = parameters.getBoolean("orient_pitch", true);
        useTargetLocation = parameters.getBoolean("use_target_location", true);
        iterations = parameters.getInt("iterations", 1);
        iterations2 = parameters.getInt("iterations2", 0);

        variable = parameters.getString("variable", "t");
        variable2 = parameters.getString("variable2", "t");

        String xEquation = parameters.getString("x_equation");
        String yEquation = parameters.getString("y_equation");
        String zEquation = parameters.getString("z_equation");

        xTransform = EquationStore.getInstance().getTransform(xEquation, variable, "i");
        yTransform = EquationStore.getInstance().getTransform(yEquation, variable, "i");
        zTransform = EquationStore.getInstance().getTransform(zEquation, variable, "i");

        String x2Equation = parameters.getString("x2_equation");
        String y2Equation = parameters.getString("y2_equation");
        String z2Equation = parameters.getString("z2_equation");

        if (x2Equation != null && y2Equation != null && z2Equation != null && iterations2 > 0) {
            x2Transform = EquationStore.getInstance().getTransform(x2Equation, variable, variable2, "i", "i2");
            y2Transform = EquationStore.getInstance().getTransform(y2Equation, variable, variable2, "i", "i2");
            z2Transform = EquationStore.getInstance().getTransform(z2Equation, variable, variable2, "i", "i2");
        }
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        iteration = 0;
        iteration2 = 0;

        startLocation = context.getEyeLocation();
        if (startLocation == null) return;

        Location targetLocation = context.getTargetLocation();
        if (!useTargetLocation || targetLocation == null || targetLocation.equals(startLocation)) {
            direction = startLocation.getDirection();
        } else {
            Vector startLoc = startLocation.toVector();
            Vector targetLoc = new Vector(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
            startLocation = targetLocation;
            direction = targetLoc.clone();
            direction.subtract(startLoc);
            direction.normalize();
        }
    }

    @Override
    public SpellResult step(CastContext context) {
        if (startLocation == null) {
            return SpellResult.NO_TARGET;
        }
        context.addWork(100);
        context.playEffects("iterate");
        return startActions();
    }

    @Override
    public boolean next(CastContext context) {
        if (startLocation == null) {
            return false;
        }
        Location target = startLocation.clone();
        Double xValue = xTransform.get(iteration, iterations);
        Double yValue = yTransform.get(iteration, iterations);
        Double zValue = zTransform.get(iteration, iterations);

        Vector result = new Vector(xValue, yValue, zValue);
        Location location = context.getLocation();
        if (!reorient && direction != null) {
            location.setDirection(direction);
        }
        if (orient && orientPitch) {
            result = VectorUtils.rotateVector(result, location);
        } else if (orient) {
            result = VectorUtils.rotateVector(result, location.getYaw(), 0);
        }

        target.add(result);
        if (x2Transform != null && y2Transform != null && z2Transform != null) {
            Double x2Value = x2Transform.get(iteration, iteration2, iterations2, iterations2);
            Double y2Value = y2Transform.get(iteration, iteration2, iterations2, iterations2);
            Double z2Value = z2Transform.get(iteration, iteration2, iterations2, iterations2);

            Vector result2 = new Vector(x2Value, y2Value, z2Value);
            if (orient && orientPitch) {
                result2 = VectorUtils.rotateVector(result2, location);
            } else if (orient) {
                result2 = VectorUtils.rotateVector(result2, location.getYaw(), 0);
            }

             target.add(result2);
        }
        actionContext.setTargetLocation(target);

        iteration2++;
        if (iteration2 > iterations2) {
            iteration2 = 0;
            iteration++;
        }
        return (iteration <= iterations);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("x_equation");
        parameters.add("y_equation");
        parameters.add("z_equation");

        parameters.add("x2_equation");
        parameters.add("y2_equation");
        parameters.add("z2_equation");

        parameters.add("variable");
        parameters.add("variable2");

        parameters.add("iterations");
        parameters.add("iterations2");
        parameters.add("reorient");
        parameters.add("orient");
        parameters.add("orient_pitch");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("orient") || parameterKey.equals("reorient") || parameterKey.equals("orient_pitch")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("iterations") || parameterKey.equals("iterations2")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public int getActionCount() {
        return (iterations * (iterations2 + 1)) * super.getActionCount();
    }
}
