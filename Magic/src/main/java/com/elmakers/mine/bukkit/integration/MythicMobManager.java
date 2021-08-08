package com.elmakers.mine.bukkit.integration;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

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
    public Entity spawn(String key, Location location) {
        ActiveMob mob = api.getMobManager().spawnMob(key, location);
        if (mob == null) {
            controller.getLogger().warning("Unable to spawn mythic mob with id of " + key);
            return null;
        }

        return mob.getEntity().getBukkitEntity();
    }

    public Collection<String> getMobKeys() {
        return api.getMobManager().getMobNames();
    }

    public boolean isMobKey(String mobKey) {
        // Hopefully this is backed by a Set?
        return api.getMobManager().getMobNames().contains(mobKey);
    }

    public void setMobLevel(Entity entity, double level) {
        if (entity == null) {
            return;
        }
        Optional<ActiveMob> mob = api.getMobManager().getActiveMob(entity.getUniqueId());
        if (!mob.isPresent()) {
            return;
        }
        mob.get().setLevel(level);
    }

    @Nullable
    public Double getMobLevel(Entity entity) {
        if (entity == null) {
            return null;
        }
        Optional<ActiveMob> mob = api.getMobManager().getActiveMob(entity.getUniqueId());
        if (!mob.isPresent()) {
            return null;
        }
        return mob.get().getLevel();
    }

    @Nullable
    public String getMobKey(Entity entity) {
        if (entity == null) {
            return null;
        }
        Optional<ActiveMob> mob = api.getMobManager().getActiveMob(entity.getUniqueId());
        if (!mob.isPresent()) {
            return null;
        }
        return mob.get().getMobType();
    }
}
