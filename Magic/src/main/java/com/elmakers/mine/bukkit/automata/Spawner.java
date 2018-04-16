package com.elmakers.mine.bukkit.automata;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class Spawner {
    @Nonnull
    private final Deque<WeightedPair<String>> entityTypeProbability;

    public Spawner(ConfigurationSection configuration) {
        entityTypeProbability = new ArrayDeque<>();
        RandomUtils.populateStringProbabilityMap(entityTypeProbability, ConfigurationUtils.getConfigurationSection(configuration, "mobs"), 0, 0, 0);
    }

    @Nullable
    public Entity spawn(MagicController controller, Location location) {
        Entity entity = null;
        if (entityTypeProbability != null && !entityTypeProbability.isEmpty()) {
            String randomType = RandomUtils.weightedRandom(entityTypeProbability);
                try {
                    EntityData entityData = controller.getMob(randomType);
                    if (entityData == null) {
                        entityData = new com.elmakers.mine.bukkit.entity.EntityData(EntityType.valueOf(randomType.toUpperCase()));
                    }
                    entity = entityData.spawn(controller, location);
                } catch (Throwable ex) {
                    controller.getLogger().log(Level.WARNING, "Error spawning mob from automata at " + location, ex);
                    entity = null;
                }
        }

        return entity;
    }

}
