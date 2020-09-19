package com.elmakers.mine.bukkit.entity;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class EntityFoxData extends EntityExtraData {
    private Object type;
    private UUID firstTrusted;

    public EntityFoxData() {

    }

    public EntityFoxData(ConfigurationSection parameters, MageController controller) {
        Logger log = controller.getLogger();
        String typeString = parameters.getString("fox_type");
        if (typeString != null && !typeString.isEmpty()) {
            try {
                type = CompatibilityUtils.getFoxType(typeString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid fox_type: " + parameters.getString("fox_type"), ex);
            }
        }
        String trusted = parameters.getString("trusted");
        if (trusted != null && !trusted.isEmpty()) {
            try {
                firstTrusted = UUID.fromString(trusted);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid trusted UUID: " + parameters.getString("trusted"), ex);
            }
        }
    }

    public EntityFoxData(Entity fox) {
        if (CompatibilityUtils.isFox(fox)) {
            type = CompatibilityUtils.getFoxType(fox);
            Object trusted = CompatibilityUtils.getFirstTrustedPlayer(fox);
            if (trusted != null && trusted instanceof Player) {
                firstTrusted = ((Player)trusted).getUniqueId();
            }
        }
    }

    @Override
    public void apply(Entity entity) {
        if (CompatibilityUtils.isFox(entity)) {
            if (type != null) {
                CompatibilityUtils.setFoxType(entity, type);
            }
            if (firstTrusted != null) {
                OfflinePlayer trusted = Bukkit.getOfflinePlayer(firstTrusted);
                if (trusted != null) {
                    CompatibilityUtils.setFirstTrustedPlayer(entity, trusted);
                }
            }
        }
    }

    @Override
    public EntityExtraData clone() {
        EntityFoxData copy = new EntityFoxData();
        copy.type = type;
        return copy;
    }
}
