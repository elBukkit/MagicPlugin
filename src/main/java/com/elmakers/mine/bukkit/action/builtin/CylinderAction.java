package com.elmakers.mine.bukkit.action.builtin;

public class CylinderAction extends VolumeAction
{
	protected double innerRadiusSquared;

	@Override
	protected boolean containsPoint(int x, int y, int z)
	{
		double fx = (double)Math.abs(x) - 0.25;
		double fz = (double)Math.abs(z) - 0.25;
		double distanceSquared = (int)((fx * fx) + (fz * fz));
		if (thickness > 0) {
			return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared;
		}
		return distanceSquared <= radiusSquared;
	}

	@Override
	protected int getStartRadius() {
		innerRadiusSquared = (radius - thickness) * (radius - thickness);

		if (thickness > 0) {
			return (int)Math.floor(radius - thickness);
		}

		return 0;
	}
}
