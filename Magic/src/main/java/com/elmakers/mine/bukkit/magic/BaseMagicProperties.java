package com.elmakers.mine.bukkit.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class BaseMagicProperties implements MagicProperties {

    protected final @Nonnull MagicController controller;
    protected ConfigurationSection configuration = new MemoryConfiguration();

    public static final ImmutableSet<String> PROPERTY_KEYS = ImmutableSet.of(
            "active_spell", "active_brush", "alternate_spell", "alternate_spell2",
            "path", "template", "passive",
            "mana", "mana_regeneration", "mana_max", "mana_max_boost",
            "mana_regeneration_boost",
            "mana_per_damage",
            "bound", "has_uses", "uses", "upgrade", "indestructible",
            "undroppable", "boostable",
            "consume_reduction", "cost_reduction", "cooldown_reduction",
            "effect_bubbles", "effect_color",
            "effect_particle", "effect_particle_count", "effect_particle_data",
            "effect_particle_interval",
            "effect_particle_min_velocity",
            "effect_particle_radius", "effect_particle_offset",
            "effect_sound", "effect_sound_interval",
            "cast_spell", "cast_parameters", "cast_interval",
            "cast_min_velocity", "cast_velocity_direction", "cast_min_bowpull",
            "hotbar_count", "hotbar",
            "icon", "icon_inactive", "icon_inactive_delay", "mode",
            "active_effects", "cancel_effects_delay",
            "brush_mode", "currency_display",
            "keep", "locked", "quiet", "force", "rename",
            "rename_description",
            "power", "overrides",
            "protection", "potion_effects", "item_attributes",
            "brushes", "brush_inventory", "spells", "spell_inventory", "spell_levels",
            "powered", "protected", "heroes",
            "enchant_count", "max_enchant_count",
            "quick_cast", "left_click", "right_click", "drop", "swap",
            "block_fov", "block_chance", "block_reflect_chance", "block_mage_cooldown", "block_cooldown",
            "unique", "track", "invulnerable", "immortal", "inventory_rows", "cast_location",
            "earn_multiplier", "class", "classes",
            "consume_spell", "stack", "unstashable", "unmoveable", "attributes"
    );

    public static final ImmutableSet<String> HIDDEN_PROPERTY_KEYS = ImmutableSet.of(
            "owner", "owner_id", "version", "item_attributes", "item_attribute_slot",
            "mana_timestamp", "storage"
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
        return hasOwnProperty(key);
    }

    @Override
    @Nullable
    public Object getProperty(String key) {
        return configuration.get(key);
    }

    @Override
    @Nullable
    public <T> T getProperty(String key, Class<T> type) {
        Object value = getProperty(key);
        if (value == null || !type.isInstance(value)) {
            return null;
        }

        return type.cast(value);
    }

    @Override
    @Nonnull
    public <T> T getProperty(String key, T defaultValue) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(defaultValue, "defaultValue");

        @SuppressWarnings("unchecked")
        Class<? extends T> clazz = (Class<? extends T>) defaultValue.getClass();

        Object value = getProperty(key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        if (value != null && value instanceof Number && defaultValue instanceof Number) {
            if (defaultValue instanceof Double) {
                return clazz.cast(NumberConversions.toDouble(value));
            } else if (defaultValue instanceof Integer) {
                return clazz.cast(NumberConversions.toInt(value));
            } else if (defaultValue instanceof Byte) {
                return clazz.cast(NumberConversions.toByte(value));
            } else if (defaultValue instanceof Float) {
                return clazz.cast(NumberConversions.toFloat(value));
            } else if (defaultValue instanceof Long) {
                return clazz.cast(NumberConversions.toLong(value));
            } else if (defaultValue instanceof Short) {
                return clazz.cast(NumberConversions.toShort(value));
            }
        }

        return defaultValue;
    }

    @Nullable
    public Object getObject(String key, Object defaultValue) {
        Object value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    @Nullable
    public Object getObject(String key) {
        return getProperty(key);
    }

    public double getDouble(String key, double defaultValue) {
        Object value = getProperty(key);
        return value == null || !(value instanceof Number) ? defaultValue : NumberConversions.toDouble(value);
    }

    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    public float getFloat(String key, float defaultValue) {
        Object value = getProperty(key);
        return value == null || !(value instanceof Number) ? defaultValue : NumberConversions.toFloat(value);
    }

    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public int getInt(String key, int defaultValue) {
        Object value = getProperty(key);
        return value == null || !(value instanceof Number) ? defaultValue : NumberConversions.toInt(value);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        Object value = getProperty(key);
        return value == null || !(value instanceof Number) ? defaultValue : NumberConversions.toLong(value);
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = getProperty(key);
        return value == null || !(value instanceof Boolean) ? defaultValue : (boolean)value;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public String getString(String key, String defaultValue) {
        Object value = getProperty(key);
        return value == null ? defaultValue : value.toString();
    }

    public String getString(String key) {
        return getString(key, null);
    }

    @Nullable
    public List<String> getStringList(String key) {
        Object value = getProperty(key);
        return ConfigurationUtils.getStringList(value);
    }

    @Nullable
    public ConfigurationSection getConfigurationSection(String key) {
        Object value = getProperty(key);
        return value == null || !(value instanceof ConfigurationSection) ? null : (ConfigurationSection)value;
    }

    @Nullable
    public Vector getVector(String key, Vector def) {
        String stringData = getString(key, null);
        if (stringData == null) {
            return def;
        }

        return ConfigurationUtils.toVector(stringData);
    }

    @Nullable
    public Vector getVector(String key) {
        return getVector(key, null);
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    @Nullable
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
        return parameterizeMessage(controller.getMessages().get(getMessageKey(messageKey), defaultValue));
    }

    protected String getMessageKey(String messageKey) {
        return messageKey;
    }

    protected String parameterizeMessage(String message) {
        return message;
    }

    public static String describeProperty(Object property) {
        return InventoryUtils.describeProperty(property, InventoryUtils.MAX_PROPERTY_DISPLAY_LENGTH);
    }

    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties, @Nullable Set<String> overriddenProperties) {
        ConfigurationSection itemConfig = getConfiguration();
        Set<String> keys = itemConfig.getKeys(false);
        for (String key : keys) {
            Object value = itemConfig.get(key);
            if (value != null && (ignoreProperties == null || !ignoreProperties.contains(key))) {
                ChatColor propertyColor = ChatColor.GRAY;
                if (overriddenProperties == null || !overriddenProperties.contains(key)) {
                    propertyColor = PROPERTY_KEYS.contains(key) ? ChatColor.DARK_AQUA : ChatColor.DARK_GREEN;
                }

                sender.sendMessage(propertyColor.toString() + key + ChatColor.GRAY + ": " + ChatColor.WHITE + describeProperty(value));
            }
        }
    }

    @Override
    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties) {
        describe(sender, ignoreProperties, null);
    }

    @Override
    public void describe(CommandSender sender) {
        describe(sender, null);
    }

    /**
     * This is used in some very specific cases where properties coming from a config file should not
     * really be part of the config, and are more meta config.
     */
    protected void clearProperty(String key) {
        configuration.set(key, null);
    }

    @Override
    public boolean isEmpty() {
        return configuration.getKeys(false).isEmpty();
    }
}
