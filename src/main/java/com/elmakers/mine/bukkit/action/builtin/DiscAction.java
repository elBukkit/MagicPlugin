package com.elmakers.mine.bukkit.action.builtin;

public class DiscAction extends PlaneAction
{
	@Override
    protected boolean containsPoint(int x, int y, int z)
	{
		switch (axis)
		{
			case X:
				return (y * y) + (z * z) <= radiusSquared;
			case Z:
				return (x * x) + (y * y) <= radiusSquared;
			default:
			    return (x * x) + (z * z) <= radiusSquared;
		}
	}
}
