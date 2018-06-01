package com.elmakers.mine.bukkit.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class MageClass extends TemplatedProperties implements com.elmakers.mine.bukkit.api.magic.MageClass  {
    protected @Nonnull MageClassTemplate template;
    protected final MageProperties mageProperties;
    protected final Mage mage;
    private MageClass parent;
    private Collection<EntityAttributeModifier> attributeModifiers;

    private static class EntityAttributeModifier {
        public EntityAttributeModifier(Attribute attribute, AttributeModifier modifier) {
            this.attribute = attribute;
            this.modifier = modifier;
        }

        public final AttributeModifier modifier;
        public final Attribute attribute;
    }

    public MageClass(@Nonnull Mage mage, @Nonnull MageClassTemplate template) {
        super(template.hasParent() ? MagicPropertyType.SUBCLASS : MagicPropertyType.CLASS, mage.getController());
        this.template = template;
        this.mageProperties = mage.getProperties();
        this.mage = mage;
    }

    @Override
    protected void migrateProperty(String key, MagicPropertyType propertyType) {
        super.migrateProperty(key, propertyType, template);
    }

    @Override
    public boolean hasProperty(String key) {
        BaseMagicProperties storage = getStorage(key);
        if (storage != null) {
            return storage.hasOwnProperty(key);
        }
        return hasOwnProperty(key) || template.hasProperty(key);
    }

    @Override
    public boolean hasOwnProperty(String key) {
        return super.hasOwnProperty(key) || template.hasOwnProperty(key);
    }

    @Override
    @Nullable
    public Object getProperty(String key) {
        Object value = null;
        BaseMagicProperties storage = getStorage(key);
        if (storage != null && storage != this) {
            value = storage.getProperty(key);
        }
        if (value == null) {
            value = super.getProperty(key);
        }
        if (value == null) {
            value = template.getProperty(key);
        }
        return value;
    }

    @Override
    @Nullable
    public Object getInheritedProperty(String key) {
        Object value = super.getProperty(key);
        if (value == null) {
            value = template.getProperty(key);
        }
        if (value == null && parent != null) {
            value = parent.getInheritedProperty(key);
        }
        return value;
    }

    @Override
    public @Nonnull MageClassTemplate getTemplate() {
        return template;
    }

    public @Nullable MageClass getParent() {
        return parent;
    }

    public MageClass getRoot() {
        if (parent == null) return this;
        return parent.getRoot();
    }

    public void setParent(@Nonnull MageClass parent) {
        this.parent = parent;
    }

    @Override
    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties, @Nullable Set<String> overriddenProperties) {
        super.describe(sender, ignoreProperties, overriddenProperties);
        if (overriddenProperties == null) {
            overriddenProperties = new HashSet<>();
        }
        Set<String> ownKeys = getConfiguration().getKeys(false);
        overriddenProperties.addAll(ownKeys);
        sender.sendMessage("" + ChatColor.BOLD + ChatColor.GREEN + "Template Configuration for (" + ChatColor.DARK_GREEN + getKey() + ChatColor.GREEN + "):");

        Set<String> overriddenTemplateProperties = new HashSet<>(overriddenProperties);
        for (String key : template.getConfiguration().getKeys(false)) {
            MagicPropertyType propertyRoute = propertyRoutes.get(key);
            if (propertyRoute == null || propertyRoute == type) {
                overriddenProperties.add(key);
            } else {
                overriddenTemplateProperties.add(key);
            }
        }

        template.describe(sender, ignoreProperties, overriddenTemplateProperties);

        MageClass parent = getParent();
        if (parent != null) {
            sender.sendMessage(ChatColor.AQUA + "Parent Class: " + ChatColor.GREEN + parent.getTemplate().getKey());
            parent.describe(sender, ignoreProperties, overriddenProperties);
        }
    }

    public ConfigurationSection getEffectiveConfiguration(boolean includeMage) {
        ConfigurationSection effectiveConfiguration = ConfigurationUtils.cloneConfiguration(getConfiguration());
        ConfigurationSection templateConfiguration = ConfigurationUtils.cloneConfiguration(template.getConfiguration());
        for (String key : templateConfiguration.getKeys(false)) {
            MagicPropertyType propertyRoute = propertyRoutes.get(key);
            if (propertyRoute != null && propertyRoute != type) {
                templateConfiguration.set(key, null);
            }
        }

        ConfigurationUtils.overlayConfigurations(effectiveConfiguration, templateConfiguration);
        if (parent != null) {
            ConfigurationSection parentConfiguration = parent.getEffectiveConfiguration(includeMage);
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, parentConfiguration);
        } else if (includeMage) {
            // If we have a parent, it has already incorporated Mage data
            ConfigurationSection mageConfiguration = mageProperties.getConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, mageConfiguration);
        }
        return effectiveConfiguration;
    }

    @Nullable
    @Override
    protected BaseMagicConfigurable getStorage(MagicPropertyType propertyType) {
        switch (propertyType) {
            case SUBCLASS: return this;
            case CLASS: return getRoot();
            case MAGE: return mageProperties;
            default: return null;
        }
    }

    @Override
    public boolean tickMana() {
        if (!hasOwnMana() && parent != null) {
            return parent.tickMana();
        }

        return super.tickMana();
    }

    @Override
    public Mage getMage() {
        return mage;
    }

    @Override
    public boolean isPlayer() {
        return mageProperties.isPlayer();
    }

    @Nullable
    @Override
    public Player getPlayer() {
        return mageProperties.getPlayer();
    }

    @Override
    public void loadProperties() {
        if (parent != null) {
            parent.loadProperties();
        }
        super.loadProperties();
        armorUpdated();
    }

    @Override
    public String getKey() {
        return template.getKey();
    }

    @Override
    public void armorUpdated() {
        if (hasOwnMana()) {
            updateMaxMana(mage);
        }
    }

    @Override
    public boolean updateMaxMana(Mage mage) {
        if (!hasOwnMana()) {
            boolean modified = false;
            if (parent != null) {
                modified = parent.updateMaxMana(mage);
                effectiveManaMax = parent.getEffectiveManaMax();
                effectiveManaRegeneration = parent.getEffectiveManaRegeneration();
            }
            return modified;
        }

        return super.updateMaxMana(mage);
    }

    @Override
    public void updated() {
        updateMaxMana(mage);
        Wand activeWand = mage.getActiveWand();
        if (activeWand != null) {
            activeWand.updated();
        }
        mage.updatePassiveEffects();
    }

    @Nullable
    @Override
    public ProgressionPath getPath() {
        String pathKey = getString("path");
        if (pathKey == null || pathKey.length() == 0) {
            pathKey = controller.getDefaultWandPath();
        }
        return controller.getPath(pathKey);
    }

    @Override
    public String getName() {
        return template.getName();
    }

    public boolean isLocked() {
        if (super.getProperty("locked", false)) return true;
        if (parent != null) return parent.isLocked();
        return false;
    }

    public void unlock() {
        configuration.set("locked", null);
        if (parent != null) parent.unlock();
        onUnlocked();
    }

    public void lock() {
        configuration.set("locked", true);
        onLocked();
    }

    @Override
    public float getCostReduction() {
        float costReduction = getFloat("cost_reduction");
        if (mage != null) {
            float reduction = mage.getCostReduction();
            return stackPassiveProperty(reduction, costReduction);
        }
        return costReduction;
    }

    @Override
    public float getCooldownReduction() {
        float cooldownReduction = getFloat("cooldown_reduction");
        if (mage != null) {
            float reduction = mage.getCooldownReduction();
            return stackPassiveProperty(reduction, cooldownReduction);
        }
        return cooldownReduction;
    }

    @Override
    public boolean isCooldownFree() {
        return getFloat("cooldown_reduction") > 1;
    }

    @Override
    public float getConsumeReduction() {
        float consumeReduction = getFloat("consume_reduction");
        if (mage != null) {
            float reduction = mage.getConsumeReduction();
            return stackPassiveProperty(reduction, consumeReduction);
        }
        return consumeReduction;
    }

    @Override
    public float getCostScale() {
        return 1.0f;
    }

    @Override
    protected String getMessageKey(String key) {
        String mageKey = "classes." + template + "." + key;
        if (controller.getMessages().containsKey(mageKey)) {
            return mageKey;
        }
        return "mage." + key;
    }

    public void onRemoved() {
        onLocked();
    }

    public void onLocked() {
        deactivateAttributes();
        if (getBoolean("clean_on_lock", false)) {
            Player player = mage.getPlayer();
            if (player != null) {
                Inventory inventory = player.getInventory();
                String key = getKey();
                for (int i = 0; i < inventory.getSize(); i++) {
                    ItemStack item = inventory.getItem(i);
                    if (controller.isSkill(item)) {
                        String skillClass = Wand.getSpellClass(item);
                        if (skillClass != null && skillClass.equals(key)) {
                            inventory.setItem(i, null);
                        }
                    }
                }
            }
        }

        List<String> classItems = getStringList("class_items");
        if (classItems != null) {
            for (String classItemKey : classItems) {
                ItemStack item = controller.createItem(classItemKey);
                if (item == null) {
                    // We already nagged about this on load...
                    continue;
                }

                mage.removeItem(item);
            }
        }
    }

    public void onUnlocked() {
        activateAttributes();
        List<String> classItems = getStringList("class_items");
        if (classItems != null) {
            for (String classItemKey : classItems) {
                ItemStack item = controller.createItem(classItemKey);
                if (item == null) {
                    controller.getLogger().warning("Invalid class item in " + getKey() + ": " + classItemKey);
                    continue;
                }

                if (!mage.hasItem(item)) {
                    String wandKey = controller.getWandKey(item);
                    if (wandKey != null) {
                        Wand wand = mage.getBoundWand(wandKey);
                        if (wand != null) {
                            mage.giveItem(wand.getItem());
                            continue;
                        }
                    }

                    mage.giveItem(item);
                }
            }
        }
    }

    public void activateAttributes() {
        Collection<EntityAttributeModifier> modifiers = getAttributeModifiers();
        if (modifiers == null) return;
        LivingEntity entity = mage.getLivingEntity();
        if (entity == null) return;

        for (EntityAttributeModifier modifier : modifiers) {
            AttributeInstance attribute = entity.getAttribute(modifier.attribute);
            attribute.addModifier(modifier.modifier);
        }
    }

    public void deactivateAttributes() {
        if (attributeModifiers == null) return;
        LivingEntity entity = mage.getLivingEntity();
        if (entity == null) return;

        for (EntityAttributeModifier modifier : attributeModifiers) {
            AttributeInstance attribute = entity.getAttribute(modifier.attribute);
            attribute.removeModifier(modifier.modifier);
        }
    }

    @Nullable
    public Collection<EntityAttributeModifier> getAttributeModifiers() {
        if (attributeModifiers != null) {
            return attributeModifiers;
        }

        ConfigurationSection config = getConfigurationSection("entity_attributes");
        if (config == null) return null;
        Set<String> keys = config.getKeys(false);
        if (keys.isEmpty()) return null;
        attributeModifiers = new ArrayList<>();
        for (String key : keys) {
            String name = "mage_" + getKey() + "_" + key;
            double value;
            String attributeKey = key;
            AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
            if (config.isConfigurationSection(key)) {
                ConfigurationSection modifierConfig = config.getConfigurationSection(key);
                name = modifierConfig.getString("name", name);
                attributeKey = modifierConfig.getString("attribute", attributeKey);
                value = modifierConfig.getDouble("value");
                String operationType = modifierConfig.getString("operation");
                if (operationType != null && !operationType.isEmpty()) {
                    try {
                        operation = AttributeModifier.Operation.valueOf(operationType.toUpperCase());
                    } catch (Exception ex) {
                        controller.getLogger().warning("Invalid operation " + operationType + " on entity_attributes." + key + " in mage class " + getKey());
                    }
                }
            } else {
                value = config.getDouble(key);
            }

            Attribute attribute = null;
            try {
                attribute = Attribute.valueOf(attributeKey.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid attribute " + attributeKey + " on entity_attributes." + key + " in mage class " + getKey());
            }
            if (attribute != null) {
                AttributeModifier modifier = new AttributeModifier(name, value, operation);
                attributeModifiers.add(new EntityAttributeModifier(attribute, modifier));
            }
        }

        return attributeModifiers;
     }

    public void setTemplate(@Nonnull MageClassTemplate template) {
        // TODO: This won't update the "type" field of the base base base class here if the
        // template hierarchy has drastically changed.
        this.template = template;
    }

    @Override
    @Deprecated
    @Nullable
    public SpellTemplate getBaseSpell(String spellKey) {
        return getSpellTemplate(spellKey);
    }
}
