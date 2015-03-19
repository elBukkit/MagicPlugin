package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.configuration.ConfigurationSection;

public class PlaneAction extends VolumeAction
{
	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
        ySize = 0;
	}

	protected boolean containsPoint(int x, int y, int z)
	{
		return true;
	}
}
