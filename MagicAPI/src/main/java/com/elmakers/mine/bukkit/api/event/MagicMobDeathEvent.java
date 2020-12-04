package com.elmakers.mine.bukkit.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class MagicMobDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final EntityDeathEvent deathEvent;
    private final EntityData entityData;
    private final MageController controller;
    private Mage mage;
    private Player player;

    public MagicMobDeathEvent(MageController controller, EntityData entityData, EntityDeathEvent deathEvent) {
        this.controller = controller;
        this.entityData = entityData;
        this.deathEvent = deathEvent;

        LivingEntity killed = deathEvent.getEntity();
        EntityDamageEvent damageEvent = killed.getLastDamageCause();
        if (damageEvent instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent damageByEvent = (EntityDamageByEntityEvent)damageEvent;
            Entity damagingEntity = damageByEvent.getDamager();
            if (damagingEntity instanceof Projectile) {
                Projectile projectile = (Projectile)damagingEntity;
                ProjectileSource source = projectile.getShooter();
                if (source instanceof LivingEntity) {
                    damagingEntity = (LivingEntity)source;
                }
            }
            if (damagingEntity != null && damagingEntity instanceof Player)
            {
                player = (Player)damagingEntity;
                mage = controller.getRegisteredMage(player.getUniqueId().toString());
            }
        }
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public EntityDeathEvent getDeathEvent() {
        return deathEvent;
    }

    public EntityData getEntityData() {
        return entityData;
    }

    public MageController getController() {
        return controller;
    }

    public Mage getMage() {
        return mage;
    }

    public Player getPlayer() {
        return player;
    }
}
