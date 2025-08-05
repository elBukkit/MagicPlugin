package com.elmakers.mine.bukkit.integration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public interface LibsDisguiseManager {
    static boolean isCurrentVersion() {
        try {
            DisguiseAPI.class.getMethod("getCustomDisguise", String.class);
            FlagWatcher.class.getMethod("setSwimming", Boolean.TYPE);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    boolean initialize();

    boolean isDisguised(Entity entity);

    boolean disguise(Entity entity, ConfigurationSection configuration);
}
