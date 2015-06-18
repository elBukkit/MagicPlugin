package com.elmakers.mine.bukkit.action.builtin;

public class DiscAction extends PlaneAction
{
	protected boolean containsPoint(int x, int y, int z)
	{
		switch (axis)
		{
			case X:
				return ((y * y) + (z * z) - (radius * radius)) <= 0;
			case Z:
				return ((x * x) + (y * y) - (radius * radius)) <= 0;
		}
		return ((x * x) + (z * z) - (radius * radius)) <= 0;
	}
}
