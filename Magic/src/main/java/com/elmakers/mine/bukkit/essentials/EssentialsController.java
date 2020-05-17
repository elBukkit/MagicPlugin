package com.elmakers.mine.bukkit.essentials;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IUser;

public class EssentialsController {
    private final Essentials essentials;

    @Nullable
    public static EssentialsController initialize(Plugin essentialsPlugin) {
        if (!(essentialsPlugin instanceof Essentials)) {
            return null;
        }

        return new EssentialsController((Essentials)essentialsPlugin);
    }

    public EssentialsController(Essentials essentials) {
        this.essentials = essentials;
    }

    public boolean isVanished(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }

        IUser user = essentials.getUser((Player)entity);
        return user == null ? false : user.isVanished();
    }
}
