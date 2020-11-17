package com.elmakers.mine.bukkit.entity;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityAnimalData extends EntityExtraData {
    private UUID owner;
    private boolean tamed;
    protected boolean sitting;

    public EntityAnimalData() {

    }

    public EntityAnimalData(ConfigurationSection parameters, MageController controller) {
        Logger log = controller.getLogger();
        String tamer = parameters.getString("owner");
        if (tamer != null && !tamer.isEmpty()) {
            try {
                this.owner = UUID.fromString(tamer);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid owner UUID: " + tamer, ex);
            }
        }
        tamed = parameters.getBoolean("tamed", owner != null);
        sitting = parameters.getBoolean("sitting", false);
    }

    public EntityAnimalData(Entity entity) {
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable)entity;
            AnimalTamer tamer = tameable.getOwner();
            if (tamer != null) {
                this.owner = tamer.getUniqueId();
            }
            this.tamed = tameable.isTamed();
        }
        if (entity instanceof Sittable) {
            Sittable sittable = (Sittable)entity;
            this.sitting = sittable.isSitting();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable)entity;
            if (owner != null) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(this.owner);
                if (owner != null) {
                    tameable.setOwner(owner);
                }
            }
            tameable.setTamed(tamed);
        }
        if (entity instanceof Sittable) {
            Sittable sittable = (Sittable)entity;
            sittable.setSitting(sitting);
        }
    }
}
