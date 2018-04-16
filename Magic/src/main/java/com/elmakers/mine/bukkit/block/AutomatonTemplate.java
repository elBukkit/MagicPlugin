package com.elmakers.mine.bukkit.block;

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

public class AutomatonTemplate {
    @Nonnull
    private final String key;
    private String name;
    private int interval;
    private Deque<WeightedPair<String>> entityTypeProbability;

    public AutomatonTemplate(@Nonnull String key) {
        this.key = key;
    }

    public AutomatonTemplate(@Nonnull String key, @Nonnull ConfigurationSection node) {
        this(key);
        name = node.getString("name");
        interval = node.getInt("interval", 0);

        if (node.contains("mobs")) {
            entityTypeProbability = new ArrayDeque<>();
            RandomUtils.populateStringProbabilityMap(entityTypeProbability, ConfigurationUtils.getConfigurationSection(node, "mobs"), 0, 0, 0);
        }
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public int getInterval() {
        return interval;
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