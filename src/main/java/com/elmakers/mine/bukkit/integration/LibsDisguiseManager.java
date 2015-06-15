package com.elmakers.mine.bukkit.integration;

import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class LibsDisguiseManager {
    private final Plugin plugin;

    public LibsDisguiseManager(Plugin owningPlugin, Plugin disguisePlugin) {
        this.plugin = owningPlugin;
    }

    public boolean isDisguised(Entity entity) {
        return DisguiseAPI.isDisguised(entity);
    }
}
