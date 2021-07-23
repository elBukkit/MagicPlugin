package com.elmakers.mine.bukkit.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.TriggerType;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.magic.MageConversation;
import com.elmakers.mine.bukkit.magic.MobTrigger;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class EntityMageData {
    // These properties will get copied directly to mage data, as if they were in the "mage" section.
    private static final String[] MAGE_PROPERTIES = {"protection", "weakness", "strength"};

    protected MobTargeting targeting;
    protected long tickInterval;
    protected long lifetime;
    protected int leashRange;
    protected @Nullable Multimap<String, MobTrigger> triggers;
    private final Set<String> triggering = new HashSet<>();
    protected ConfigurationSection mageProperties;
    protected boolean requiresTarget;
    protected ItemData requiresWand;
    protected boolean aggro;
    protected double trackRadiusSquared;
    protected boolean isCancelLaunch = true;
    protected List<String> dialog;
    protected double dialogRadius;

    public EntityMageData(@Nonnull MageController controller, @Nonnull ConfigurationSection parameters) {
        requiresWand = controller.getOrCreateItem(parameters.getString("cast_requires_item"));

        targeting = MobTargeting.getFromMobConfig(controller, parameters);
        mageProperties = parameters.getConfigurationSection("mage");

        for (String mageProperty : MAGE_PROPERTIES) {
            ConfigurationSection mageConfig = parameters.getConfigurationSection(mageProperty);
            if (mageConfig != null) {
                if (mageProperties == null) {
                    mageProperties = ConfigurationUtils.newConfigurationSection();
                }
                mageProperties.set(mageProperty, mageConfig);
            }
        }

        lifetime = parameters.getInt("lifetime", 0);
        tickInterval = parameters.getLong("interval", parameters.getLong("cast_interval", 0));
        requiresTarget = parameters.getBoolean("cast_requires_target", parameters.getBoolean("interval_requires_target", true));
        trackRadiusSquared = parameters.getDouble("track_radius", 128);
        trackRadiusSquared = trackRadiusSquared * trackRadiusSquared;

        dialog = ConfigurationUtils.getStringList(parameters, "dialog");
        dialogRadius = parameters.getDouble("dialog_range", 3);
        int dialogInterval = parameters.getInt("dialog_interval", 2000);
        if (hasDialog()) {
            tickInterval = Math.max(tickInterval, dialogInterval);
        }

        ConfigurationSection triggerConfig = parameters.getConfigurationSection("triggers");

        // Support legacy "cast" format
        if (parameters.contains("cast")) {
            if (triggerConfig == null) {
                triggerConfig = ConfigurationUtils.newConfigurationSection();
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
        if (triggers != null && tickInterval <= 0 && triggers.containsKey(TriggerType.INTERVAL.name().toLowerCase())) {
            tickInterval = 1000;
        }
        if (lifetime > 0 && tickInterval > lifetime / 2) {
            tickInterval = lifetime / 2;
        }

        aggro = parameters.getBoolean("aggro", !isEmpty());
        leashRange = parameters.getInt("leash_range", 0);
    }

    public boolean isEmpty() {
        boolean hasTriggers = triggers != null;
        boolean hasProperties = mageProperties != null;
        boolean hasLifetime = lifetime > 0;
        boolean hasTargeting = targeting != null;
        boolean hasLeash = leashRange > 0;
        return !hasProperties && !hasTriggers && !aggro && !hasLifetime && !hasDialog() && !hasTargeting && !hasLeash;
    }

    public boolean hasDialog() {
        return dialog != null && !dialog.isEmpty();
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
        if (leashRange > 0) {
            // This currently only works on NPCs
            Entity entity = mage.getEntity();
            MagicNPC npc = mage.getController().getNPC(entity);
            if (npc != null) {
                Location npcLocation = npc.getLocation();
                Location entityLocation = entity.getLocation();
                if (!npcLocation.getWorld().equals(entityLocation.getWorld()) || npcLocation.distanceSquared(entityLocation) > leashRange * leashRange) {
                    CompatibilityLib.getCompatibilityUtils().teleportWithVehicle(entity, npcLocation);
                }
            }
        }

        Collection<MobTrigger> intervalTriggers = getTriggers(TriggerType.INTERVAL);
        if (intervalTriggers != null) {
            boolean isValid = true;
            Entity entity = mage.getLivingEntity();
            Creature creature = (entity instanceof Creature) ? (Creature)entity : null;
            if (requiresTarget && (creature == null || creature.getTarget() == null)) {
                isValid = false;
            }
            if (isValid && requiresWand != null && entity instanceof LivingEntity) {
                LivingEntity li = (LivingEntity)entity;
                ItemStack itemInHand = li.getEquipment().getItemInMainHand();
                if (itemInHand == null || itemInHand.getType() != requiresWand.getType()) {
                    isValid = false;
                }
            }

            if (isValid) {
                for (MobTrigger trigger : intervalTriggers) {
                    trigger.execute(mage);
                }
            }
        }

        if (hasDialog() && mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            com.elmakers.mine.bukkit.magic.Mage speaker = (com.elmakers.mine.bukkit.magic.Mage)mage;
            Map<Player, MageConversation> conversations = speaker.getConversations();
            Map<Player, MageConversation> progress = conversations.isEmpty() ? conversations : new HashMap<>(conversations);
            conversations.clear();

            Location targetLocation = null;
            Collection<Entity> nearby = mage.getLocation().getWorld().getNearbyEntities(mage.getLocation(), dialogRadius, dialogRadius, dialogRadius);
            for (Entity targetEntity : nearby) {
                if (!(targetEntity instanceof Player) || mage.getController().isNPC(targetEntity)) continue;
                Player targetPlayer = (Player)targetEntity;
                MageConversation conversation = progress.get(targetPlayer);
                if (conversation == null) {
                    conversation = new MageConversation(speaker, targetPlayer);
                }
                conversation.sayNextLine(dialog);
                conversations.put(targetPlayer, conversation);
                if (targetLocation == null) {
                    targetLocation = targetPlayer.getLocation().clone();
                } else {
                    targetLocation.add(targetPlayer.getLocation()).multiply(0.5);
                }
            }

            if (targetLocation != null) {
                Entity entity = mage.getEntity();
                if (entity != null && entity.isValid()) {
                    Location location = entity.getLocation();
                    Vector direction = targetLocation.toVector().subtract(location.toVector());
                    location.setDirection(direction);
                    entity.teleport(location);
                }
            }
        }

        if (targeting != null) {
            targeting.tick(mage);
        }
    }

    public double getTrackRadiusSquared() {
        return trackRadiusSquared;
    }

    public boolean canTarget(Entity target) {
        if (target == null || targeting == null) return true;
        return targeting.canTarget(target);
    }

    public boolean isFriendly(Entity target) {
        if (target == null || targeting == null) return false;
        return targeting.isFriendly(target);
    }
}
