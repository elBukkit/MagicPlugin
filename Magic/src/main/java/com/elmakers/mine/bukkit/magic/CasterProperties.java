package com.elmakers.mine.bukkit.magic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.event.AddSpellEvent;
import com.elmakers.mine.bukkit.api.event.SpellUpgradeEvent;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public abstract class CasterProperties extends BaseMagicConfigurable implements com.elmakers.mine.bukkit.api.magic.CasterProperties {
    // This is used for property migration, if the stored data's version property is too low
    // then we migrate.
    protected static int CURRENT_VERSION = 7;

    protected int effectiveManaMax = 0;
    protected int effectiveManaRegeneration = 0;

    public CasterProperties(MagicPropertyType type, MageController controller) {
        super(type, controller);
    }

    public boolean hasOwnMana() {
        MagicPropertyType propertyType = propertyRoutes.get("mana");
        return (propertyType == null || propertyType == type);
    }

    @Override
    public int getManaRegeneration() {
        ManaController manaController = controller.getManaController();
        if (manaController != null && isPlayer()) {
            return manaController.getManaRegen(getPlayer());
        }
        return getInt("mana_regeneration", getInt("xp_regeneration"));
    }

    @Override
    public int getManaMax() {
        ManaController manaController = controller.getManaController();
        if (manaController != null && isPlayer()) {
            return manaController.getMaxMana(getPlayer());
        }
        return getInt("mana_max", getInt("xp_max"));
    }

    @Override
    public void setMana(float mana) {
        if (isCostFree()) {
            setProperty("mana", null);
        } else {
            ManaController manaController = controller.getManaController();
            if (manaController != null && isPlayer()) {
                manaController.setMana(getPlayer(), mana);
                return;
            }
            setProperty("mana", Math.max(0, mana));
        }
    }

    @Override
    public void setManaMax(int manaMax) {
        setProperty("mana_max", Math.max(0, manaMax));
    }

    @Override
    public void setManaRegeneration(int manaRegeneration) {
        setProperty("mana_regeneration", Math.max(0, manaRegeneration));
    }

    @Override
    public float getMana() {
        ManaController manaController = controller.getManaController();
        if (manaController != null && isPlayer()) {
            return manaController.getMana(getPlayer());
        }
        return getFloat("mana", getFloat("xp"));
    }

    @Override
    public void removeMana(float amount) {
        ManaController manaController = controller.getManaController();
        if (manaController != null && isPlayer()) {
            manaController.removeMana(getPlayer(), amount);
            return;
        }
        setMana(getMana() - amount);
    }

    public float getManaRegenerationBoost() {
        return getFloat("mana_regeneration_boost", getFloat("xp_regeneration_boost"));
    }

    public float getManaMaxBoost() {
        return getFloat("mana_max_boost", getFloat("xp_max_boost"));
    }

    public boolean isCostFree() {
        return getFloat("cost_reduction") > 1;
    }

    public boolean isCooldownFree() {
        return getFloat("cooldown_reduction") > 1;
    }

    public int getEffectiveManaMax() {
        ManaController manaController = controller.getManaController();
        if (manaController != null && isPlayer()) {
            return manaController.getMaxMana(getPlayer());
        }
        return effectiveManaMax;
    }

    public int getEffectiveManaRegeneration() {
        ManaController manaController = controller.getManaController();
        if (manaController != null && isPlayer()) {
            return manaController.getManaRegen(getPlayer());
        }
        return effectiveManaRegeneration;
    }

    protected long getLastManaRegeneration() {
        return getLong("mana_timestamp");
    }

    public void armorUpdated() {
    }

    public boolean updateMaxMana(Mage mage) {
        if (!usesMana()) {
            return false;
        }
        int currentMana = effectiveManaMax;
        int currentManaRegen = effectiveManaRegeneration;

        float effectiveBoost = getManaMaxBoost();
        float effectiveRegenBoost = getManaRegenerationBoost();
        if (mage != null)
        {
            Collection<Wand> activeArmor = mage.getActiveArmor();
            for (Wand armorWand : activeArmor) {
                effectiveBoost += armorWand.getManaMaxBoost();
                effectiveRegenBoost += armorWand.getManaRegenerationBoost();
            }
            Wand activeWand = mage.getActiveWand();
            if (activeWand != null && !activeWand.isPassive()) {
                effectiveBoost += activeWand.getManaMaxBoost();
                effectiveRegenBoost += activeWand.getManaRegenerationBoost();
            }
            Wand offhandWand = mage.getOffhandWand();
            if (offhandWand != null && !offhandWand.isPassive()) {
                effectiveBoost += offhandWand.getManaMaxBoost();
                effectiveRegenBoost += offhandWand.getManaRegenerationBoost();
            }
        }
        boolean boostable = getBoolean("boostable", true);
        effectiveManaMax = getManaMax();
        if (boostable && effectiveBoost != 0) {
            effectiveManaMax = (int)Math.ceil(effectiveManaMax + effectiveBoost * effectiveManaMax);
        }
        effectiveManaRegeneration = getManaRegeneration();
        if (boostable && effectiveRegenBoost != 0) {
            effectiveManaRegeneration = (int)Math.ceil(effectiveManaRegeneration + effectiveRegenBoost * effectiveManaRegeneration);
        }

        return (currentMana != effectiveManaMax || effectiveManaRegeneration != currentManaRegen);
    }

    public boolean usesMana() {
        if (isCostFree()) return false;
        return getManaMax() > 0;
    }

    public boolean tickMana() {
        boolean updated = false;
        if (usesMana() && hasOwnMana() && !getMage().isManaRegenerationDisabled()) {
            long now = System.currentTimeMillis();
            long lastManaRegeneration = getLastManaRegeneration();
            int effectiveManaRegeneration = getEffectiveManaRegeneration();
            if (lastManaRegeneration > 0 && effectiveManaRegeneration > 0)
            {
                long delta = now - lastManaRegeneration;
                int effectiveManaMax = getEffectiveManaMax();
                int manaMax = getManaMax();
                float mana = getMana();
                if (effectiveManaMax == 0 && manaMax > 0) {
                    effectiveManaMax = manaMax;
                }
                setMana(Math.min(effectiveManaMax, mana + (float) effectiveManaRegeneration * (float)delta / 1000));
                updated = true;
            }
            lastManaRegeneration = now;
            setProperty("mana_timestamp", lastManaRegeneration);
        }

        return updated;
    }

    public void tick() {
        tickMana();
    }

    @Override
    public boolean addSpell(String spellKey) {
        BaseMagicConfigurable storage = getStorage("spells");
        if (storage != this && storage != null) {
            return storage.addSpell(spellKey);
        }

        SpellTemplate template = controller.getSpellTemplate(spellKey);
        if (template == null) {
            controller.getLogger().warning("Tried to add unknown spell: " + spellKey);
            return false;
        }

        // Convert to spell if aliased
        spellKey = template.getKey();

        Collection<String> spells = getBaseSpells();
        SpellKey key = new SpellKey(spellKey);
        SpellTemplate currentSpell = getSpellTemplate(spellKey);
        boolean modified = spells.add(key.getBaseKey());
        if (modified) {
            setProperty("spells", new ArrayList<>(spells));
        }
        boolean levelModified = false;
        if (key.getLevel() > 1) {
            levelModified = upgradeSpellLevel(key.getBaseKey(), key.getLevel());
        }

        if (!modified && !levelModified) {
            return false;
        }

        // Special handling for spells to remove
        Collection<SpellKey> spellsToRemove = template.getSpellsToRemove();
        for (SpellKey removeKey : spellsToRemove) {
            removeSpell(removeKey.getBaseKey());
        }

        Mage mage = getMage();
        if (mage != null)
        {
            if (currentSpell != null) {
                String levelDescription = template.getLevelDescription();
                if (levelDescription == null || levelDescription.isEmpty()) {
                    levelDescription = template.getName();
                }
                sendLevelMessage("spell_upgraded", currentSpell.getName(), levelDescription);

                String upgradeDescription = template.getUpgradeDescription().replace("$name", currentSpell.getName());
                if (!upgradeDescription.isEmpty()) {
                    mage.sendMessage(controller.getMessages().get("spell.upgrade_description_prefix") + upgradeDescription);
                }

                SpellUpgradeEvent upgradeEvent = new SpellUpgradeEvent(mage, getWand(), currentSpell, template);
                Bukkit.getPluginManager().callEvent(upgradeEvent);
            } else {
                // This is a little hacky, but it is here to fix duplicate spell messages from the spellshop.
                if (mage.getActiveGUI() == null)
                    sendAddMessage("spell_added", template.getName());

                AddSpellEvent addEvent = new AddSpellEvent(mage, getWand(), template);
                Bukkit.getPluginManager().callEvent(addEvent);
            }
        }

        return true;
    }

    @Override
    public boolean addBrush(String brushKey) {
        BaseMagicConfigurable storage = getStorage("brushes");
        if (storage != this && storage != null) {
            return storage.addBrush(brushKey);
        }

        Collection<String> brushes = getBrushes();
        boolean modified = brushes.add(brushKey);
        if (modified) {
            setProperty("brushes", new ArrayList<>(brushes));
        }

        Mage mage = getMage();
        if (modified && mage != null)
        {
            Messages messages = controller.getMessages();
            String materialName = MaterialBrush.getMaterialName(messages, brushKey);
            if (materialName == null)
            {
                mage.getController().getLogger().warning("Invalid material: " + brushKey);
                materialName = brushKey;
            }

            sendAddMessage("brush_added", materialName);
        }

        return modified;
    }

    public boolean removeSpell(String spellKey) {
        Collection<String> spells = getBaseSpells();
        SpellKey key = new SpellKey(spellKey);
        boolean modified = spells.remove(key.getBaseKey());
        if (modified) {
            setProperty("spells", new ArrayList<>(spells));
            Map<String, Integer> spellLevels = getSpellLevels();
            if (spellLevels.remove(key.getBaseKey()) != null) {
                setProperty("spell_levels", spellLevels);
            }
        }

        return modified;
    }

    public boolean removeBrush(String brushKey) {
        Collection<String> brushes = getBrushes();
        boolean modified = brushes.remove(brushKey);
        if (modified) {
            setProperty("brushes", new ArrayList<>(brushes));
        }

        return modified;
    }

    public int getSpellLevel(String spellKey) {
        Map<String, Integer> spellLevels = getSpellLevels();
        Integer level = spellLevels.get(spellKey);
        return level == null ? 1 : level;
    }

    @Nullable
    @Override
    public SpellTemplate getSpellTemplate(String spellKey) {
        SpellKey key = new SpellKey(spellKey);
        Collection<String> spells = getBaseSpells();
        if (!spells.contains(key.getBaseKey())) return null;
        SpellKey baseKey = new SpellKey(key.getBaseKey(), getSpellLevel(key.getBaseKey()));
        return controller.getSpellTemplate(baseKey.getKey());
    }

    public void updateMana() {

    }

    @Override
    public boolean hasSpell(String key) {
        SpellKey spellKey = new SpellKey(key);

        if (!getBaseSpells().contains(spellKey.getBaseKey())) return false;
        int level = getSpellLevel(spellKey.getBaseKey());
        return (level >= spellKey.getLevel());
    }

    public Collection<String> getBaseSpells() {
        Object existingSpells = getObject("spells");
        Set<String> spells = new HashSet<>();
        if (existingSpells != null) {
            if (!(existingSpells instanceof List)) {
                controller.getLogger().warning("Spell list in " + type + " is " + existingSpells.getClass().getName() + ", expected List");
            } else {
                @SuppressWarnings("unchecked")
                List<String> existingList = (List<String>)existingSpells;
                spells.addAll(existingList);
            }
        }
        return spells;
    }

    @Override
    public Set<String> getSpells() {
        Set<String> spellSet = new HashSet<>();
        Collection<String> spells = getBaseSpells();
        Map<String, Integer> spellLevels = getSpellLevels();

        for (String key : spells) {
            Integer level = spellLevels.get(key);
            if (level != null) {
                spellSet.add(new SpellKey(key, level).getKey());
            } else {
                spellSet.add(key);
            }
        }
        return spellSet;
    }

    @Nullable
    @Override
    public Spell getSpell(String spellKey) {
        Mage mage = getMage();
        if (mage == null) {
            return null;
        }
        SpellKey key = new SpellKey(spellKey);
        spellKey = key.getBaseKey();
        Set<String> spells = getSpells();
        if (!spells.contains(spellKey)) return null;
        Map<String, Integer> spellLevels = getSpellLevels();
        Integer level = spellLevels.get(spellKey);
        if (level != null) {
            spellKey = new SpellKey(spellKey, level).getKey();
        }
        return mage.getSpell(spellKey);
    }

    public Set<String> getBrushes() {
        Object existingBrushes = getObject("brushes");
        Set<String> brushes = new HashSet<>();
        if (existingBrushes != null) {
            if (!(existingBrushes instanceof List)) {
                controller.getLogger().warning("Brush list in " + type + " is " + existingBrushes.getClass().getName() + ", expected List");
            } else {
                @SuppressWarnings("unchecked")
                List<String> existingList = (List<String>)existingBrushes;
                brushes.addAll(existingList);
            }
        }
        return brushes;
    }

    @Nullable
    @Override
    public ProgressionPath getPath() {
        String pathKey = getString("path");
        if (pathKey != null && !pathKey.isEmpty()) {
            return controller.getPath(pathKey);
        }
        return null;
    }

    @Override
    public boolean canProgress() {
        ProgressionPath path = getPath();
        return (path != null && path.canProgress(this));
    }

    protected float stackPassiveProperty(float property, float stackProperty) {
        boolean stack = getBoolean("stack");

        // If stacking, then this value has already been added to the base value.
        // If this value is 0, then we don't need to look at it.
        if (!stack && stackProperty != 0) {
            property = Math.max(property, stackProperty);
        }
        return property;
    }

    @Override
    protected boolean upgradeInternal(String key, Object value) {
        if (key.equals("path")) {
            ProgressionPath path = getPath();
            if (path != null && path.hasPath(value.toString())) {
                return false;
            }
            setProperty(key, value);
            return true;
        }

        return super.upgradeInternal(key, value);
    }

    @Nullable
    @Override
    public Double getAttribute(String attributeKey) {
        ConfigurationSection attributes = getConfigurationSection("attributes");
        return attributes == null ? null : attributes.getDouble(attributeKey);
    }

    @Override
    public void setAttribute(String attributeKey, Double attributeValue) {
        ConfigurationSection attributes = getConfigurationSection("attributes");
        if (attributes == null) {
            if (attributeValue == null) return;
            attributes = new MemoryConfiguration();
        }
        attributes.set(attributeKey, attributeValue);
        setProperty("attributes", attributes);
        updated();
    }

    @Override
    public boolean addItem(ItemStack item) {
        if (Wand.isSpell(item) && !Wand.isSkill(item)) {
            String spell = Wand.getSpell(item);
            SpellKey spellKey = new SpellKey(spell);
            Map<String, Integer> spellLevels = getSpellLevels();
            Integer currentLevel = spellLevels.get(spellKey.getBaseKey());
            if ((currentLevel == null || currentLevel < spellKey.getLevel()) && addSpell(spell)) {
                return true;
            }
        } else if (Wand.isBrush(item)) {
            String materialKey = Wand.getBrush(item);
            Set<String> materials = getBrushes();
            if (!materials.contains(materialKey) && addBrush(materialKey)) {
                return true;
            }
        }
        Mage mage = getMage();
        if (mage != null && !mage.isAtMaxSkillPoints() && controller.skillPointItemsEnabled()) {
            Integer sp = Wand.getSP(item);
            if (sp != null) {
                int amount = (int)Math.floor(mage.getSPMultiplier() * sp * item.getAmount());
                mage.addSkillPoints(amount);
                return true;
            }
        }

        return false;
    }

    protected void sendLevelMessage(String messageKey, String nameParam, String level) {
        Mage mage = getMage();
        if (mage == null || nameParam == null || nameParam.isEmpty()) return;

        String message = getMessage(messageKey).replace("$name", nameParam).replace("$level", level);
        mage.sendMessage(message);
    }

    @Override
    protected void sendAddMessage(String messageKey, String nameParam) {
        Mage mage = getMage();
        if (mage == null || nameParam == null || nameParam.isEmpty()) return;
        String message = getMessage(messageKey).replace("$name", nameParam);
        mage.sendMessage(message);
    }

    @Override
    protected void sendMessage(String messageKey) {
        Mage mage = getMage();
        if (mage == null || messageKey == null || messageKey.isEmpty()) return;
        mage.sendMessage(getMessage(messageKey));
    }

    @Override
    protected void sendDebug(String debugMessage) {
        Mage mage = getMage();
        if (mage != null) {
            mage.sendDebugMessage(debugMessage);
        }
    }

    @Nullable
    protected Wand getWand() {
        return null;
    }

    public abstract boolean isPlayer();
    @Nullable
    public abstract Player getPlayer();
    @Nullable
    @Override
    public abstract Mage getMage();

    @SuppressWarnings("unchecked")
    protected void migrateBrushes(ConfigurationSection configuration) {
        Object brushInventoryRaw = configuration.get("brush_inventory");
        if (brushInventoryRaw != null) {
            Map<String, ? extends Object> brushInventory = null;
            Map<String, Integer> newBrushInventory = new HashMap<>();
            if (brushInventoryRaw instanceof Map) {
                brushInventory = (Map<String, ? extends Object>)brushInventoryRaw;
            } else if (brushInventoryRaw instanceof ConfigurationSection) {
                brushInventory = NMSUtils.getMap((ConfigurationSection)brushInventoryRaw);
            }
            if (brushInventory != null) {
                for (Map.Entry<String, ? extends Object> brushEntry : brushInventory.entrySet()) {
                    Object slot = brushEntry.getValue();
                    if (slot != null && slot instanceof Integer) {
                        String materialKey = brushEntry.getKey();
                        materialKey = DeprecatedUtils.migrateMaterial(materialKey);
                        newBrushInventory.put(materialKey, (Integer)slot);
                    }
                }
                configuration.set("brush_inventory", newBrushInventory);
            }
        }

        Object brushesRaw = getObject("brushes", getObject("materials"));
        if (brushesRaw != null) {
            Collection<String> brushes = null;
            if (brushesRaw instanceof String) {
                String[] brushNames = StringUtils.split((String)brushesRaw, ',');
                brushes = Arrays.asList(brushNames);
            } else if (brushesRaw instanceof Collection) {
                brushes = (Collection<String>)brushesRaw;
            }

            if (brushes != null) {

                configuration.set("brushes", new ArrayList<>(brushes));
            }
        }
    }

    protected void migrate(int version, ConfigurationSection configuratoin) {
        // Migration: Update brushes to 1.13
        if (version <= 6) {
            migrateBrushes(configuratoin);
        }

        configuratoin.set("version", CURRENT_VERSION);
    }

    @Override
    public void load(@Nullable ConfigurationSection configuration) {
        int version = configuration.getInt("version", 0);
        if (version < CURRENT_VERSION) {
            // Migration will be handled by CasterProperties, this is just here
            // So that we save the data after to avoid re-migrating.
            migrate(version, configuration);
        }
        super.load(configuration);
    }
}
