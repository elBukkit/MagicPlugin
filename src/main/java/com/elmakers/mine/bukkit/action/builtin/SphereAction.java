package com.elmakers.mine.bukkit.action.builtin;

public class SphereAction extends VolumeAction
{
	protected int innerRadiusSquared;

	@Override
	protected int getStartRadius() {
		innerRadiusSquared = (radius - thickness) * (radius - thickness);
		return 0;
	}

	protected boolean containsPoint(int x, int y, int z)
	{
		float fx = (float)Math.abs(x) - 0.25f;
		float fy = (float)Math.abs(y) - 0.25f;
		float fz = (float)Math.abs(z) - 0.25f;
		int distanceSquared = (int)((fx * fx) + (fy * fy) + (fz * fz));
		if (thickness > 0) {
			return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared;
		}
		return distanceSquared <= radiusSquared;
	}
}
