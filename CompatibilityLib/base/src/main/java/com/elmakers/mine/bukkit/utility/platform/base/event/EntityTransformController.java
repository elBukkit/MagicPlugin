package com.elmakers.mine.bukkit.utility.platform.base.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.inventory.EntityEquipment;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityTransformController implements Listener {
    private MageController controller;

    public EntityTransformController(MageController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        Entity original = event.getEntity();
        com.elmakers.mine.bukkit.api.entity.EntityData entityData = controller.getMob(original);
        if (entityData != null && !entityData.isTransformable()) {
            event.setCancelled(true);

            // Sadly it seems the mob does not get their equipment back when this event is cancelled.
            // I think it gets moved to the target entity, so we'll take it back.
            Entity transformed = event.getTransformedEntity();
            if (transformed instanceof LivingEntity && original instanceof LivingEntity) {
                LivingEntity from = (LivingEntity)transformed;
                LivingEntity to = (LivingEntity)original;
                EntityEquipment equipment = to.getEquipment();
                if (equipment != null) {
                    equipment.setArmorContents(from.getEquipment().getArmorContents());
                    equipment.setItemInMainHand(from.getEquipment().getItemInMainHand());
                    equipment.setItemInOffHand(from.getEquipment().getItemInOffHand());
                }
            }
        }
    }
}
