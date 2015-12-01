package com.elmakers.mine.bukkit.action.builtin;

public class SphereAction extends VolumeAction
{
	protected double innerRadiusSquared;

	@Override
	protected int getStartRadius() {
		innerRadiusSquared = (radius - thickness) * (radius - thickness);
		return 0;
	}

	protected boolean containsPoint(int x, int y, int z)
	{
		double fx = (double)Math.abs(x) - 0.25;
		double fy = (double)Math.abs(y) - 0.25;
		double fz = (double)Math.abs(z) - 0.25;
		double distanceSquared = ((fx * fx) + (fy * fy) + (fz * fz));
		if (thickness > 0) {
			return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared;
		}
		return distanceSquared <= radiusSquared;
	}
}
