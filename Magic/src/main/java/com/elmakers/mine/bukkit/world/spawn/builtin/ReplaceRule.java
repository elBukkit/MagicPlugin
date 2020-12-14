package com.elmakers.mine.bukkit.world.spawn.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import com.elmakers.mine.bukkit.world.spawn.EntityDataParser;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class ReplaceRule extends SpawnRule {
    // Keep these separate for efficiency
    protected EntityData replaceWith;
    protected Deque<WeightedPair<EntityData>> replaceProbability;

    @Override
    public void finalizeLoad(String worldName) {
        // Legacy support
        if (!parameters.contains("type")) {
            parameters.set("type", parameters.get("replace_type"));
        }
        if (!parameters.contains("sub_type")) {
            parameters.set("sub_type", parameters.get("replace_sub_type"));
        }

        ConfigurationSection section = parameters.getConfigurationSection("type");
        if (section == null) {
            replaceWith = controller.getMob(parameters.getString("type"));
        } else {
            replaceProbability = new ArrayDeque<>();
            RandomUtils.populateProbabilityMap(EntityDataParser.getInstance(controller), replaceProbability, section);
        }

        if (replaceProbability == null && (replaceWith == null || replaceWith.getType() == null)) {
            controller.getLogger().warning("Error reading in configuration for custom mob in " + worldName + " for rule " + key);
            return;
        }
        String replaceDescription = replaceWith == null ? "(randomized)" : replaceWith.describe();
        String message = " Replacing: " + getTargetEntityTypeName() + " in " + worldName + " at y > " + minY
                + " with " + replaceDescription + " at a " + (percentChance * 100) + "% chance";

        if (tags != null) {
            message = message + " in regions tagged with any of " + tags.toString();
        }
        if (biomes != null) {
            message = message + " in biomes " + biomes.toString();
        }
        if (notBiomes != null) {
            message = message + " not in biomes " + notBiomes.toString();
        }
        controller.info(message);
    }

    @Override
    @Nullable
    public LivingEntity onProcess(Plugin plugin, LivingEntity entity) {
        if (replaceWith == null && replaceProbability == null) return null;
        EntityData replaceWith = this.replaceWith;
        if (replaceProbability != null) {
            replaceWith = RandomUtils.weightedRandom(replaceProbability);
        }
        if (replaceWith == null) {
            return null;
        }

        // This makes replacing the same type of mob have better balance,
        // particularly with mob spawners
        if (entity.getType() == replaceWith.getType()) {
            replaceWith.modify(entity);
            return entity;
        }

        Entity spawned = replaceWith.spawn(entity.getLocation());
        return spawned instanceof LivingEntity ? (LivingEntity)spawned : null;
    }
}
