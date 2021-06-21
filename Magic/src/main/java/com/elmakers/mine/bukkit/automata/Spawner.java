package com.elmakers.mine.bukkit.automata;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;

public class Spawner {
    @Nonnull
    private final MageController controller;
    @Nonnull
    private final Deque<WeightedPair<EntityData>> entityTypeProbability;
    @Nonnull
    private final Deque<WeightedPair<Integer>> countProbability;
    private final Set<String> entityNames = new HashSet<>();
    private final Set<EntityType> entityTypes = new HashSet<>();
    private boolean randomizeYaw = false;
    private boolean randomizePitch = false;
    private final int playerRange;
    private final int minPlayers;
    private final double probability;
    private final int limit;
    private final int limitRange;
    private final int verticalRange;
    private final int checkRadius;
    private final int verticalCheckRadius;
    private final boolean checkFloor;
    private final double radius;
    private final double verticalRadius;
    private final int locationRetry;
    private final MaterialSet passthrough;
    private final boolean track;
    private final boolean leash;
    private int interval;

    public Spawner(@Nonnull MageController controller, @Nonnull AutomatonTemplate automaton, ConfigurationSection configuration) {
        this.controller = controller;
        ConfigurationSection entityParameters = configuration.getConfigurationSection("parameters");
        entityTypeProbability = new ArrayDeque<>();
        Deque<WeightedPair<String>> keyProbability = new ArrayDeque<>();
        RandomUtils.populateStringProbabilityMap(keyProbability, configuration, "mobs");
        if (keyProbability.isEmpty()) {
            controller.getLogger().warning("Automaton template " + automaton.getKey() + " has a spawner with no mobs defined");
        } else {
            for (WeightedPair<String> keyPair : keyProbability) {
                String mobKey = keyPair.getValue();
                EntityData entityData = null;
                if (!mobKey.equalsIgnoreCase("none")) {
                    entityData = controller.getMob(mobKey);
                    if (entityData == null) {
                        controller.getLogger().warning("Invalid mob type in automaton " + automaton.getKey() + ": " + mobKey);
                    } else {
                        if (entityParameters != null) {
                            entityData = entityData.clone();
                            ConfigurationSection effectiveParameters = ConfigurationUtils.cloneConfiguration(entityData.getConfiguration());
                            effectiveParameters = ConfigurationUtils.addConfigurations(effectiveParameters, entityParameters);
                            entityData.load(effectiveParameters);
                        }
                        String customMob = entityData.getName();
                        if (customMob == null || customMob.isEmpty()) {
                            entityTypes.add(entityData.getType());
                        } else {
                            entityNames.add(customMob);
                        }
                    }
                }
                entityTypeProbability.add(new WeightedPair<>(keyPair, entityData));
            }
        }
        countProbability = new ArrayDeque<>();
        RandomUtils.populateIntegerProbabilityMap(countProbability, configuration, "count", 0, 0, 0);
        if (countProbability.isEmpty()) {
            countProbability.add(new WeightedPair<>(1.0f, 1));
        }

        probability = configuration.getDouble("probability", 0);

        playerRange = configuration.getInt("player_range", 64);
        minPlayers = configuration.getInt("min_players", 1);

        interval = configuration.getInt("interval", 0);
        int configuredLimit = configuration.getInt("limit", 0);
        limitRange = configuration.getInt("limit_range", 16);
        verticalRange = configuration.getInt("vertical_range", 0);
        radius = configuration.getDouble("radius");
        verticalRadius = configuration.getDouble("vertical_radius");
        checkRadius = configuration.getInt("check_radius", 1);
        verticalCheckRadius = configuration.getInt("vertical_check_radius", 1);
        checkFloor = configuration.getBoolean("check_floor", true);
        locationRetry = configuration.getInt("retries", 4);
        passthrough = controller.getMaterialSetManager().getMaterialSet("passthrough");
        randomizePitch = configuration.getBoolean("randomize_pitch", false);
        randomizeYaw = configuration.getBoolean("randomize_yaw", false);
        track = configuration.getBoolean("track", true);
        leash = configuration.getBoolean("leash", true);

        // Make sure the limit is at least big enough for all the spawn cunts
        for (WeightedPair<Integer> count : countProbability) {
            configuredLimit = Math.max(configuredLimit, count.getValue());
        }
        limit = configuredLimit;
    }

