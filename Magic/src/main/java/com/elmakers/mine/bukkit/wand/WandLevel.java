package com.elmakers.mine.bukkit.wand;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class WandLevel {
    private final WandUpgradePath path;

    private Deque<WeightedPair<Integer>> spellCountProbability = new ArrayDeque<>();
    private Deque<WeightedPair<Integer>> materialCountProbability = new ArrayDeque<>();
    private Deque<WeightedPair<String>> spellProbability = new ArrayDeque<>();
    private Deque<WeightedPair<String>> materialProbability = new ArrayDeque<>();
    private Deque<WeightedPair<Integer>> useProbability = new ArrayDeque<>();

    private Deque<WeightedPair<Integer>> propertyCountProbability = new ArrayDeque<>();
    private Map<String, Deque<WeightedPair<Float>>> propertiesProbability = new HashMap<>();

    private Deque<WeightedPair<Integer>> manaRegenerationProbability = new ArrayDeque<>();
    private Deque<WeightedPair<Integer>> manaMaxProbability = new ArrayDeque<>();

    protected WandLevel(WandUpgradePath path, MageController controller, ConfigurationSection template, int levelIndex, int nextLevelIndex, float distance) {
        this.path = path;

        // Fetch spell probabilities, and filter out invalid/unknown spells
        Deque<WeightedPair<String>> spells = new ArrayDeque<>();
        RandomUtils.populateStringProbabilityMap(spells, template, "spells", levelIndex, nextLevelIndex, distance);

        for (WeightedPair<String> spellValue : spells) {
            if (controller.getSpellTemplate(spellValue.getValue()) != null) {
                spellProbability.add(spellValue);
            }
        }

        // Fetch spell count probabilities
        RandomUtils.populateIntegerProbabilityMap(spellCountProbability, template, "spell_count", levelIndex, nextLevelIndex, distance);

        // Fetch material probabilities, filter out invalid materials (important for backwards compatibility)
        Deque<WeightedPair<String>> brushes = new ArrayDeque<>();
        RandomUtils.populateStringProbabilityMap(brushes, template, "materials", levelIndex, nextLevelIndex, distance);
        for (WeightedPair<String> brushValue : brushes) {
            MaterialBrush brush = new MaterialBrush(brushValue.getValue());
            if (brush.isValid(false)) {
                materialProbability.add(brushValue);
            }
        }
        // Fetch material count probabilities
        RandomUtils.populateIntegerProbabilityMap(materialCountProbability, template.getConfigurationSection("material_count"), levelIndex, nextLevelIndex, distance);

        // Fetch uses
        RandomUtils.populateIntegerProbabilityMap(useProbability, template, "uses", levelIndex, nextLevelIndex, distance);

        // Fetch property count probability
        RandomUtils.populateIntegerProbabilityMap(propertyCountProbability, template, "property_count", levelIndex, nextLevelIndex, distance);

        // Fetch properties
        ConfigurationSection propertiesConfig = template.getConfigurationSection("properties");
        if (propertiesConfig != null) {
            for (String propertyKey : propertiesConfig.getKeys(false)) {
                Deque<WeightedPair<Float>> propertyProbability = new ArrayDeque<>();
                RandomUtils.populateFloatProbabilityMap(propertyProbability, propertiesConfig, propertyKey, levelIndex, nextLevelIndex, distance);
                propertyKey = propertyKey.replace("|", ".");
                propertiesProbability.put(propertyKey, propertyProbability);
            }
        }

        // Fetch regeneration
        RandomUtils.populateIntegerProbabilityMap(manaRegenerationProbability, template, "mana_regeneration", levelIndex, nextLevelIndex, distance);
        RandomUtils.populateIntegerProbabilityMap(manaMaxProbability, template, "mana_max", levelIndex, nextLevelIndex, distance);
    }

    public void add(WandLevel other) {
        spellProbability = RandomUtils.merge(spellProbability, other.spellProbability);
        materialProbability = RandomUtils.merge(materialProbability, other.materialProbability);

        this.materialCountProbability = materialCountProbability.isEmpty() ? other.materialCountProbability : materialCountProbability;
        this.spellCountProbability = spellCountProbability.isEmpty() ? other.spellCountProbability : spellCountProbability;
        this.useProbability = useProbability.isEmpty() ? other.useProbability : useProbability;
        this.propertyCountProbability = propertyCountProbability.isEmpty() ? other.propertyCountProbability : propertyCountProbability;
        for (Map.Entry<String, Deque<WeightedPair<Float>>> entry : other.propertiesProbability.entrySet()) {
            Deque<WeightedPair<Float>> thisOne = propertiesProbability.get(entry.getKey());
            if (thisOne == null || thisOne.isEmpty()) {
                propertiesProbability.put(entry.getKey(), entry.getValue());
            }
        }
        this.manaRegenerationProbability = manaRegenerationProbability.isEmpty() ? other.manaRegenerationProbability : manaRegenerationProbability;
        this.manaMaxProbability = manaMaxProbability.isEmpty() ? other.manaMaxProbability : manaMaxProbability;
    }

    public int getSpellCount() {
        int count = 0;
        for (WeightedPair<Integer> spellCount : spellCountProbability) {
            if (spellCount.getValue() > count) {
                count = spellCount.getValue();
            }
        }

        if (count == 0 && !spellProbability.isEmpty()) {
            count = 1;
        }
        return count;
    }

    public int getMaterialCount() {
        int count = 0;
        for (WeightedPair<Integer> materialCount : materialCountProbability) {
            if (materialCount.getValue() > count) {
                count = materialCount.getValue();
            }
        }

        return count;
    }

    public Deque<WeightedPair<String>> getRemainingSpells(CasterProperties properties) {
        Deque<WeightedPair<String>> remainingSpells = new ArrayDeque<>();
        for (WeightedPair<String> spell : spellProbability) {
            if (spell.getRawThreshold() >= 1 && !properties.hasSpell(spell.getValue())) {
                remainingSpells.add(spell);
            }
        }

        return remainingSpells;
    }

    public Deque<WeightedPair<String>> getRemainingMaterials(Wand wand) {
        Deque<WeightedPair<String>> remainingMaterials = new ArrayDeque<>();
        for (WeightedPair<String> material : materialProbability) {
            String materialKey = material.getValue();
            // Fixup |'s to :'s .... kinda hacky, but I didn't think this through unfortunately. :\
            // TODO: escape the keys as strings with '', which is probably the right way to do it.
            materialKey = materialKey.replace("|", ":");
            if (!wand.hasBrush(materialKey) && MaterialBrush.isValidMaterial(materialKey, false)) {
                remainingMaterials.add(material);
            }
        }

        return remainingMaterials;
    }

    public boolean randomizeWand(Mage mage, Wand wand, boolean hasUpgrade, boolean addSpells) {
        // Add random spells to the wand
        Mage activeMage = wand.getActiveMage();
        if (mage == null) {
            mage = activeMage;
        }
        wand.setActiveMage(mage);
        boolean addedSpells = false;
        Deque<WeightedPair<String>> remainingSpells = getRemainingSpells(wand);

        if (addSpells) {
            if (remainingSpells.size() > 0) {
                Integer spellCount = RandomUtils.weightedRandom(spellCountProbability);
                if (spellCount == null || spellCount == 0) {
                    spellCount = 1;
                }
                for (int i = 0; i < spellCount; i++) {
                    String spellKey = RandomUtils.weightedRandom(remainingSpells);
                    boolean added = wand.addSpell(spellKey);
                    if (mage != null) {
                        mage.sendDebugMessage("Trying to add spell: " + spellKey + " ? " + added);
                    }
                    if (added) {
                        addedSpells = true;
                    }
                }
            }
        }

        // Look through all spells for the max mana casting cost
        // Also look for any material-using spells
        boolean needsMaterials = false;
        int maxManaCost = 0;
        Set<String> spells = wand.getSpells();
        for (String spellName : spells) {
            SpellTemplate spell = wand.getController().getSpellTemplate(spellName);
            if (spell != null) {
                needsMaterials = needsMaterials || spell.usesBrush();
                Collection<CastingCost> costs = spell.getCosts();
                if (costs != null) {
                    for (CastingCost cost : costs) {
                        maxManaCost = Math.max(maxManaCost, cost.getMana());
                    }
                }
            }
        }

        // Add random materials
        boolean addedMaterials = false;
        Deque<WeightedPair<String>> remainingMaterials = getRemainingMaterials(wand);
        if (needsMaterials && remainingMaterials.size() > 0) {
            int currentMaterialCount = wand.getBrushes().size();
            Integer materialCount = RandomUtils.weightedRandom(materialCountProbability);

            // Make sure the wand has at least one material.
            if (materialCount == null) {
                materialCount = 0;
            }
            if (currentMaterialCount == 0) {
                materialCount = Math.max(1, materialCount);
            }
            int retries = 100;
            for (int i = 0; i < materialCount; i++) {
                String materialKey = RandomUtils.weightedRandom(remainingMaterials);
                materialKey = materialKey.replace("|", ":");
                if (!wand.addBrush(materialKey)) {
                    // Try again up to a certain number if we picked one the wand already had.
                    if (retries-- > 0) i--;
                } else {
                    addedMaterials = true;
                }
            }
        }

        // Let them upgrade if they aren't getting any new spells or brushes
        if (hasUpgrade && addSpells && !(addedMaterials && needsMaterials) && !addedSpells && ((getSpellCount() > 0 && spellProbability.size() > 0) || (getMaterialCount() > 0 && materialProbability.size() > 0)))
        {
            if (mage != null && mage.getDebugLevel() > 0) {
                mage.sendDebugMessage("Has upgrade: " + hasUpgrade);
                mage.sendDebugMessage("Added spells: " + addedSpells + ", should: " + addSpells);
                mage.sendDebugMessage("Spells per enchant: " + getSpellCount());
                mage.sendDebugMessage("Spells in list: " + spellProbability.size());
                mage.sendDebugMessage("Added brushes: " +  addedMaterials + ", needed: " + needsMaterials);
            }
            wand.setActiveMage(activeMage);
            return false;
        }

        // Add random wand properties
        boolean addedProperties = false;
        Integer propertyCount = propertyCountProbability.size() == 0 ? Integer.valueOf(0) : RandomUtils.weightedRandom(propertyCountProbability);
        ConfigurationSection wandProperties = new MemoryConfiguration();

        List<String> propertyKeys = new ArrayList<>(propertiesProbability.keySet());
        List<String> propertiesAvailable = new ArrayList<>();

        for (String propertyKey : propertyKeys) {
            double currentValue = wand.getDouble(propertyKey);
            double maxValue = path.getMaxProperty(propertyKey);
            if (currentValue < maxValue) {
                propertiesAvailable.add(propertyKey);
            }
        }

        // Make sure we give them *something* if something is available
        if (propertiesAvailable.size() > 0 && !addedMaterials && !addedSpells && propertyCount == 0) {
            propertyCount = 1;
        }

        while (propertyCount != null && propertyCount-- > 0 && propertiesAvailable.size() > 0)
        {
            int randomPropertyIndex = (int)(Math.random() * propertiesAvailable.size());
            String randomProperty = propertiesAvailable.get(randomPropertyIndex);
            Deque<WeightedPair<Float>> probabilities = propertiesProbability.get(randomProperty);
            double current = wand.getDouble(randomProperty);
            double maxValue = path.getMaxProperty(randomProperty);
            if (probabilities.size() > 0 && current < maxValue) {
                addedProperties = true;
                current = Math.min(maxValue, current + RandomUtils.weightedRandom(probabilities));
                wandProperties.set(randomProperty, current);
            }
        }

        // The mana system is considered separate from other properties

        if (wand.isCostFree()) {
            // Cost-Free wands don't need mana.
            wandProperties.set("mana_regeneration", 0);
            wandProperties.set("mana_max", 0);
            wandProperties.set("mana", 0);
        } else {
            int manaRegeneration = wand.getManaRegeneration();
            if (manaRegenerationProbability.size() > 0 && manaRegeneration < path.getMaxManaRegeneration()) {
                addedProperties = true;
                manaRegeneration = Math.min(path.getMaxManaRegeneration(), manaRegeneration + RandomUtils.weightedRandom(manaRegenerationProbability));
                wandProperties.set("mana_regeneration", manaRegeneration);
            }
            int manaMax = wand.getManaMax();
            if (manaMaxProbability.size() > 0 && manaMax < path.getMaxMaxMana()) {
                manaMax = Math.min(path.getMaxMaxMana(), manaMax + RandomUtils.weightedRandom(manaMaxProbability));
                if (path.getMatchSpellMana()) {
                    // Make sure the wand has at least enough mana to cast the highest costing spell it has.
                    manaMax = Math.max(maxManaCost, manaMax);
                }
                wandProperties.set("mana_max", manaMax);
                addedProperties = true;
            }

            // Refill the wand's mana, why not
            wandProperties.set("mana", manaMax);
        }

        // Add or set uses to the wand
        if (!useProbability.isEmpty()) {
            int wandUses = wand.getRemainingUses();
            if (wandUses < path.getMaxUses() && useProbability.size() > 0) {
                wandProperties.set("uses", Math.min(path.getMaxUses(), wandUses + RandomUtils.weightedRandom(useProbability)));
                addedProperties = true;
            }
        }

        // Set properties.
        wand.upgrade(wandProperties);
        wand.setActiveMage(activeMage);
        return addedMaterials || addedSpells || addedProperties;
    }

    public int getSpellProbabilityCount() {
        return spellProbability.size();
    }

    public int getMaterialProbabilityCount() {
        return materialProbability.size();
    }
}
