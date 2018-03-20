package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.wand.Wand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CasterProperties extends BaseMagicConfigurable implements com.elmakers.mine.bukkit.api.magic.CasterProperties {
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
        Collection<String> spells = getBaseSpells();
        SpellKey key = new SpellKey(spellKey);
        boolean modified = spells.add(key.getBaseKey());
        if (modified) {
            setProperty("spells", new ArrayList<>(spells));
        }
        boolean levelModified = false;
        if (key.getLevel() > 1) {
            levelModified = upgradeSpellLevel(key.getBaseKey(), key.getLevel());
        }

        return modified || levelModified;
    }

    @Override
    public boolean addBrush(String brushKey) {
        Collection<String> brushes = getBrushes();
        boolean modified = brushes.add(brushKey);
        if (modified) {
            setProperty("brushes", new ArrayList<>(brushes));
        }

        return modified;
    }

    public boolean removeSpell(String spellKey) {
        Collection<String> spells = getBaseSpells();
        SpellKey key = new SpellKey(spellKey);
        boolean modified = spells.remove(key.getBaseKey());
        if (modified) {
            setProperty("spells", new ArrayList<>(spells));
            Map<String, Object> spellLevels = getSpellLevels();
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
        Map<String, Object> spellLevels = getSpellLevels();
        Object level = spellLevels.get(spellKey);
        return level == null || !(level instanceof Integer) ? 1 : (Integer)level;
    }

    public SpellTemplate getBaseSpell(String spellKey) {
        SpellKey key = new SpellKey(spellKey);
        Collection<String> spells = getSpells();
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
    public Collection<String> getSpells() {
        Set<String> spellSet = new HashSet<>();
        Collection<String> spells = getBaseSpells();
        Map<String, Object> spellLevels = getSpellLevels();

        for (String key : spells) {
            Object levelObject = spellLevels.get(key);
            Integer level = levelObject != null && levelObject instanceof Integer ? (Integer)levelObject : null;
            if (level != null) {
                spellSet.add(new SpellKey(key, level).getKey());
            } else {
            	spellSet.add(key);
			}
        }
		return spellSet;
    }

    public Collection<String> getBrushes() {
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
        if (stack) {
            property += stackProperty;
        } else {
            property = Math.max(property, stackProperty);
        }
		return property;
	}

	@Override
	public boolean upgrade(String key, Object value) {
        if (key.equals("path")) {
            ProgressionPath path = getPath();
            if (path != null && path.hasPath(value.toString())) {
                return false;
            }
            setProperty(key, value);
            return true;
        }

        return super.upgrade(key, value);
    }

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

    public abstract boolean isPlayer();
    public abstract Player getPlayer();
    @Override
    public abstract Mage getMage();
}
