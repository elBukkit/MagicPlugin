package com.elmakers.mine.bukkit.action.builtin;

public class CylinderAction extends VolumeAction
{
	protected boolean containsPoint(int x, int y, int z)
	{
		return ((x * x) + (z * z) - (radius * radius)) <= 0;
	}
}
