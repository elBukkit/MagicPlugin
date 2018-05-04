package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;

public class PlaneAction extends VolumeAction {
    protected enum Axis { X, Y, Z }

    protected Axis axis;
    protected Axis brushAxis;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        axis = parseAxis(parameters.getString("axis"), Axis.Y);
        brushAxis = parseAxis(parameters.getString("brush_axis"), axis);

        super.prepare(context, parameters);
    }

    protected Axis parseAxis(String axisType, Axis defaultAxis) {
        if (axisType != null) {
            if (axisType.equalsIgnoreCase("x")) {
                return Axis.X;
            } else if (axisType.equalsIgnoreCase("z")) {
                return Axis.Z;
            } else {
                return Axis.Y;
            }
        }

        return defaultAxis;
    }

    @Override
    protected boolean calculateSize(CastContext context) {
        switch (axis)
        {
            case X:
                xSize = 0;
                break;
            case Z:
                zSize = 0;
                break;
            default:
                ySize = 0;
                break;
        }
        return super.calculateSize(context);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        MaterialBrush brush = context.getBrush();
        Location targetLocation = context.getTargetLocation();
        if (targetLocation == null) return;

        Location orientLocation = targetLocation.clone();
        switch (brushAxis) {
            case X:
                orientLocation.setY(orientLocation.getY() + 1);
                orientLocation.setZ(orientLocation.getZ() + 1);
                break;
            case Z:
                orientLocation.setX(orientLocation.getX() + 1);
                orientLocation.setY(orientLocation.getY() + 1);
                break;
            default:
                orientLocation.setX(orientLocation.getX() + 1);
                orientLocation.setZ(orientLocation.getZ() + 1);
                break;
        }
        brush.setTarget(orientLocation, targetLocation);
    }

    @Override
    protected boolean containsPoint(int x, int y, int z)
    {
        return true;
    }
}
