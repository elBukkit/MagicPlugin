package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BaseMagicProperties implements MagicProperties {

    protected final @Nonnull MagicController controller;
    private ConfigurationSection configuration = new MemoryConfiguration();
    private ConfigurationSection effectiveConfiguration = new MemoryConfiguration();
    private BaseMagicProperties parent;
    private boolean dirty = false;

    protected BaseMagicProperties(MageController controller) {
        // Don't really like this, but Wand is very dependent on MagicController
        Preconditions.checkArgument(controller instanceof MagicController);
        this.controller = (MagicController)controller;
    }
    
    public void setProperty(String key, Object value) {
        configuration.set(key, value);
        dirty = true;
    }
    
    private void rebuildEffectiveConfiguration() {
        if (dirty) {
            effectiveConfiguration = ConfigurationUtils.cloneConfiguration(configuration);
            if (parent != null) {
                ConfigurationSection parentConfiguration = parent.getEffectiveConfiguration();
                ConfigurationUtils.addConfigurations(effectiveConfiguration, parentConfiguration, false);
            }
            dirty = false;
        }
    }

    public void load(ConfigurationSection configuration) {
        this.configuration = ConfigurationUtils.cloneConfiguration(configuration);
        dirty = true;
    }

    @Override
    public boolean hasProperty(String key) {
        return effectiveConfiguration.contains(key);
    }

    @Override
    public Optional<Object> getProperty(String key) {
        rebuildEffectiveConfiguration();
        return Optional.fromNullable(effectiveConfiguration.get(key));
    }

    @Override
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        rebuildEffectiveConfiguration();

        Object value = effectiveConfiguration.get(key);
        if(value == null || !type.isInstance(value)) {
            return Optional.absent();
        }

        return Optional.of(type.cast(value));
    }

    @Override
    public <T> T getProperty(String key, T defaultValue) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(defaultValue, "defaultValue");
        rebuildEffectiveConfiguration();

        @SuppressWarnings("unchecked")
        Class<? extends T> clazz = (Class<? extends T>) defaultValue.getClass();

        Object value = effectiveConfiguration.get(key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        return defaultValue;
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public ConfigurationSection getEffectiveConfiguration() {
        rebuildEffectiveConfiguration();
        return effectiveConfiguration;
    }

    public void setParent(BaseMagicProperties properties) {
        this.parent = properties;
        dirty = true;
    }
    
    public void clear() {
        configuration = new MemoryConfiguration();
        effectiveConfiguration = new MemoryConfiguration();
        parent = null;
        dirty = false;
    }
    
    public void configure(ConfigurationSection configuration) {
        ConfigurationUtils.addConfigurations(this.configuration, configuration);
        dirty = true;
    }

    protected boolean upgradeProperty(String key, Object value) {
        return upgradeProperty(key, value, false);
    }

    protected boolean upgradeProperty(String key, Object value, boolean force) {
        Object currentValue = getEffectiveConfiguration().get(key);
        if (currentValue == value) {
            return false;
        }
        if (currentValue != null && value != null && !force) {
            if (currentValue.equals(value)) {
                return false;
            } else if (value instanceof String) {
                String stringValue = (String) value;
                String stringCurrent = (String) currentValue;
                if (stringValue.equalsIgnoreCase(stringCurrent)) return false;
            } else if (value instanceof Number) {
                float floatValue = NumberConversions.toFloat(value);
                float floatCurrent = NumberConversions.toFloat(currentValue);
                // TODO: What about properties (see: block_cooldown) where less is better?
                if (floatCurrent >= floatValue) return false;
            }
        }

        sendDebug("Upgraded property: " + key);
        if (value != null && value instanceof Number) {
            sendAddMessage("upgraded_property", controller.getMessages().getLevelString("wand." + key, NumberConversions.toFloat(value)));
        }
        sendMessage(key + "_usage");
        setProperty(key, value);
        return true;
    }

    protected void addPotionEffects(Map<PotionEffectType, Integer> effects, String effectsString) {
        if (effectsString == null || effectsString.isEmpty()) {
            return;
        }
        effectsString = effectsString.replaceAll("[\\]\\[]", "");
        String[] effectStrings = StringUtils.split(effectsString, ',');
        for (String effectString : effectStrings) {
            try {
                effectString = effectString.trim();
                PotionEffectType type;
                int power = 0;
                if (effectString.contains(":")) {
                    String[] pieces = StringUtils.split(effectString, ':');
                    type = PotionEffectType.getByName(pieces[0].toUpperCase());
                    power = Integer.parseInt(pieces[1]);
                } else {
                    type = PotionEffectType.getByName(effectString.toUpperCase());
                }
                if (type == null) {
                    throw new Exception("Invalid potion effect");
                }

                Integer existing = effects.get(type);
                if (existing == null || existing < power) {
                    effects.put(type, power);
                }
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Invalid potion effect: " + effectString);
            }
        }
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

    protected boolean upgradePotionEffects(String key, Object value) {
        if (!(value instanceof String)) return false;
        boolean modified = false;
        String currentValue = getEffectiveConfiguration().getString(key);
        Map<PotionEffectType, Integer> currentEffects = new HashMap<>();
        Map<PotionEffectType, Integer> newEffects = new HashMap<>();
        addPotionEffects(currentEffects, currentValue);
        addPotionEffects(newEffects, (String)value);

        for (Map.Entry<PotionEffectType, Integer> otherEffects : newEffects.entrySet()) {
            Integer current = currentEffects.get(otherEffects.getKey());
            if (current == null || current < otherEffects.getValue()) {
                currentEffects.put(otherEffects.getKey(), otherEffects.getValue());
                sendAddMessage("upgraded_property", describePotionEffect(otherEffects.getKey(), otherEffects.getValue()));
                sendDebug("Added potion effect: " + otherEffects.getKey());
                modified = true;
            }
        }
        if (modified) {
            setProperty("potion_effects", getPotionEffectString(currentEffects));
        }
        return modified;
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

    protected boolean organizeInventory() {
        return false;
    }

    protected boolean alphabetizeInventory() {
        return false;
    }

    public boolean addBrush(String materialKey) {
        return false;
    }

    public boolean upgradeBrushes(Object value) {
        if (!(value instanceof String) && !(value instanceof List)) return false;
        boolean modified = false;

        // Add materials
        @SuppressWarnings("unchecked")
        Collection<String> materials = value instanceof String ?
                  Arrays.asList(StringUtils.split((String)value))
                : (List<String>)value;
        for (String materialKey : materials) {
            materialKey = StringUtils.split(materialKey, '@')[0].trim();
            if (addBrush(materialKey)) {
                modified = true;
                sendDebug("Added brush: " + materialKey);
            }
        }

        return modified;
    }

    public boolean addOverride(String key, String value) {
        return false;
    }

    public boolean upgradeOverrides(Object value) {
        boolean modified = false;

        @SuppressWarnings("unchecked")
        Collection<String> overrides = value instanceof String ?
                Arrays.asList(StringUtils.split((String)value, ','))
                : (List<String>)value;

        Set<String> upgradedSpells = new HashSet<>();
        for (String override : overrides) {
            override = override.replace("\\|", ",");
            String[] pairs = StringUtils.split(override, ' ');
            if (pairs.length > 1) {
                if (addOverride(pairs[0], pairs[1])) {
                    sendDebug("Added override: " + pairs[0] + " " + pairs[1]);
                    String[] pieces = StringUtils.split(pairs[0], '.');
                    if (pieces.length > 1 && !upgradedSpells.contains(pieces[0])) {
                        upgradedSpells.add(pieces[0]);
                        SpellTemplate spell = controller.getSpellTemplate(pieces[0]);
                        if (spell != null) {
                            sendAddMessage("spell_override_upgraded", spell.getName());
                        }
                    }
                    modified = true;
                }
            }
        }

        return modified;
    }

    public boolean addSpell(String spellKey) {
        return false;
    }

    public boolean upgradeSpells(Object value) {
        if (!(value instanceof String)) return false;
        boolean modified = false;

        @SuppressWarnings("unchecked")
        Collection<String> spells = value instanceof String ?
                Arrays.asList(StringUtils.split((String)value))
                : (List<String>)value;
        for (String spellKey : spells) {
            spellKey = StringUtils.split(spellKey,'@')[0].trim();
            if (addSpell(spellKey)) {
                modified = true;
                sendDebug("Added spell: " + spellKey);
            }
        }
        return modified;
    }

    public boolean upgrade(ConfigurationSection configuration) {

        boolean modified = false;
        Set<String> keys = configuration.getKeys(false);
        for (String key : keys) {
            Object value = configuration.get(key);
            switch (key) {
                // Special-case properties first
                case "quiet":
                    modified = upgradeProperty(key, value, true) || modified;
                    break;
                case "potion_effects":
                    modified = upgradePotionEffects(key, value);
                    break;

                // Mana modifiers don't need to apply if cost-free
                case "mana":
                case "mana_regeneration":
                case "mana_max":
                case "mana_per_damage":
                    double costReduction = getEffectiveConfiguration().getDouble("cost_reduction", 0.0);
                    if (costReduction <= 1) {
                        modified = upgradeProperty(key, value) || modified;
                    }
                    break;

                // This may parse as a numeric value, which we don't want.
                case "effect_color":
                    if (value instanceof String) {
                        ColorHD newColor = new ColorHD((String)value);
                        modified = upgradeProperty(key, newColor.toString(), true) || modified;
                    }
                    break;

                // Organizing and alphabetizing require special behavior
                case "organize":
                    if (organizeInventory()) {
                        modified = true;
                        sendMessage(getMessage("reorganized"));
                    }
                    break;
                case "alphabetize":
                    if (alphabetizeInventory()) {
                        modified = true;
                        sendMessage(getMessage("alphabetized"));
                    }
                    break;

                // Spells, overrides and brushes need merging
                case "spells":
                    modified = upgradeSpells(value) || modified;
                    break;
                case "materials":
                    modified = upgradeBrushes(value) || modified;
                    break;
                case "overrides":
                    modified = upgradeOverrides(value) || modified;
                    break;

                // Default behavior is to replace any null or string values
                // And to only increase numeric values
                default:
                    modified = upgradeProperty(key, value) || modified;
            }
        }

        dirty = dirty || modified;
        return modified;
    }
}
