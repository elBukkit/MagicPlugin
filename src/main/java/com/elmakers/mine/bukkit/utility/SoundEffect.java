package com.elmakers.mine.bukkit.utility;

import com.elmakers.mine.bukkit.api.magic.MageController;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class SoundEffect implements com.elmakers.mine.bukkit.api.effect.SoundEffect {
    private Sound sound;
    private String customSound;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private int range = 0;

    public SoundEffect(Sound sound) {
        this.sound = sound;
        this.customSound = null;
    }

    public SoundEffect(String key) {
        if (key != null && key.length() > 0) {
            String[] pieces = StringUtils.split(key, ',');
            String soundName = pieces[0];

            if (soundName.indexOf('.') < 0) {
                try {
                    sound = Sound.valueOf(soundName.toUpperCase());
                } catch (Exception ex) {
                    sound = null;
                    customSound = soundName;
                }
            } else {
                customSound = soundName;
            }
            if (pieces.length > 1) {
                try {
                    volume = Float.parseFloat(pieces[1]);
                } catch (Exception ex) {
                    volume = 1;
                }
            }
            if (pieces.length > 2) {
                try {
                    pitch = Float.parseFloat(pieces[2]);
                } catch (Exception ex) {
                    pitch = 1;
                }
            }
        }
    }

    @Override
    public String getCustomSound() {
        return customSound;
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String toString() {
        return sound.name() + "," + volume + "," + pitch;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SoundEffect)) return false;

        SoundEffect otherEffect = (SoundEffect)other;
        return sound != otherEffect.sound || pitch != otherEffect.pitch || volume != otherEffect.volume;
    }

    public int getRange() {
        return range;
    }

    public void play(MageController controller, Entity entity) {
        if (entity == null || controller == null) return;

        Location sourceLocation = entity.getLocation();
        if (customSound != null) {
            if (range > 0) {
                int rangeSquared = range * range;
                Plugin plugin = controller.getPlugin();
                Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
                for (Player player : players) {
                    Location location = player.getLocation();
                    if (location.getWorld().equals(sourceLocation.getWorld()) && location.distanceSquared(sourceLocation) <= rangeSquared) {
                        player.playSound(sourceLocation, customSound, volume, pitch);
                    }
                }
            } else if (entity instanceof Player) {
                Player player = (Player)entity;
                player.playSound(sourceLocation, customSound, volume, pitch);
            }
        }

        if (sound != null) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                player.playSound(sourceLocation, sound, volume, pitch);
            } else {
                entity.getLocation().getWorld().playSound(sourceLocation, sound, volume, pitch);
            }
        }
    }
}
