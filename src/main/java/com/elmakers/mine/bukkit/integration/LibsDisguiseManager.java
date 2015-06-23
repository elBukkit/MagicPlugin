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
        try {
            Class<?> disguiseAPI = Class.forName("me.libraryaddict.disguise.DisguiseAPI");
            if (disguiseAPI != null) {
                isDisguisedMethod = disguiseAPI.getMethod("isDisguised", Entity.class);
            }
        } catch (Exception ex) {
            owningPlugin.getLogger().log(Level.WARNING, "LibsDisguise integration failed", ex);
            isDisguisedMethod = null;
            return;
        }
        if (isDisguisedMethod != null) {
            owningPlugin.getLogger().log(Level.WARNING, "Something went wrong with LibsDisguise integration");
        }
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
