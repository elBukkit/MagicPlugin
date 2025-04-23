package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityFoxData extends EntityAnimalData {
    private Fox.Type type;
    private Boolean crouching;
    private UUID firstTrusted;
    private UUID secondTrusted;

    public EntityFoxData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        String typeString = parameters.getString("fox_type");
        if (typeString != null && !typeString.isEmpty()) {
            try {
                type = Fox.Type.valueOf(typeString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid fox_type: " + typeString, ex);
            }
        }
        crouching = ConfigUtils.getOptionalBoolean(parameters, "crouching");
        String trusted = parameters.getString("trusted");
        if (trusted != null && !trusted.isEmpty()) {
            try {
                firstTrusted = UUID.fromString(trusted);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid trusted UUID: " + trusted, ex);
            }
        }
        String secondTrusted = parameters.getString("second_trusted");
        if (secondTrusted != null && !secondTrusted.isEmpty()) {
            try {
                this.secondTrusted = UUID.fromString(secondTrusted);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid trusted UUID: " + secondTrusted, ex);
            }
        }
    }

    public EntityFoxData(Entity entity) {
        super(entity);
        if (entity instanceof Fox) {
            Fox fox = (Fox)entity;
            type = fox.getFoxType();
            crouching = fox.isCrouching();
            AnimalTamer trusted = fox.getFirstTrustedPlayer();
            if (trusted != null) {
                firstTrusted = trusted.getUniqueId();
            }
            AnimalTamer second = fox.getSecondTrustedPlayer();
            if (second != null) {
                secondTrusted = second.getUniqueId();
            }
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Fox) {
            Fox fox = (Fox)entity;
            if (type != null) {
                fox.setFoxType(type);
            }
            if (crouching != null) fox.setCrouching(crouching);
            if (firstTrusted != null) {
                OfflinePlayer trusted = Bukkit.getOfflinePlayer(firstTrusted);
                if (trusted != null) {
                    fox.setFirstTrustedPlayer(trusted);
                }
            }
            if (secondTrusted != null) {
                OfflinePlayer trusted = Bukkit.getOfflinePlayer(secondTrusted);
                if (trusted != null) {
                    fox.setSecondTrustedPlayer(trusted);
                }
            }
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        Fox fox = (Fox)entity;
        Fox.Type type = fox.getFoxType();
        Fox.Type[] typeValues = Fox.Type.values();
        int typeOrdinal = (type.ordinal() + 1) % typeValues.length;
        type = typeValues[typeOrdinal];
        fox.setFoxType(type);
        return true;
    }

    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Fox;
    }
}
