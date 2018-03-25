package com.elmakers.mine.bukkit.action.builtin;

public class SphereAction extends VolumeAction
{
    protected double innerRadiusSquared;

    @Override
    protected int getStartRadius() {
        innerRadiusSquared = (radius - thickness + radiusPadding) * (radius - thickness + radiusPadding);
        return 0;
    }

    @Override
    protected boolean containsPoint(int x, int y, int z)
    {
        double distanceSquared = (((double)x * (double)x) + ((double)y * (double)y) + ((double)z * (double)z));
        if (thickness > 0) {
            return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared;
        }
        return distanceSquared <= radiusSquared;
    }
}
