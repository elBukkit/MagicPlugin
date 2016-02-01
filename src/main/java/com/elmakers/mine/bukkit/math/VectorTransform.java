package com.elmakers.mine.bukkit.math;

import com.elmakers.mine.bukkit.api.math.Transform;
import de.slikey.effectlib.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class VectorTransform {
    private Transform xTransform;
    private Transform yTransform;
    private Transform zTransform;
    private boolean orient;

    public VectorTransform(ConfigurationSection configuration) {
        xTransform = Transforms.loadTransform(configuration, "x");
        yTransform = Transforms.loadTransform(configuration, "y");
        zTransform = Transforms.loadTransform(configuration, "z");
        orient = configuration.getBoolean("orient", true);
    }

    public Vector get(Location source, double t) {
        // This returns a unit vector with the new direction calculated via the equations
        Double xValue = xTransform.get(t);
        Double yValue = yTransform.get(t);
        Double zValue = zTransform.get(t);

        Vector result = new Vector(xValue, yValue, zValue);

        // Rotates to player's direction
        if (orient && source != null)
        {
            result = VectorUtils.rotateVector(result, source);
        }

        return result;
    }
}
