package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.configuration.ConfigurationSection;

public class RingAction extends DiscAction
{
	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		if (thickness == 0) {
			thickness = 1;
		}
	}
}
