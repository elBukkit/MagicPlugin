package com.elmakers.mine.bukkit.automata;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.base.Preconditions;

public class AutomatonTemplate {
    @Nonnull
    private final MageController controller;
    @Nonnull
    private final ConfigurationSection configuration;
    @Nonnull
    private final String key;
    @Nullable
    private String name;
    @Nullable
    private String description;
    private int interval;
    @Nullable
    private Spawner spawner;
    @Nullable
    private Caster caster;
    @Nullable
    private Collection<EffectPlayer> effects;

    private final int playerRange;
    private final int minPlayers;
    private final Integer minTimeOfDay;
    private final Integer maxTimeOfDay;
    private final Integer minPhaseOfMoon;
    private final Integer maxPhaseOfMoon;

    public AutomatonTemplate(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        this.key = Preconditions.checkNotNull(key);
        this.controller = controller;
        this.configuration = configuration;
        name = configuration.getString("name");
        description = configuration.getString("description");
        interval = configuration.getInt("interval", 0);
        if (configuration.isList("effects")) {
            effects = controller.loadEffects(configuration, "effects");
        } else {
            String effectKey = configuration.getString("effects");
            if (effectKey != null) {
                effects = controller.getEffects(effectKey);
                if (effects.isEmpty()) {
                    effects = null;
                }
            }
        }

        if (configuration.contains("spawn")) {
            spawner = new Spawner(controller, this, configuration.getConfigurationSection("spawn"));
        }

        if (configuration.contains("cast")) {
            caster = new Caster(this, configuration.getConfigurationSection("cast"));
        }

        // Common parameters
        playerRange = configuration.getInt("player_range", 64);
        minPlayers = configuration.getInt("min_players", 0);
        minTimeOfDay = parseTime(configuration, "min_time", controller.getLogger());
        maxTimeOfDay = parseTime(configuration, "max_time", controller.getLogger());
        if (configuration.contains("moon_phase")) {
            minPhaseOfMoon = maxPhaseOfMoon = parseMoonPhase(configuration, "moon_phase", controller.getLogger());
        } else {
            minPhaseOfMoon = parseMoonPhase(configuration, "min_moon_phase", controller.getLogger());
            maxPhaseOfMoon = parseMoonPhase(configuration, "max_moon_phase", controller.getLogger());
        }
    }

    @Nullable
    private Integer parseTime(ConfigurationSection configuration, String key, Logger log) {
        Integer time = null;
        if (configuration.contains(key)) {
            if (configuration.isInt(key)) {
                time = configuration.getInt(key);
            } else {
                String timeString = configuration.getString(key);
                if (timeString.equalsIgnoreCase("day")) {
                    time = 0;
                } else if (timeString.equalsIgnoreCase("night")) {
                    time = 13000;
                } else if (timeString.equalsIgnoreCase("dusk") || timeString.equalsIgnoreCase("sunset")) {
                    time = 12000;
                } else if (timeString.equalsIgnoreCase("dawn") || timeString.equalsIgnoreCase("sunrise")) {
                    time = 23000;
                } else if (timeString.equalsIgnoreCase("noon") || timeString.equalsIgnoreCase("midday")) {
                    time = 6000;
                } else if (timeString.equalsIgnoreCase("midnight")) {
                    time = 18000;
                } else {
                    log.warning("Invalid time in automata config: " + timeString);
                }
            }
        }

        return time;
    }

    @Nullable
    private Integer parseMoonPhase(ConfigurationSection configuration, String key, Logger log) {
        Integer phase = null;
        if (configuration.contains(key)) {
            if (configuration.isInt(key)) {
                phase = configuration.getInt(key);
            } else {
                String phaseString = configuration.getString(key);
                if (phaseString.equalsIgnoreCase("new")) {
                    phase = 4;
                } else if (phaseString.equalsIgnoreCase("full")) {
                    phase = 0;
                } else {
                    log.warning("Invalid phase of moon in automata config: " + phaseString);
                }
            }
        }

        return phase;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nonnull
    public String getName() {
        String name = this.name;
        return name == null ? key : name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public int getInterval() {
        return interval;
    }

    public void tick(Automaton instance) {
        boolean isActive = checkActive(instance.getLocation());
        boolean firstActivate = false;
        if (isActive) {
            if (!instance.isActive()) {
                firstActivate = true;
                instance.activate();
            }
        } else {
            if (instance.isActive()) {
                instance.deactivate();
            }
            return;
        }

        if (spawner != null) {
            if (instance.getTimeToNextSpawn() <= 0) {
                instance.spawn();
            }
            instance.checkEntities();
        }

        if (caster != null && (caster.isRecast() || firstActivate)) {
            caster.cast(instance.getMage());
        }
    }

    public AutomatonTemplate getVariant(ConfigurationSection parameters) {
        ConfigurationSection mergedConfiguration = ConfigurationUtils.cloneConfiguration(configuration);
        mergedConfiguration = ConfigurationUtils.addConfigurations(mergedConfiguration, parameters);
        return new AutomatonTemplate(controller, key, mergedConfiguration);
    }

    @Nullable
    public Collection<EffectPlayer> getEffects() {
        return effects;
    }

    private boolean checkRange(Integer min, Integer max, int value) {
        if (min != null) {
            if (max != null && max < min) {
                if (value < min && value > max) {
                    return false;
                }
            } else {
                if (value < min) {
                    return false;
                }
            }
        }
        if (max != null && value > max && (min == null || min <= max)) {
            return false;
        }

        return true;
    }

    public boolean checkActive(Location location) {
        if (!checkRange(minTimeOfDay, maxTimeOfDay, (int)location.getWorld().getTime())) {
            return false;
        }
        if (!checkRange(minPhaseOfMoon, maxPhaseOfMoon, (int)((location.getWorld().getFullTime() / 24000) % 8))) {
            return false;
        }

        if (minPlayers >= 0 && playerRange > 0) {
            int playerCount = 0;
            int rangeSquared = playerRange * playerRange;
            List<Player> players = location.getWorld().getPlayers();
            for (Player player : players) {
                if (player.getLocation().distanceSquared(location) <= rangeSquared) {
                    playerCount++;
                }
            }

            if (playerCount < minPlayers) {
                return false;
            }
        }

        return true;
    }

    public boolean isUndoAll() {
        return caster != null && caster.isUndoAll();
    }

    @Nullable
    public Spawner getSpawner() {
        return spawner;
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }
}
