package com.elmakers.mine.bukkit.action.builtin;

public class DiscAction extends PlaneAction
{
	protected boolean containsPoint(int x, int y, int z)
	{
		switch (axis)
		{
			case X:
				return (double)((y * y) + (z * z)) <= radiusSquared;
			case Z:
				return (double)((x * x) + (y * y)) <= radiusSquared;
			default:
			    return (double)((x * x) + (z * z)) <= radiusSquared;
		}
	}
}
