package com.elmakers.mine.bukkit.automata;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class Spawner {
    @Nonnull
    private final Deque<WeightedPair<String>> entityTypeProbability;
    private final Set<String> entityNames = new HashSet<>();
    private final Set<EntityType> entityTypes = new HashSet<>();
    private final int playerRange;
    private final int minPlayers;
    private final double probability;
    private final int limit;
    private final int limitRange;
    private final int verticalRange;

    public Spawner(@Nonnull MageController controller, @Nonnull AutomatonTemplate automaton, ConfigurationSection configuration) {
        entityTypeProbability = new ArrayDeque<>();
        RandomUtils.populateStringProbabilityMap(entityTypeProbability, ConfigurationUtils.getConfigurationSection(configuration, "mobs"), 0, 0, 0);
        if (entityTypeProbability.isEmpty()) {
            controller.getLogger().warning("Automata template " + automaton.getKey() + " has a spawner with no mobs defined");
        }

        probability = configuration.getDouble("probability", 0);

        playerRange = configuration.getInt("player_range", 64);
        minPlayers = configuration.getInt("min_players", 1);

        limit = configuration.getInt("limit", 0);
        limitRange = configuration.getInt("limit_range", 16);
        verticalRange = configuration.getInt("vertical_range", 0);
    }

    @Nullable
    public Entity spawn(MagicController controller, Location location) {
        if (entityTypeProbability.isEmpty()) {
            return null;
        }

        if (probability > 0) {
            if (controller.getRandom().nextDouble() > probability) {
                return null;
            }
        }
        boolean hasLimit = limit > 0 && limitRange > 0;
        boolean requiresPlayers = playerRange > 0 && minPlayers > 0;
        if (hasLimit || requiresPlayers) {
            int range = 0;
            int playerRangeSquared = playerRange * playerRange;
            int limitRangeSquared = limitRange * limitRange;
            if (hasLimit) {
                range = limitRange;
            }
            if (requiresPlayers) {
                range = Math.max(playerRange, range);
            }
            int playerCount = 0;
            int mobCount = 0;
            int vertical = verticalRange > 0 ? verticalRange : range;
            Collection<Entity> entities = location.getWorld().getNearbyEntities(location, range, vertical, range);
            for (Entity entity : entities) {
                if (entity instanceof Player) {
                    if (playerRange == range || entity.getLocation().distanceSquared(location) <= playerRangeSquared) {
                        playerCount++;
                    }
                } else if (hasLimit && (limitRange == range || entity.getLocation().distanceSquared(location) <= limitRangeSquared)) {
                    String customName = entity.getCustomName();
                    if (customName == null || customName.isEmpty()) {
                        if (entityTypes.contains(entity.getType())) {
                            mobCount++;
                        }
                    } else if (entityNames.contains(customName)) {
                        mobCount++;
                    }
                    if (mobCount >= limit) break;
                }
            }

            if (requiresPlayers && playerCount < minPlayers) {
                return null;
            }
            if (hasLimit && mobCount >= limit) {
                return null;
            }
        }
        Entity entity;
        String randomType = RandomUtils.weightedRandom(entityTypeProbability);
        try {
            EntityData entityData = controller.getMob(randomType);
            if (entityData == null) {
                EntityType entityType = EntityType.valueOf(randomType.toUpperCase());
                entityData = new com.elmakers.mine.bukkit.entity.EntityData(entityType);
            }
            String customMob = entityData.getName();
            if (customMob == null || customMob.isEmpty()) {
                entityTypes.add(entityData.getType());
            } else {
                entityNames.add(entityData.getName());
            }
            entity = entityData.spawn(controller, location);
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error spawning mob from automata at " + location, ex);
            entity = null;
        }

        return entity;
    }

}
