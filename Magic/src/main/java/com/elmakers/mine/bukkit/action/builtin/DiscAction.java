package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;

public class DiscAction extends PlaneAction
{
    protected double innerRadiusSquared;

    @Override
    protected boolean containsPoint(CastContext context, int y, int z, int x)
    {
        double distanceSquared;
        switch (axis)
        {
            case X:
                distanceSquared = (y * y) + (z * z);
                break;
            case Z:
                distanceSquared = (x * x) + (y * y);
                break;
            default:
                distanceSquared = (x * x) + (z * z);
        }

        if (thickness > 0) {
            return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared;
        }
        return distanceSquared <= radiusSquared;
    }

    @Override
    protected int getStartRadius() {
        innerRadiusSquared = (radius - thickness + radiusPadding) * (radius - thickness + radiusPadding);

        if (thickness > 0) {
            return (int)Math.floor(radius - thickness - radiusPadding);
        }

        return 0;
    }
}
