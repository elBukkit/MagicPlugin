package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;

public class EntityEnderDragonData extends EntityExtraData {
    public Phase phase;

    public EntityEnderDragonData() {

    }

    public EntityEnderDragonData(ConfigurationSection parameters, MageController controller) {
        Logger log = controller.getLogger();
        String phaseName = parameters.getString("phase");
        if (phaseName != null && !phaseName.isEmpty()) {
            try {
                phase = Phase.valueOf(phaseName.toUpperCase());
            } catch (Throwable ex) {
                log.warning("Invalid ender dragon phase: " + phaseName);
            }
        }
    }

    public EntityEnderDragonData(Entity entity) {
        if (entity instanceof EnderDragon) {
            phase = ((EnderDragon)entity).getPhase();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (phase != null && entity instanceof EnderDragon) {
            ((EnderDragon)entity).setPhase(phase);
        }
    }
}
