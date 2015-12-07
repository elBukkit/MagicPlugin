package com.elmakers.mine.bukkit.action.builtin;

public class CylinderAction extends VolumeAction
{
	protected double innerRadiusSquared;

	@Override
	protected boolean containsPoint(int x, int y, int z)
	{
		double distanceSquared = ((double)x * (double)x) + ((double)z * (double)z);
		if (thickness > 0) {
			return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared;
		}
		return distanceSquared <= radiusSquared;
	}

	@Override
	protected int getStartRadius() {
		innerRadiusSquared = (radius - thickness + radiusPadding) * (radius - thickness + radiusPadding);

		if (thickness > 0) {
			return (int)Math.floor(radius - thickness + radiusPadding);
		}

		return 0;
	}
}
