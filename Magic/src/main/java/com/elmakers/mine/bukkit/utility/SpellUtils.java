package com.elmakers.mine.bukkit.utility;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class SpellUtils {
    public static Mage getCastSource(EntityData.SourceType sourceType, Entity initiator, Entity target, Mage magicBlock, MageController controller, String loggerContext) {
        Entity sourceEntity = initiator;
        switch (sourceType) {
            case PLAYER:
                sourceEntity = initiator;
                break;
            case OPPED_PLAYER:
                controller.getLogger().info("Invalid spell source on " + loggerContext + ": OPPED_initiator, will not op initiator for spell cast");
                sourceEntity = initiator;
                break;
            case MOB:
                if (target == null) {
                    controller.getLogger().info("Invalid spell source on " + loggerContext + ": MOB, there is no target entity");
                } else {
                    sourceEntity = target;
                }
                break;
            case BLOCK:
                if (magicBlock == null) {
                    controller.getLogger().info("Invalid spell source on " + loggerContext + ": BLOCK, there is no magic block");
                } else {
                    return magicBlock;
                }
            case CONSOLE:
            default:
                controller.getLogger().info("Invalid spell source on " + loggerContext + ": " + sourceType + ", using MOB instead");
                sourceEntity = target;
                break;
        }
        return controller.getMage(sourceEntity);
    }

    public static void prepareParameters(EntityData.TargetType targetType, ConfigurationSection parameters, Entity initiator, Entity target, Mage magicBlock, MageController controller, String loggerContext) {
        switch (targetType) {
            case PLAYER:
                parameters.set("player", initiator.getName());
                break;
            case MOB:
                if (target == null) {
                    controller.getLogger().info("Invalid spell source on " + loggerContext + ": MOB, there is no target entity");
                } else {
                    parameters.set("entity", target.getUniqueId().toString());
                }
                break;
            case BLOCK:
                if (magicBlock == null) {
                    controller.getLogger().info("Invalid spell source on " + loggerContext + ": BLOCK, there is no magic block");
                } else {
                    Location loc = magicBlock.getLocation();
                    parameters.set("tx", loc.getX());
                    parameters.set("ty", loc.getY());
                    parameters.set("tz", loc.getZ());
                    parameters.set("tworld", loc.getWorld().getName());
                }
                break;
            case NONE:
                break;
        }
    }
}
