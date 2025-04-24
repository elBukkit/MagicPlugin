package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.google.common.collect.Iterables;

public class PlayRecordAction extends BaseSpellAction
{
    private Set<String> records = new HashSet<>();

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        this.records.clear();
        String recordList = parameters.getString("records", "");
        if (!recordList.isEmpty()) {
            String[] records = StringUtils.split(recordList, ",");
            for (String record : records) {
                this.records.add(record.toUpperCase(Locale.ROOT));
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        List<Sound> sounds = new ArrayList<>();
        for (Sound sound : Sound.values()) {
            String soundName = sound.name().toUpperCase(Locale.ROOT);
            boolean isMusicDisc = soundName.startsWith("MUSIC_DISC");
            if (records.isEmpty()) {
                if (isMusicDisc) {
                    sounds.add(sound);
                }
            } else {
                if (records.contains(soundName)) {
                    sounds.add(sound);
                } else if (isMusicDisc) {
                    String musicDiscName = soundName.substring("MUSIC_DISC_".length());
                    if (records.contains(musicDiscName)) {
                        sounds.add(sound);
                    }
                }
            }
        }

        if (sounds.isEmpty()) {
            return SpellResult.FAIL;
        }

        Random random = context.getRandom();
        Sound sound = Iterables.get(sounds, random.nextInt(sounds.size()));

        Location location = context.getTargetLocation();
        location.getWorld().playSound(location, sound, SoundCategory.RECORDS, 1, 1);

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
