package com.elmakers.mine.bukkit.entity;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityFoxData extends EntityAnimalData {
    private Fox.Type type;
    private UUID firstTrusted;
    private UUID secondTrusted;
    private boolean crouching;

    public EntityFoxData() {

    }

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
        crouching = parameters.getBoolean("crouching");
    }

    public EntityFoxData(Entity entity) {
        super(entity);
        if (entity instanceof Fox) {
            Fox fox = (Fox)entity;
            type = fox.getFoxType();
            AnimalTamer trusted = fox.getFirstTrustedPlayer();
            if (trusted != null) {
                firstTrusted = trusted.getUniqueId();
            }
            AnimalTamer second = fox.getSecondTrustedPlayer();
            if (second != null) {
                secondTrusted = second.getUniqueId();
            }
            crouching = fox.isCrouching();
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
            fox.setCrouching(crouching);
        }
    }

    public static boolean tame(Player tamer, Entity entity) {
        if (!(entity instanceof Fox)) return false;
        if (tamer == null) return false;
        Fox fox = (Fox)entity;
        AnimalTamer current = fox.getFirstTrustedPlayer();
        if (current != null && current.getUniqueId().equals(tamer.getUniqueId())) return false;
        fox.setFirstTrustedPlayer(tamer);
        return true;
    }
}
