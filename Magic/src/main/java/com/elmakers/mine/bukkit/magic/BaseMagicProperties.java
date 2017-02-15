package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BaseMagicProperties implements MagicProperties {

    protected final @Nonnull MagicController controller;
    private ConfigurationSection configuration = new MemoryConfiguration();
    private ConfigurationSection effectiveConfiguration = new MemoryConfiguration();
    private MagicProperties parent;
    private boolean dirty = false;

    protected BaseMagicProperties(MageController controller) {
        // Don't really like this, but Wand is very dependent on MagicController
        Preconditions.checkArgument(controller instanceof MagicController);
        this.controller = (MagicController)controller;
    }
    
    @Override
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
    public Object getProperty(String key) {
        rebuildEffectiveConfiguration();
        return effectiveConfiguration.get(key);
    }

    @Override
    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    @Override
    public ConfigurationSection getEffectiveConfiguration() {
        rebuildEffectiveConfiguration();
        return effectiveConfiguration;
    }

    @Override
    public void setParent(MagicProperties properties) {
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
        if (currentValue == null || (force && !value.equals(currentValue))) {
            setProperty(key, value);
        } else if (value instanceof String) {
            String stringValue = (String)value;
            String stringCurrent = (String)currentValue;
            if (stringValue.equalsIgnoreCase(stringCurrent)) return false;
        } else if (value instanceof Number) {
            float floatValue = NumberConversions.toFloat(value);
            float floatCurrent = NumberConversions.toFloat(currentValue);
            // TODO: What about properties (see: block_cooldown) where less is better?
            if (floatCurrent >= floatValue) return false;
        } else if (currentValue.equals(value)) {
            return false;
        }

        sendDebug("Upgraded property: " + key);
        if (value instanceof Number) {
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
        String[] effectStrings = StringUtils.split(effectsString, ",");
        for (String effectString : effectStrings) {
            try {
                effectString = effectString.trim();
                PotionEffectType type;
                int power = 0;
                if (effectString.contains(":")) {
                    String[] pieces = effectString.split(":");
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
            materialKey = materialKey.split("@")[0].trim();
            if (addBrush(materialKey)) {
                modified = true;
                sendDebug("Added brush: " + materialKey);
            }
        }

        return modified;
    }

    public boolean upgradeOverrides(Object value) {
        if (!(value instanceof String)) return false;
        boolean modified = false;
        /*
        if (other.castOverrides != null && other.castOverrides.size() > 0) {
            if (castOverrides == null) {
                castOverrides = new HashMap<>();
            }
            HashSet<String> upgradedSpells = new HashSet<>();
            for (Map.Entry<String, String> entry : other.castOverrides.entrySet()) {
                String overrideKey = entry.getKey();
                String currentValue = castOverrides.get(overrideKey);
                String value = entry.getValue();
                if (currentValue != null) {
                    try {
                        double currentDouble = Double.parseDouble(currentValue);
                        double newDouble = Double.parseDouble(value);
                        if (newDouble < currentDouble) {
                            value = currentValue;
                        }
                    } catch (Exception ex) {
                    }
                }

                boolean addOverride = currentValue == null || !value.equals(currentValue);
                modified = modified || addOverride;
                if (addOverride && mage != null && overrideKey.contains(".")) {
                    String[] pieces = StringUtils.split(overrideKey, '.');
                    String spellKey = pieces[0];
                    String spellName = spellKey;
                    if (!upgradedSpells.contains(spellKey)) {
                        SpellTemplate spell = controller.getSpellTemplate(spellKey);
                        if (spell != null) spellName = spell.getName();
                        mage.sendMessage(messages.get("wand.spell_override_upgraded").replace("$name", spellName));
                        upgradedSpells.add(spellKey);
                    }
                }
                castOverrides.put(entry.getKey(), entry.getValue());
            }
        }
        */
        return modified;
    }

    public boolean upgradeSpells(Object value) {
        if (!(value instanceof String)) return false;
        boolean modified = false;
        /*
        Set<String> spells = other.getSpells();
        for (String spellKey : spells) {
            SpellTemplate currentSpell = getBaseSpell(spellKey);
            if (addSpell(spellKey)) {
                modified = true;
                String spellName = spellKey;
                SpellTemplate spell = controller.getSpellTemplate(spellKey);
                if (spell != null) spellName = spell.getName();

                if (mage != null) {
                    if (currentSpell != null) {
                        String levelDescription = spell.getLevelDescription();
                        if (levelDescription == null || levelDescription.isEmpty()) {
                            levelDescription = spellName;
                        }
                        mage.sendMessage(messages.get("wand.spell_upgraded").replace("$name", currentSpell.getName()).replace("$level", levelDescription).replace("$wand", getName()));
                        mage.sendMessage(spell.getUpgradeDescription().replace("$name", currentSpell.getName()));

                        SpellUpgradeEvent upgradeEvent = new SpellUpgradeEvent(mage, this, currentSpell, spell);
                        Bukkit.getPluginManager().callEvent(upgradeEvent);
                    } else {
                        mage.sendMessage(messages.get("wand.spell_added").replace("$name", spellName).replace("$wand", getName()));

                        AddSpellEvent addEvent = new AddSpellEvent(mage, this, spell);
                        Bukkit.getPluginManager().callEvent(addEvent);
                    }
                }
            }
        }
        */
        return modified;
    }

    public boolean upgrade(ConfigurationSection configuration) {

        boolean modified = false;
        boolean needsInventoryUpdate = false;

        Messages messages = controller.getMessages();
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
