package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.SoundEffect;

public class PlaySoundAction extends BaseSpellAction
{
    private SoundEffect sound;
    private int radius;

    @Override
    public SpellResult perform(CastContext context)
    {
        if (sound == null) {
            return SpellResult.FAIL;
        }
        sound.setRange(radius);
        if (radius == 0) {
            Entity entity = context.getTargetEntity();
            if (entity == null || !(entity instanceof Player)) {
                return SpellResult.NO_TARGET;
            }
            sound.play(context.getPlugin(), entity);
            return SpellResult.CAST;
        }
        Location location = context.getTargetLocation();
        if (location == null) {
            location = context.getLocation();
        }
        if (location == null) {
            return SpellResult.NO_TARGET;
        }
        sound.play(context.getPlugin(), location);
        return SpellResult.CAST;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        sound = new SoundEffect(parameters.getString("sound"));
        sound.setPitch((float)parameters.getDouble("pitch", sound.getPitch()));
        sound.setPitch((float)parameters.getDouble("sound_pitch", sound.getPitch()));
        sound.setVolume((float)parameters.getDouble("volume", sound.getVolume()));
        sound.setVolume((float)parameters.getDouble("sound_volume", sound.getVolume()));
        radius = parameters.getInt("radius", 32);
    }
}
