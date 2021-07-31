package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Location;

import javax.annotation.Nullable;

public class MythicMobManager {

    private final MagicController controller;
    private final MythicMobs api;

    public MythicMobManager(MagicController controller, MythicMobs api) {
        this.controller = controller;
        this.api = api;
    }

    public boolean isValid() {
        return api != null;
    }

    @Nullable
    public EntityData spawnMythicMob(String key, Location location) {
        ActiveMob mob = api.getMobManager().spawnMob(key, location);
        return new EntityData(controller, mob);
    }

}
