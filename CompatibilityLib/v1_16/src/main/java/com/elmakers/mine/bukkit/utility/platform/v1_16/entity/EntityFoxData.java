package com.elmakers.mine.bukkit.utility.platform.v1_16.entity;

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

public class EntityFoxData extends com.elmakers.mine.bukkit.utility.platform.v1_14.entity.EntityFoxData {
    private UUID firstTrusted;
    private UUID secondTrusted;

    public EntityFoxData() {

    }

    public EntityFoxData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
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
}
