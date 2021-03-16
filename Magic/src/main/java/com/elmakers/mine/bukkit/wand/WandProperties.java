package com.elmakers.mine.bukkit.wand;

import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.magic.MageClass;
import com.elmakers.mine.bukkit.magic.TemplatedProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.collect.ImmutableSet;

public abstract class WandProperties extends TemplatedProperties {
    public static final ImmutableSet<String> PROPERTY_KEYS = new ImmutableSet.Builder<String>()
        .addAll(BaseMagicProperties.PROPERTY_KEYS)
        .add(
            "modifiers", "enchantments", "item_attributes", "item_attribute_slot", "auto_absorb",
            "limit_spells_to_path", "limit_brushes_to_path", "level_spells",
            "active_spell", "active_brush", "worn",
            "alternate_spell", "alternate_spell2", "alternate_spell3", "alternate_spell4",
            "alternate_spell5", "alternate_spell6", "alternate_spell7",
            "bound", "has_uses", "uses", "upgrade", "indestructible", "use_mode",
            "undroppable", "boostable",
            "effect_bubbles", "effect_color",
            "effect_particle", "effect_particle_count", "effect_particle_data",
            "effect_particle_interval",
            "effect_particle_min_velocity",
            "effect_particle_radius", "effect_particle_offset",
            "effect_sound", "effect_sound_interval",
            "cast_spell", "cast_parameters", "cast_interval", "cast_interval_cost_free",
            "cast_min_velocity", "cast_velocity_direction", "cast_min_bowpull",
            "icon_inactive", "icon_inactive_delay", "mode",
            "active_effects", "cancel_effects_delay",
            "brush_mode", "currency_display",
            "keep", "locked", "quiet", "force", "rename", "rename_description",
            "power", "heroes",
            "enchant_count", "max_enchant_count", "cast_location",
            "quick_cast",
            "left_click", "right_click", "drop", "swap",
            "left_click_sneak", "right_click_sneak", "drop_sneak", "swap_sneak",
            "block_fov", "block_chance", "block_reflect_chance", "block_mage_cooldown", "block_cooldown",
            "unique", "track", "invulnerable", "immortal", "inventory_rows",
            "class", "classes", "consume_spell", "stack", "unstashable", "unmoveable",
            "page_free_space", "enchantable", "hide_flags", "slot", "slots", "slotted",
            "use_active_name", "interactible", "craftable", "unswappable", "worth", "glow", "spell_glow"
        ).build();
    protected BaseMagicProperties wandTemplate;
    protected MageClass mageClass;

    public WandProperties(MageController controller) {
        super(MagicPropertyType.WAND, controller);
    }

    public void setWandTemplate(WandTemplate template) {
        super.setTemplate(template);
        Mage mage = getMage();
        if (mage != null) {
            template = template.getMageTemplate(mage);
        }
        this.wandTemplate = template;
    }

    public void setMageClass(MageClass mageClass) {
        this.mageClass = mageClass;
    }

    @Override
    public void clear() {
        super.clear();
        wandTemplate = null;
    }

    @Override
    @Nullable
    public Object getInheritedProperty(String key) {
        Object value = super.getInheritedProperty(key);
        if (value == null && mageClass != null) {
            value = mageClass.getInheritedProperty(key);
        }
        return value;
    }

    @Nullable
    @Override
    public ConfigurationSection getConfigurationSection(String key) {
        ConfigurationSection own = super.getConfigurationSection(key);
        ConfigurationSection fromClass = mageClass == null ? null : mageClass.getConfigurationSection(key);

        if (own == null) {
            return fromClass;
        }
        if (fromClass != null) {
            own = ConfigurationUtils.cloneConfiguration(own);
            own = ConfigurationUtils.overlayConfigurations(own, fromClass);
        }
        return own;
    }

    public ConfigurationSection getEffectiveConfiguration() {
        ConfigurationSection effectiveConfiguration = ConfigurationUtils.cloneConfiguration(getConfiguration());
        if (wandTemplate != null) {
            ConfigurationSection parentConfiguration = wandTemplate.getConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, parentConfiguration);
        }
        if (mageClass != null) {
            ConfigurationSection classConfiguration = mageClass.getConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, classConfiguration);
        }
        return effectiveConfiguration;
    }

    @Override
    protected Set<String> getAllPropertyKeys() {
        return PROPERTY_KEYS;
    }
}
