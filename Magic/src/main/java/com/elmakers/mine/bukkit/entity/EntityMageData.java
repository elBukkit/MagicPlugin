package com.elmakers.mine.bukkit.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MageTrigger;
import com.elmakers.mine.bukkit.magic.MageTrigger.MageTriggerType;

public class EntityMageData {
    // These properties will get copied directly to mage data, as if they were in the "mage" section.
    private static final String[] MAGE_PROPERTIES = {"protection", "weakness", "strength"};

    protected long tickInterval;
    protected long lifetime;
    protected Map<String, List<MageTrigger>> triggers;
    protected ConfigurationSection mageProperties;
    protected boolean requiresTarget;
    protected ItemData requiresWand;
    protected boolean aggro;
    protected double trackRadiusSquared;

    public EntityMageData(@Nonnull MageController controller, @Nonnull ConfigurationSection parameters) {
        requiresWand = controller.getOrCreateItem(parameters.getString("cast_requires_item"));

        mageProperties = parameters.getConfigurationSection("mage");

        for (String mageProperty : MAGE_PROPERTIES) {
            ConfigurationSection mageConfig = parameters.getConfigurationSection(mageProperty);
            if (mageConfig != null) {
                if (mageProperties == null) {
                    mageProperties = new MemoryConfiguration();
                }
                mageProperties.set(mageProperty, mageConfig);
            }
        }

        lifetime = parameters.getInt("lifetime", 0);
        tickInterval = parameters.getLong("interval", parameters.getLong("cast_interval", 0));
        requiresTarget = parameters.getBoolean("cast_requires_target", true);
        trackRadiusSquared = parameters.getDouble("track_radius", 128);
        trackRadiusSquared = trackRadiusSquared * trackRadiusSquared;

        ConfigurationSection triggerConfig = parameters.getConfigurationSection("triggers");

        // Support legacy "cast" format
        if (parameters.contains("cast")) {
            if (triggerConfig == null) {
                triggerConfig = new MemoryConfiguration();
            }
            ConfigurationSection castSection = triggerConfig.createSection("interval");
            castSection.set("cast", parameters.getConfigurationSection("cast"));
        }

        Set<String> triggerKeys = triggerConfig == null ? null : triggerConfig.getKeys(false);
        if (triggerKeys != null) {
            triggers = new HashMap<>();
            for (String triggerKey : triggerKeys) {
                MageTrigger trigger = new MageTrigger(controller, triggerKey, triggerConfig.getConfigurationSection(triggerKey));
                List<MageTrigger> typeTriggers = triggers.get(trigger.getType());
                if (typeTriggers == null) {
                    typeTriggers = new ArrayList<>();
                    triggers.put(trigger.getType(), typeTriggers);
                }
                typeTriggers.add(trigger);
            }
        }

        // Default to 1-second interval if any interval triggers are set but no interval was specified
        if (triggers != null && tickInterval <= 0 && triggers.containsKey(MageTriggerType.INTERVAL.name())) {
            tickInterval = 1000;
        }
        if (tickInterval < lifetime / 2) {
            tickInterval = lifetime / 2;
        }

        aggro = parameters.getBoolean("aggro", !isEmpty());
    }

    public boolean isEmpty() {
        boolean hasTriggers = triggers != null;
        boolean hasProperties = mageProperties != null;
        boolean hasLifetime = lifetime > 0;
        return !hasProperties && !hasTriggers && !aggro && !hasLifetime;
    }

    @Nullable
    private List<MageTrigger> getTriggers(MageTriggerType type) {
        return getTriggers(type.name());
    }

    @Nullable
    private List<MageTrigger> getTriggers(String type) {
        return triggers == null ? null : triggers.get(type);
    }

    public boolean trigger(Mage mage, String triggerKey) {
        List<MageTrigger> triggers = getTriggers(triggerKey);
        if (triggers == null || triggers.isEmpty()) return false;
        for (MageTrigger trigger : triggers) {
            trigger.execute(mage);
        }
        return true;
    }

    public void onDeath(Mage mage) {
        List<MageTrigger> deathTriggers = getTriggers(MageTriggerType.DEATH);
        if (deathTriggers == null) return;
        for (MageTrigger trigger : deathTriggers) {
            trigger.execute(mage);
        }
    }

    public boolean onLaunch(Mage mage, double bowpull) {
        List<MageTrigger> launchTriggers = getTriggers(MageTriggerType.LAUNCH);
        if (launchTriggers == null) return false;

        for (MageTrigger trigger : launchTriggers) {
            trigger.execute(mage, 0, bowpull);
        }

        return true;
    }

    public void onDamage(Mage mage, double damage) {
        List<MageTrigger> damageTriggers = getTriggers(MageTriggerType.DAMAGE);
        if (damageTriggers == null) return;
        for (MageTrigger trigger : damageTriggers) {
            trigger.execute(mage, damage);
        }
    }

    public void onSpawn(Mage mage) {
        List<MageTrigger> spawnTriggers = getTriggers(MageTriggerType.SPAWN);
        if (spawnTriggers != null) {
            for (MageTrigger trigger : spawnTriggers) {
                trigger.execute(mage);
            }
        }
    }

    public void tick(Mage mage) {
        if (lifetime > 0 && System.currentTimeMillis() > mage.getCreatedTime() + lifetime) {
            onDeath(mage);
            mage.getEntity().remove();
            return;
        }

        List<MageTrigger> intervalTriggers = getTriggers(MageTriggerType.INTERVAL);
        if (intervalTriggers == null) return;

        Entity entity = mage.getLivingEntity();
        Creature creature = (entity instanceof Creature) ? (Creature)entity : null;
        if (requiresTarget && (creature == null || creature.getTarget() == null)) return;
        if (requiresWand != null && entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            ItemStack itemInHand = li.getEquipment().getItemInMainHand();
            if (itemInHand == null || itemInHand.getType() != requiresWand.getType()) return;
        }

        for (MageTrigger trigger : intervalTriggers) {
            trigger.execute(mage);
        }
    }

    public double getTrackRadiusSquared() {
        return trackRadiusSquared;
    }
}
