package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class BaseMagicProperties implements MagicProperties {

    protected final @Nonnull MagicController controller;
    protected ConfigurationSection configuration = new MemoryConfiguration();

    private static int MAX_PROPERTY_DISPLAY_LENGTH = 50;
    public final static Set<String> PROPERTY_KEYS = ImmutableSet.of(
            "active_spell", "active_brush",
            "path", "template", "passive",
            "mana", "mana_regeneration", "mana_max", "mana_max_boost",
            "mana_regeneration_boost",
            "mana_per_damage",
            "bound", "soul", "has_uses", "uses", "upgrade", "indestructible",
            "undroppable",
            "consume_reduction", "cost_reduction", "cooldown_reduction",
            "effect_bubbles", "effect_color",
            "effect_particle", "effect_particle_count", "effect_particle_data",
            "effect_particle_interval",
            "effect_particle_min_velocity",
            "effect_particle_radius", "effect_particle_offset",
            "effect_sound", "effect_sound_interval",
            "cast_spell", "cast_parameters", "cast_interval",
            "cast_min_velocity", "cast_velocity_direction",
            "hotbar_count", "hotbar",
            "icon", "icon_inactive", "icon_inactive_delay", "mode",
            "active_effects",
            "brush_mode",
            "keep", "locked", "quiet", "force", "rename",
            "rename_description",
            "power", "overrides",
            "protection", "protection_physical", "protection_projectiles",
            "protection_falling", "protection_fire", "protection_explosions",
            "potion_effects",
            "brushes", "brush_inventory", "spells", "spell_inventory", "spell_levels",
            "powered", "protected", "heroes",
            "enchant_count", "max_enchant_count",
            "quick_cast", "left_click", "right_click", "drop", "swap",
            "block_fov", "block_chance", "block_reflect_chance", "block_mage_cooldown", "block_cooldown",
            "unique", "track", "invulnerable", "immortal", "inventory_rows", "cast_location",
            "sp_multiplier", "class", "consume_spell"
    );

    public final static Set<String> HIDDEN_PROPERTY_KEYS = ImmutableSet.of(
            "owner", "owner_id", "version", "attributes", "attribute_slot",
            "mana_timestamp", "property_holders"
    );

    protected BaseMagicProperties(@Nonnull MageController controller) {
        // Don't really like this, but Wand is very dependent on MagicController
        Preconditions.checkArgument(controller instanceof MagicController);
        this.controller = (MagicController)controller;
    }

    public void load(@Nullable ConfigurationSection configuration) {
        this.configuration = ConfigurationUtils.cloneConfiguration(configuration);
    }

    public boolean hasOwnProperty(String key) {
        return configuration.contains(key);
    }

    @Override
    public boolean hasProperty(String key) {
        return getEffectiveConfiguration().contains(key);
    }

    @Override
    public Object getProperty(String key) {
        return getEffectiveConfiguration().get(key);
    }

    @Override
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        Object value = getEffectiveConfiguration().get(key);
        if(value == null || !type.isInstance(value)) {
            return Optional.absent();
        }

        return Optional.of(type.cast(value));
    }

    @Override
    public <T> T getProperty(String key, T defaultValue) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(defaultValue, "defaultValue");

        @SuppressWarnings("unchecked")
        Class<? extends T> clazz = (Class<? extends T>) defaultValue.getClass();

        Object value = getEffectiveConfiguration().get(key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        return defaultValue;
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public ConfigurationSection getEffectiveConfiguration() {
        return configuration;
    }
    
    public void clear() {
        configuration = new MemoryConfiguration();
    }

    protected static String getPotionEffectString(Map<PotionEffectType, Integer> potionEffects) {
        if (potionEffects.size() == 0) return null;
        Collection<String> effectStrings = new ArrayList<>();
        for (Map.Entry<PotionEffectType, Integer> entry : potionEffects.entrySet()) {
            String effectString = entry.getKey().getName();
            if (entry.getValue() > 0) {
                effectString += ":" + entry.getValue();
            }
            effectStrings.add(effectString);
        }
        return StringUtils.join(effectStrings, ",");
    }

    protected String describePotionEffect(PotionEffectType effect, int level) {
        String effectName = effect.getName();
        String effectFirst = effectName.substring(0, 1);
        effectName = effectName.substring(1).toLowerCase().replace("_", " ");
        effectName = effectFirst + effectName;
        return controller.getMessages().getLevelString("wand.potion_effect", level, 5).replace("$effect", effectName);
    }

    protected void sendDebug(String debugMessage) {
        // Does nothing unless overridden
    }

    protected void sendMessage(String messageKey) {
        // Does nothing unless overridden
    }

    protected void sendAddMessage(String messageKey, String nameParam) {
        String message = getMessage(messageKey).replace("$name", nameParam);
        sendMessage(message);
    }

    protected String getMessage(String messageKey) {
        return getMessage(messageKey, "");
    }

    protected String getMessage(String messageKey, String defaultValue) {
        return controller.getMessages().get(messageKey, defaultValue);
    }

    public static String describeProperty(Object property) {
        return InventoryUtils.describeProperty(property, MAX_PROPERTY_DISPLAY_LENGTH);
    }

    @Override
    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties) {
        ConfigurationSection itemConfig = getConfiguration();
        Set<String> keys = itemConfig.getKeys(false);
        for (String key : keys) {
            Object value = itemConfig.get(key);
            if (value != null && (ignoreProperties == null || !ignoreProperties.contains(key))) {
                String propertyColor = PROPERTY_KEYS.contains(key) ? ChatColor.DARK_AQUA.toString() : ChatColor.DARK_GREEN.toString();
                sender.sendMessage(propertyColor + key + ChatColor.GRAY + ": " + ChatColor.WHITE + describeProperty(value));
            }
        }
    }

    @Override
    public void describe(CommandSender sender) {
        describe(sender, null);
    }
}
