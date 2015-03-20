package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.configuration.ConfigurationSection;

public class DiscAction extends VolumeAction
{
	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
        ySize = 0;
	}

	protected boolean containsPoint(int x, int y, int z)
	{
		return ((x * x) + (z * z) - (radius * radius)) <= 0;
	}
}
