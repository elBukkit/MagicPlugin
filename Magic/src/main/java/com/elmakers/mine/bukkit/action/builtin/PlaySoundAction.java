package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.SoundEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class PlaySoundAction extends BaseSpellAction
{
	private SoundEffect sound;

	@Override
	public SpellResult perform(CastContext context)
	{
		Location location = context.getTargetLocation();
		if (location == null) {
			location = context.getLocation();
		}
		if (sound == null || location == null) {
			return SpellResult.FAIL;
		}
		sound.play(context.getPlugin(), location);
		return SpellResult.CAST;
	}

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		sound = new SoundEffect(parameters.getString("sound"));
        sound.setPitch((float)parameters.getDouble("pitch", sound.getPitch()));
		sound.setVolume((float)parameters.getDouble("volume", sound.getVolume()));
	}
}