    private boolean isSafe(Location location) {
        if (checkRadius <= 0) return true;
        int range = checkRadius - 1;
        int verticalRange = verticalCheckRadius - 1;
        Block block = location.getBlock();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                for (int y = -verticalRange - 1; y <= verticalRange + 1; y++) {
                    Block testBlock = block.getRelative(x, y, z);
                    if (y < 0 && checkFloor) {
                        if (passthrough.testBlock(testBlock)) {
                            return false;
                        }
                    } else if (!passthrough.testBlock(testBlock)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Nullable
    private Location getSafeLocation(Location location) {
        int retry = 0;
        Block onBlock = location.getBlock().getRelative(BlockFace.DOWN);
        boolean goDown = passthrough.testBlock(onBlock);
        while (retry <= locationRetry) {
            if (isSafe(location)) {
                return location;
            }

            if (goDown) {
                location.setY(location.getY() - 1);
                if (location.getY() <= 0) return null;
            } else {
                location.setY(location.getY() + 1);
            }

            retry++;
        }

        return null;
    }

    public Nearby getNearby(Automaton automaton) {
        Location location = automaton.getLocation();
        int range = 0;
        Nearby nearby = new Nearby();
        nearby.mobs = automaton.getSpawnedCount();
        int playerRangeSquared = playerRange * playerRange;
        int limitRangeSquared = limitRange * limitRange;
        boolean hasLimit = limit > 0 && limitRange > 0;
        boolean requiresPlayers = playerRange > 0 && minPlayers > 0;
        if (hasLimit) {
            range = limitRange;
        }
        if (requiresPlayers) {
            range = Math.max(playerRange, range);
        }
        int vertical = verticalRange > 0 ? verticalRange : range;
        Collection<Entity> entities = location.getWorld().getNearbyEntities(location, range, vertical, range);
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                if (playerRange == range || entity.getLocation().distanceSquared(location) <= playerRangeSquared) {
                    nearby.players++;
                }
            } else if (!track && hasLimit && (limitRange == range || entity.getLocation().distanceSquared(location) <= limitRangeSquared)) {
                String customName = entity.getCustomName();
                if (customName == null || customName.isEmpty()) {
                    if (entityTypes.contains(entity.getType())) {
                        nearby.mobs++;
                    }
                } else if (entityNames.contains(customName)) {
                    nearby.mobs++;
                }
                if (nearby.mobs >= limit) break;
            }
        }
        return nearby;
    }

    @Nullable
    public List<Entity> spawn(Automaton automaton) {
        Location location = automaton.getLocation();
        if (entityTypeProbability.isEmpty()) {
            return null;
        }

        Random random = controller.getRandom();
        if (probability > 0) {
            if (random.nextDouble() > probability) {
                return null;
            }
        }

        Nearby nearby = null;
        boolean hasLimit = limit > 0 && limitRange > 0;
        boolean requiresPlayers = playerRange > 0 && minPlayers > 0;
        if (hasLimit || requiresPlayers) {
            nearby = getNearby(automaton);
            if (requiresPlayers && nearby.players < minPlayers) {
                return null;
            }
            if (hasLimit && nearby.mobs >= limit) {
                return null;
            }
        }

        int count = RandomUtils.weightedRandom(countProbability);
        if (nearby != null) {
            count = Math.min(count, limit - nearby.mobs);
        }

        List<Entity> spawned = new ArrayList<>();
        for (int num = 0; num < count; num++) {
            Location target = getSpawnLocation(location);

            if (randomizeYaw) {
                target.setYaw(RandomUtils.getRandom().nextFloat() * 360);
            }

            if (randomizePitch) {
                target.setPitch(RandomUtils.getRandom().nextFloat() * 180 - 90);
            }

            Entity entity;
            EntityData entityData = RandomUtils.weightedRandom(entityTypeProbability);
            if (entityData == null) continue;
            try {
                entity = entityData.spawn(target);
            } catch (Throwable ex) {
                controller.getLogger().log(Level.WARNING, "Error spawning mob from automaton at " + location, ex);
                entity = null;
            }
            if (entity != null) {
                spawned.add(entity);
            }
        }

        return spawned;
    }

    @Nonnull
    public Location getSpawnLocation(Location location) {
        Location target = location;
        if (radius > 0) {
            Random random = controller.getRandom();
            for (int i = 0; i < locationRetry + 1; i++) {
                target = location.clone();
                double vertical = verticalRadius >= 0 ? verticalRadius : radius;
                double xOffset = 2 * radius * random.nextDouble() - radius;
                double yOffset = vertical > 0 ? 2 * vertical * random.nextDouble() - vertical : 0;
                double zOffset = 2 * radius * random.nextDouble() - radius;

                target.setX(target.getX() + xOffset);
                target.setY(target.getY() + yOffset);
                target.setZ(target.getZ() + zOffset);

                target = getSafeLocation(target);
                if (target != null) break;
            }
        }

        if (target == null) {
           target = location;
        }
        return target;
    }

    public int getLimit() {
        return limit;
    }

    public int getInterval() {
        return interval;
    }

    public boolean isLeashed() {
        return leash && limitRange > 0;
    }

    public int getLimitRange() {
        return limitRange;
    }

    public boolean isTracked() {
        return track;
    }

    public int getMinPlayers() {
        return minPlayers;
    }
}
