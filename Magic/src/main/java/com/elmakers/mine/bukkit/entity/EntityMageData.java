package com.elmakers.mine.bukkit.entity;

import java.util.Collection;
import java.util.HashSet;
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
import com.elmakers.mine.bukkit.api.magic.TriggerType;
import com.elmakers.mine.bukkit.magic.MobTrigger;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class EntityMageData {
    // These properties will get copied directly to mage data, as if they were in the "mage" section.
    private static final String[] MAGE_PROPERTIES = {"protection", "weakness", "strength"};

    protected long tickInterval;
    protected long lifetime;
    protected @Nullable Multimap<String, MobTrigger> triggers;
    private final Set<String> triggering = new HashSet<>();
    protected ConfigurationSection mageProperties;
    protected boolean requiresTarget;
    protected ItemData requiresWand;
    protected boolean aggro;
    protected double trackRadiusSquared;
    protected boolean isCancelLaunch = true;

    public EntityMageData(@Nonnull MageController controller, @Nonnull ConfigurationSection parameters) {
        requiresWand = controller.getOrCreateItemOrWand(parameters.getString("cast_requires_item"));

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
        requiresTarget = parameters.getBoolean("cast_requires_target", parameters.getBoolean("interval_requires_target", true));
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
            triggers = ArrayListMultimap.create();
            for (String triggerKey : triggerKeys) {
                MobTrigger trigger = new MobTrigger(controller, triggerKey, triggerConfig.getConfigurationSection(triggerKey));
                triggers.put(trigger.getTrigger(), trigger);
            }
        }

        // Default to 1-second interval if any interval triggers are set but no interval was specified
        if (triggers != null && tickInterval <= 0 && triggers.containsKey(TriggerType.INTERVAL.name())) {
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
    private Collection<MobTrigger> getTriggers(TriggerType type) {
        return getTriggers(type.name());
    }

    @Nullable
    private Collection<MobTrigger> getTriggers(String type) {
        return triggers == null ? null : triggers.get(type.toLowerCase());
    }

    public void resetTriggers() {
        triggering.clear();
    }

    public boolean trigger(Mage mage, String triggerKey) {
        if (triggering.contains(triggerKey)) return false;
        triggering.add(triggerKey);
        Collection<MobTrigger> triggers = getTriggers(triggerKey);
        if (triggers == null || triggers.isEmpty()) return false;
        boolean activated = false;
        isCancelLaunch = false;
        for (MobTrigger trigger : triggers) {
            if (trigger.execute(mage)) {
                isCancelLaunch = isCancelLaunch || trigger.isCancelLaunch();
            }
            activated = true;
        }
        return activated;
    }

    public void tick(Mage mage) {
        triggering.clear();
        if (lifetime > 0 && System.currentTimeMillis() > mage.getCreatedTime() + lifetime) {
            trigger(mage, "death");
            mage.getEntity().remove();
            return;
        }

        Collection<MobTrigger> intervalTriggers = getTriggers(TriggerType.INTERVAL);
        if (intervalTriggers == null) return;

        Entity entity = mage.getLivingEntity();
        Creature creature = (entity instanceof Creature) ? (Creature)entity : null;
        if (requiresTarget && (creature == null || creature.getTarget() == null)) return;
        if (requiresWand != null && entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            ItemStack itemInHand = li.getEquipment().getItemInMainHand();
            if (itemInHand == null || itemInHand.getType() != requiresWand.getType()) return;
        }

        for (MobTrigger trigger : intervalTriggers) {
            trigger.execute(mage);
        }
    }

    public double getTrackRadiusSquared() {
        return trackRadiusSquared;
    }
}
