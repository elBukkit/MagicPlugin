package com.elmakers.mine.bukkit.magic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.entity.EntityData;

public class MageTargeting {
    private final Mage mage;
    private final EntityData entityData;
    private final Map<UUID, DamagedBy> damagedBy = new HashMap<>();
    private DamagedBy topDamager;
    private DamagedBy lastDamager;
    public MageTargeting(Mage mage) {
        this.mage = mage;
        this.entityData = mage.getEntityData();
    }

    @Nullable
    public Collection<Entity> getDamagers() {
        if (damagedBy == null) return null;
        Collection<Entity> damagers = new ArrayList<>();
        for (DamagedBy damager : damagedBy.values()) {
            Entity entity = damager.getEntity();
            if (entity != null) {
                damagers.add(entity);
            }
        }
        return damagers;
    }

    private boolean withinRange(Entity entity) {
        boolean withinRange = mage.getLocation().getWorld() == entity.getLocation().getWorld();
        double rangeSquared = entityData == null ? 0 : entityData.getTrackRadiusSquared();
        if (rangeSquared > 0 && withinRange) {
            withinRange = mage.getLocation().distanceSquared(entity.getLocation()) <= rangeSquared;
        }
        return withinRange;
    }

    public @Nullable Entity getTopDamager() {
        if (topDamager == null) return null;
        Entity topEntity = topDamager.getEntity();
        if (topEntity == null && damagedBy != null) {
            double topDamage = 0;
            for (Iterator<Map.Entry<UUID, DamagedBy>> it = damagedBy.entrySet().iterator(); it.hasNext();) {
                Map.Entry<UUID, DamagedBy> entry = it.next();
                DamagedBy damaged = entry.getValue();
                Entity entity = damaged.getEntity();
                if (entity != null && entity.isValid() && !entity.isDead()) {
                    boolean withinRange = withinRange(entity);
                    if (withinRange && damaged.damage > topDamage) {
                        topEntity = entity;
                        topDamage = damaged.damage;
                        topDamager = damaged;
                    }
                } else {
                    it.remove();
                }
            }
        }

        return topEntity;
    }

    public void damagedBy(@Nonnull Entity damager, double damage) {
        Entity lastDamagerEntity = lastDamager == null ? null : lastDamager.getEntity();
        if (lastDamagerEntity == damager) {
            lastDamager.damage += damage;
        } else {
            // See if we have been damaged by this before
            // Currently only mobs track damage history like this
            if (damagedBy != null) {
                lastDamager = damagedBy.get(damager.getUniqueId());
            } else {
                lastDamager = null;
            }
            // If this is a new damager, create a new record to track it
            if (lastDamager == null) {
                lastDamager = new DamagedBy(damager, damage);
                if (damagedBy != null) {
                    damagedBy.put(damager.getUniqueId(), lastDamager);
                }
            } else {
                lastDamager.damage += damage;
            }
        }

        if (topDamager != null) {
            if (topDamager.getEntity() == null || topDamager.damage < lastDamager.damage || !withinRange(topDamager.getEntity())) {
                topDamager = lastDamager;
                if (entityData != null && entityData.shouldFocusOnDamager()) {
                    mage.setTarget(damager);
                }
            }
        } else {
            topDamager = lastDamager;
            if (entityData != null && entityData.shouldFocusOnDamager()) {
                mage.setTarget(damager);
            }
        }
    }

    private static class DamagedBy {
        public double damage;
        private WeakReference<Entity> player;

        public DamagedBy(Entity entity, double damage) {
            this.player = new WeakReference<>(entity);
            this.damage = damage;
        }

        public Entity getEntity() {
            return player.get();
        }
    }
}
