package com.elmakers.mine.bukkit.action.builtin;

public class SphereAction extends VolumeAction
{
	protected boolean containsPoint(int x, int y, int z)
	{
		return ((x * x) + (y * y) + (z * z) - (radius * radius)) <= 0;
	}
}
