package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public class PlaySoundAction extends BaseSpellAction
{
	private Sound sound;
	private float volume;
	private float pitch;

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
		location.getWorld().playSound(location, sound, volume, pitch);
		return SpellResult.CAST;
	}

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		try {
			String soundName = parameters.getString("sound").toUpperCase();
			sound = Sound.valueOf(soundName);
		} catch (Exception ex) {
			sound = null;
		}
        pitch = (float)parameters.getDouble("pitch", 1);
		volume = (float)parameters.getDouble("volume", 1);
	}
}
