package com.elmakers.mine.bukkit.entity;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MageTrigger;
import com.elmakers.mine.bukkit.magic.MageTrigger.MageTriggerType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityMageData {
    // These properties will get copied directly to mage data, as if they were in the "mage" section.
    private static final String[] MAGE_PROPERTIES = {"protection", "weakness", "strength"};

    protected long tickInterval;
    protected Map<MageTriggerType, List<MageTrigger>> triggers;
    protected ConfigurationSection mageProperties;
    protected boolean requiresTarget;
    protected ItemData requiresWand;
    protected boolean aggro;

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

        tickInterval = parameters.getLong("interval", parameters.getLong("cast_interval", 0));
        requiresTarget = parameters.getBoolean("cast_requires_target", true);
        aggro = parameters.getBoolean("aggro", !isEmpty());

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
                if (trigger.isValid()) {
                    List<MageTrigger> typeTriggers = triggers.get(trigger.getType());
                    if (typeTriggers == null) {
                        typeTriggers = new ArrayList<>();
                        triggers.put(trigger.getType(), typeTriggers);
                    }
                    typeTriggers.add(trigger);
                }
            }
        }

        // Default to 1-second interval if any interval triggers are set but no interval was specified
        if (triggers != null && tickInterval <= 0 && triggers.containsKey(MageTriggerType.INTERVAL)) {
            tickInterval = 1000;
        }
    }

    public boolean isEmpty() {
        boolean hasTriggers = triggers != null;
        boolean hasProperties = mageProperties != null;
        return !hasProperties && !hasTriggers && !aggro;
    }

    private List<MageTrigger> getTriggers(MageTriggerType type) {
        return triggers == null ? null : triggers.get(type);
    }

    public void onDeath(Mage mage) {
        List<MageTrigger> deathTriggers = getTriggers(MageTriggerType.DEATH);
        if (deathTriggers == null) return;
        for (MageTrigger trigger : deathTriggers) {
            trigger.execute(mage);
        }
    }

    public void tick(Mage mage) {
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
}
