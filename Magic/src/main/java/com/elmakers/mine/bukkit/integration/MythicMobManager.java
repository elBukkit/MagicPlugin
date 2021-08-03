package com.elmakers.mine.bukkit.integration;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;

public class MythicMobManager {

    private final MagicController controller;
    private final Plugin plugin;

    private MythicMobs api = null;

    public MythicMobManager(MagicController controller, Plugin plugin) {
        this.controller = controller;
        this.plugin = plugin;
    }

    public boolean initialize() {
        if (plugin == null || !(plugin instanceof MythicMobs)) {
            return false;
        }

        api = MythicMobs.inst();
        return true;
    }

    public boolean isEnabled() {
        return api != null;
    }

    @Nullable
    public EntityData spawnMythicMob(String key, Location location) {
        ActiveMob mob = api.getMobManager().spawnMob(key, location);
        if (mob == null) {
            controller.getLogger().warning("Unable to spawn mythic mob with id of " + key);
            return null;
        }

        return new EntityData(controller, key, mob.getEntity().getBukkitEntity());
    }

}
