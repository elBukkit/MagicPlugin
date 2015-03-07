package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

public class WeatherAction extends BaseSpellAction
{
	private String weatherString = "";

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        weatherString = parameters.getString("weather", "");
    }

	@Override
	public SpellResult perform(CastContext context) {
		World world = context.getWorld();
		if (world == null) {
			return SpellResult.WORLD_REQUIRED;
		}
		boolean hasStorm = world.hasStorm();
		boolean makeStorm = weatherString.equals("storm");
		if (weatherString.equals("cycle")) {
			makeStorm = !hasStorm;
		}

		if (makeStorm) {
			world.setStorm(true);
			world.setThundering(true);
		} else {
			world.setStorm(false);
			world.setThundering(false);
		}
		return makeStorm ? SpellResult.CAST : SpellResult.ALTERNATE;
	}

	@Override
	public void getParameterNames(Collection<String> parameters) {
		super.getParameterNames(parameters);
		parameters.add("weather");
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey) {
		if (parameterKey.equals("weather")) {
			examples.add("storm");
			examples.add("cycle");
			examples.add("clear");
		} else {
			super.getParameterOptions(examples, parameterKey);
		}
	}
}
