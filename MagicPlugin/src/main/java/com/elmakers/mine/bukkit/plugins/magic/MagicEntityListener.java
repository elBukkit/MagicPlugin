package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import com.elmakers.mine.bukkit.magic.Magic;

class MagicEntityListener extends EntityListener
{
    private Magic master;

    @Override
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (Player.class.isInstance(event.getEntity()))
        {
            Player player = (Player) event.getEntity();
            master.onPlayerDamage(player, event);
        }
    }

    public void setMagic(Magic master)
    {
        this.master = master;
    }
}
