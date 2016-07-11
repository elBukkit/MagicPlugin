package com.elmakers.mine.bukkit.effect;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Hologram
{
    private Object skull;
    private Object horse;

    protected Hologram(Location location, String text)
    {
        skull = HoloUtils.createSkull(location);
        horse = HoloUtils.createHorse(location, text);
    }

    public void teleport(Location location)
    {
        HoloUtils.teleport(location, skull, horse);
    }

    public void setLabel(String label)
    {
        HoloUtils.rename(label, horse);
    }

    public void show(Player player)
    {
        HoloUtils.sendToPlayer(player, skull, horse);
    }

    public void hide(Player player)
    {
        HoloUtils.removeFromPlayer(player, skull, horse);
    }
}
