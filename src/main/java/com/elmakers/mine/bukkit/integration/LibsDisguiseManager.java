package com.elmakers.mine.bukkit.integration;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class LibsDisguiseManager {
    private final Plugin plugin;
    private Method isDisguisedMethod = null;

    public LibsDisguiseManager(Plugin owningPlugin, Plugin disguisePlugin) {
        this.plugin = owningPlugin;
    }

    public boolean initialize() {
        try {
            Class<?> disguiseAPI = Class.forName("me.libraryaddict.disguise.DisguiseAPI");
            if (disguiseAPI != null) {
                isDisguisedMethod = disguiseAPI.getMethod("isDisguised", Entity.class);
            } else {
                plugin.getLogger().log(Level.WARNING, "LibsDisguise plugin found, but DisguiseAPI could not be loaded");
                return false;
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "LibsDisguise integration failed", ex);
            isDisguisedMethod = null;
            return false;
        }
        if (isDisguisedMethod == null) {
            plugin.getLogger().log(Level.WARNING, "Something went wrong with LibsDisguise integration");
            return false;
        }

        return true;
    }

    public boolean isDisguised(Entity entity) {
        if (isDisguisedMethod == null) return false;
        boolean disguised = false;
        try {
            disguised = (Boolean)isDisguisedMethod.invoke(null, entity);
        } catch (Exception ex) {
            disguised = false;
        }
        return disguised;
    }
}
