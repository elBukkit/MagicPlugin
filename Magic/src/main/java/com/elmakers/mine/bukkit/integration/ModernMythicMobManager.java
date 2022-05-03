package com.elmakers.mine.bukkit.integration;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.MagicController;

import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;

public class ModernMythicMobManager implements MythicMobManager {

    private final MagicController controller;
    private final Plugin plugin;

    private MythicPlugin api = null;

    public ModernMythicMobManager(MagicController controller, Plugin plugin) {
        this.controller = controller;
        this.plugin = plugin;
    }

    @Override
    public boolean initialize() {
        if (plugin == null || !(plugin instanceof MythicPlugin)) {
            return false;
        }

        api = (MythicPlugin)plugin;
        return true;
    }

    @Override
    public boolean isEnabled() {
        return api != null;
    }

    @Override
    @Nullable
    public Entity spawn(String key, Location location, double level) {
        if (api == null) return null;
        Optional<MythicMob> mythicMob = api.getMobManager().getMythicMob(key);
        if (!mythicMob.isPresent()) {
            controller.getLogger().warning("Unknown mythic mob type: " + key);
            return null;
        }
        ActiveMob mob = mythicMob.get().spawn(BukkitAdapter.adapt(location), level);
        if (mob == null) {
            controller.getLogger().warning("Unable to spawn mythic mob with id of " + key);
            return null;
        }

        return mob.getEntity().getBukkitEntity();
    }

    @Override
    public Collection<String> getMobKeys() {
        if (api == null) return null;
        return api.getMobManager().getMobNames();
    }

    @Override
    public boolean isMobKey(String mobKey) {
        if (api == null) return false;
        // Hopefully this is backed by a Set?
        return api.getMobManager().getMobNames().contains(mobKey);
    }

    @Override
    public void setMobLevel(Entity entity, double level) {
        if (api == null || entity == null) {
            return;
        }
        Optional<ActiveMob> mob = getActiveMob(entity.getUniqueId());
        if (!mob.isPresent()) {
            return;
        }
        mob.get().setLevel(level);
    }

    @Override
    @Nullable
    public Double getMobLevel(Entity entity) {
        if (api == null || entity == null) {
            return null;
        }
        Optional<ActiveMob> mob = getActiveMob(entity.getUniqueId());
        if (!mob.isPresent()) {
            return null;
        }
        return mob.get().getLevel();
    }

    @Override
    @Nullable
    public String getMobKey(Entity entity) {
        if (api == null || entity == null) {
            return null;
        }
        Optional<ActiveMob> mob = getActiveMob(entity.getUniqueId());
        if (!mob.isPresent()) {
            return null;
        }
        return mob.get().getMobType();
    }

    // Not in the API...
    @SuppressWarnings("unchecked")
    public Optional<ActiveMob> getActiveMob(UUID id) {
        try {
            MobManager manager = api.getMobManager();
            Method getActiveMobMethod = manager.getClass().getMethod("getActiveMob", UUID.class);
            if (getActiveMobMethod != null) {
                return (Optional<ActiveMob>)getActiveMobMethod.invoke(manager, id);
            } else {
                controller.getLogger().warning("MythicMobs integration has gone wrong, disabling");
                api = null;
            }
        } catch (Exception ex) {
            controller.getLogger().warning("MythicMobs integration has gone wrong, disabling");
            api = null;
        }
        return Optional.empty();
    }
}
