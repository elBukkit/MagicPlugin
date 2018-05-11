package com.elmakers.mine.bukkit.automata;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

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
    private int interval;
    @Nullable
    private Spawner spawner;
    @Nullable
    private Caster caster;
    @Nullable
    private Collection<EffectPlayer> effects;

    public AutomatonTemplate(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        this.key = Preconditions.checkNotNull(key);
        this.controller = controller;
        this.configuration = configuration;
        name = configuration.getString("name");
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

    public int getInterval() {
        return interval;
    }

    public void tick(Automaton instance) {
        if (spawner != null) {
            List<Entity> entities = spawner.spawn(instance.getLocation());
            if (entities != null && !entities.isEmpty()) {
                instance.track(entities);
            }
            instance.checkEntities();
        }

        if (caster != null) {
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
}