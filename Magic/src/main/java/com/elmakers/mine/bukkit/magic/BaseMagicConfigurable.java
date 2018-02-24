package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicConfigurable;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public abstract class BaseMagicConfigurable extends BaseMagicProperties implements MagicConfigurable {
    protected final MagicPropertyType type;
    protected final Map<String, MagicPropertyType> propertyRoutes = new HashMap<>();

    public BaseMagicConfigurable(MagicPropertyType type, MageController controller) {
        super(controller);
        this.type = type;
    }

    public void loadProperties() {
        Object storageObject = getInheritedProperty("storage");
        ConfigurationSection routeConfig = storageObject == null || !(storageObject instanceof ConfigurationSection) ? null : (ConfigurationSection)storageObject;
        if (routeConfig != null) {
            Set<String> keys = routeConfig.getKeys(false);
            for (String key : keys) {
                String propertyTypeName = routeConfig.getString(key);
                MagicPropertyType propertyType = null;
                try {
                    propertyType = MagicPropertyType.valueOf(propertyTypeName.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().info("Invalid property type: " + propertyTypeName);
                    continue;
                }
                propertyRoutes.put(key, propertyType);

                // Migrate data if necessary
                if (propertyType != type) {
                    migrateProperty(key, propertyType);
                }
            }
        }
    }
    
    public Object getInheritedProperty(String key) {
        return getProperty(key);    
    }

    protected void migrateProperty(String key, MagicPropertyType propertyType) {
        migrateProperty(key, propertyType, null);
    }

    protected void migrateProperty(String key, MagicPropertyType propertyType, BaseMagicProperties template) {
        Object ownValue = configuration.get(key);
        Object value = ownValue;
        if (value == null && template != null) {
            value = template.getConfiguration().get(key);
        }
        if (value != null) {
            BaseMagicConfigurable storage = getStorage(propertyType);
            if (storage != null) {
                if (ownValue != null) {
                    configuration.set(key, null);
                }
                storage.upgrade(key, value);
            } else {
                controller.getLogger().warning("Attempt to migrate property " + key + " on " + type + " which routes to unavailable storage " + propertyType);
            }
        }
    }

    public void setProperty(String key, Object value) {
        MagicPropertyType propertyType = propertyRoutes.get(key);
        if (propertyType == null || propertyType == type) {
            configuration.set(key, value);
        } else {
            BaseMagicConfigurable storage = getStorage(propertyType);
            if (storage != null) {
                storage.configuration.set(key, value);
            } else {
                controller.getLogger().warning("Attempt to set property " + key + " on " + type + " which routes to unavailable storage " + propertyType);
            }
        }
    }

    @Nullable
    protected BaseMagicConfigurable getStorage(String key) {
        MagicPropertyType propertyType = propertyRoutes.get(key);
        if (propertyType == null || propertyType == type) {
            return null;
        }
        return getStorage(propertyType);
    }

    @Nullable
    protected BaseMagicConfigurable getStorage(MagicPropertyType propertyType) {
        return null;
    }

    protected Object convertProperty(Object value) {
        Object result = value;
        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        if (isTrue || isFalse) {
            result = Boolean.valueOf(isTrue);
        } else {
            try {
                result = Double.valueOf(value instanceof Double?((Double)value).doubleValue():(value instanceof Float?(double)((Float)value).floatValue():Double.parseDouble(value.toString())));
            } catch (Exception notADouble) {
                try {
                    result = Integer.valueOf(value instanceof Integer?((Integer)value).intValue():Integer.parseInt(value.toString()));
                } catch (Exception notAnInteger) {
                }
            }
        }

        return result;
    }

    protected boolean upgradeProperty(String key, Object value) {
        return upgradeProperty(key, value, false);
    }

    protected boolean upgradeProperty(String key, Object value, boolean force) {
        value = convertProperty(value);
        Object currentValue = getProperty(key);
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

    protected boolean upgradePotionEffects(String key, Object value) {
        if (!(value instanceof String)) return false;
        boolean modified = false;
        String currentValue = getProperty(key, "");
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

    protected boolean organizeInventory() {
        return false;
    }

    protected boolean alphabetizeInventory() {
        return false;
    }

    public boolean addBrush(String materialKey) {
        return false;
    }

    private boolean upgradeBrushes(Object value) {
        if (!(value instanceof String) && !(value instanceof List)) return false;
        boolean modified = false;

        // Add materials
        @SuppressWarnings("unchecked")
        Collection<String> materials = value instanceof String ?
                Arrays.asList(StringUtils.split((String)value, ','))
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

    private boolean upgradeOverrides(Object value) {
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

    private boolean upgradeSpells(Object value) {
        if (!(value instanceof String) && !(value instanceof List)) return false;
        boolean modified = false;

        @SuppressWarnings("unchecked")
        Collection<String> spells = value instanceof String ?
                Arrays.asList(StringUtils.split((String)value, ','))
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

    @SuppressWarnings("unchecked")
    protected @Nonnull Map<String, Object> getSpellLevels() {
        Object existingLevelsRaw = getObject("spell_levels");
        if (existingLevelsRaw == null) return new HashMap<>();
        Map<String, Object> spellLevels = null;

        if (existingLevelsRaw instanceof Map) {
            spellLevels = (Map<String, Object>)existingLevelsRaw;
        } else if (existingLevelsRaw instanceof ConfigurationSection) {
            spellLevels = NMSUtils.getMap((ConfigurationSection)existingLevelsRaw);
        }

        return spellLevels;
    }

    protected boolean upgradeSpellLevel(String spellKey, int level) {
        boolean modified = false;
        Map<String, Object> spellLevels = getSpellLevels();
        Object existingLevel = spellLevels.get(spellKey);
        if (existingLevel == null || !(existingLevel instanceof Integer) || level > (Integer)existingLevel) {
            modified = true;
            spellLevels.put(spellKey, level);
        }
        if (modified) {
            setProperty("spell_levels", spellLevels);
        }

        return modified;
    }

    @SuppressWarnings("unchecked")
    private boolean upgradeSpellLevels(Object value) {
        if (!(value instanceof Map) && !(value instanceof ConfigurationSection)) return false;
        boolean modified = false;

        Map<String, Object> spellLevels = getSpellLevels();
        Map<String, Object> newLevels;
        if (value instanceof Map) {
            newLevels = (Map<String, Object>)value;
        } else {
            newLevels = NMSUtils.getMap((ConfigurationSection)value);
        }

        for (Map.Entry<String, ? extends Object> entry : newLevels.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                Integer newLevel = (Integer)entry.getValue();
                String key = entry.getKey();
                Object existingLevel = spellLevels.get(key);
                if (existingLevel == null || !(existingLevel instanceof Integer) || newLevel > (Integer)existingLevel) {
                    modified = true;
                    sendDebug("Upgraded spell level for " + key + " from " + existingLevel + " to " + newLevel);
                    spellLevels.put(key, newLevel);
                }
            }
        }
        if (modified) {
            setProperty("spell_levels", spellLevels);
        }

        return modified;
    }

    @Override
    public void configure(@Nonnull ConfigurationSection configuration) {
        Set<String> keys = configuration.getKeys(true);
        for (String key : keys) {
            Object value = configuration.get(key);
            value = convertProperty(value);
            setProperty(key, value);
        }
        updated();
    }

    public boolean upgrade(String key, Object value) {
        boolean modified = false;
        switch (key) {
            // Special-case properties first
            case "quiet":
                modified = upgradeProperty(key, value, true);
                break;
            case "potion_effects":
                modified = upgradePotionEffects(key, value);
                break;

            // Mana modifiers don't need to apply if cost-free
            case "mana":
            case "mana_regeneration":
            case "mana_max":
            case "mana_per_damage":
                double costReduction = getDouble("cost_reduction", 0.0);
                if (costReduction <= 1) {
                    modified = upgradeProperty(key, value);
                }
                break;

            // This may parse as a numeric value, which we don't want.
            case "effect_color":
                if (value instanceof String) {
                    ColorHD newColor = new ColorHD((String)value);
                    modified = upgradeProperty(key, newColor.toString(), true);
                }
                break;

            // Organizing and alphabetizing require special behavior
            case "organize":
                if (organizeInventory()) {
                    modified = true;
                    sendMessage("reorganized");
                }
                break;
            case "alphabetize":
                if (alphabetizeInventory()) {
                    modified = true;
                    sendMessage("alphabetized");
                }
                break;

            // Spells, overrides and brushes need merging
            case "spells":
                modified = upgradeSpells(value);
                break;
            case "spell_levels":
                modified = upgradeSpellLevels(value);
                break;
            case "materials":
                modified = upgradeBrushes(value);
                break;
            case "overrides":
                modified = upgradeOverrides(value);
                break;

            // Default behavior is to replace any null or string values
            // And to only increase numeric values
            default:
                modified = upgradeProperty(key, value);
        }
        return modified;
    }

    @Override
    public boolean upgrade(ConfigurationSection configuration) {
        boolean modified = false;
        Set<String> keys = configuration.getKeys(false);
        for (String key : keys) {
            Object value = configuration.get(key);
            modified = upgrade(key, value) || modified;
        }

        if (modified) {
            updated();
        }
        return modified;
    }

    public void updated() {

    }

    @Override
    public boolean removeProperty(String key) {
        if (!hasOwnProperty(key)) return false;
        setProperty(key, null);
        updated();
        return true;
    }

    public void clear() {
        configuration = new MemoryConfiguration();
    }
}
