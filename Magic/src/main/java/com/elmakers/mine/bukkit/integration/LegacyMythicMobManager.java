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

public class LegacyMythicMobManager implements MythicMobManager {

    private final MagicController controller;
    private final Plugin plugin;

    private MythicMobs api = null;

    public LegacyMythicMobManager(MagicController controller, Plugin plugin) {
        this.controller = controller;
        this.plugin = plugin;
    }

    @Override
    public boolean initialize() {
        if (plugin == null || !(plugin instanceof MythicMobs)) {
            return false;
        }

        api = MythicMobs.inst();
        return api != null;
    }

    @Override
    public boolean isEnabled() {
        return api != null;
    }

    @Override
    @Nullable
    public Entity spawn(String key, Location location, double level) {
        ActiveMob mob = api.getMobManager().spawnMob(key, location);
        if (mob == null) {
            controller.getLogger().warning("Unable to spawn mythic mob with id of " + key);
            return null;
        }

        mob.setLevel(level);
        return mob.getEntity().getBukkitEntity();
    }

    @Override
    public Collection<String> getMobKeys() {
        return api.getMobManager().getMobNames();
    }

    @Override
    public boolean isMobKey(String mobKey) {
        // Hopefully this is backed by a Set?
        return api.getMobManager().getMobNames().contains(mobKey);
    }

    @Override
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

    @Override
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

    @Override
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
