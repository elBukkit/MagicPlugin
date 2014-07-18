package com.elmakers.mine.bukkit.wand;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class WandLevel {
    private final WandUpgradePath path;

	private final LinkedList<WeightedPair<Integer>> spellCountProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> materialCountProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<String>> spellProbability = new LinkedList<WeightedPair<String>>();
	private final LinkedList<WeightedPair<String>> materialProbability = new LinkedList<WeightedPair<String>>();
	private final LinkedList<WeightedPair<Integer>> useProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> addUseProbability = new LinkedList<WeightedPair<Integer>>();

	private final LinkedList<WeightedPair<Integer>> propertyCountProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Float>> costReductionProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> powerProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionPhysicalProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionProjectilesProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionFallingProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionFireProbability = new LinkedList<WeightedPair<Float>>();
	private final LinkedList<WeightedPair<Float>> damageReductionExplosionsProbability = new LinkedList<WeightedPair<Float>>();
	
	private final LinkedList<WeightedPair<Integer>> xpRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> xpMaxProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> healthRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> hungerRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	
	private final LinkedList<WeightedPair<Float>> hasteProbability = new LinkedList<WeightedPair<Float>>();

	protected WandLevel(WandUpgradePath path, ConfigurationSection template, int levelIndex, int nextLevelIndex, float distance) {
        this.path = path;

		// Fetch spell probabilities
		com.elmakers.mine.bukkit.utility.RandomUtils.populateStringProbabilityMap(spellProbability, template.getConfigurationSection("spells"), levelIndex, nextLevelIndex, distance);
		
		// Fetch spell count probabilities
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(spellCountProbability, template.getConfigurationSection("spell_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch material probabilities
		com.elmakers.mine.bukkit.utility.RandomUtils.populateStringProbabilityMap(materialProbability, template.getConfigurationSection("materials"), levelIndex, nextLevelIndex, distance);
		
		// Fetch material count probabilities
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(materialCountProbability, template.getConfigurationSection("material_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch uses
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(useProbability, template.getConfigurationSection("uses"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(addUseProbability, template.getConfigurationSection("add_uses"), levelIndex, nextLevelIndex, distance);
		
		// Fetch property count probability
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(propertyCountProbability, template.getConfigurationSection("property_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch cost and damage reduction
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(costReductionProbability, template.getConfigurationSection("cost_reduction"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionProbability, template.getConfigurationSection("protection"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionPhysicalProbability, template.getConfigurationSection("protection_physical"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionFallingProbability, template.getConfigurationSection("protection_falling"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionProjectilesProbability, template.getConfigurationSection("protection_projectiles"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionFireProbability, template.getConfigurationSection("protection_fire"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(damageReductionExplosionsProbability, template.getConfigurationSection("protection_explosions"), levelIndex, nextLevelIndex, distance);

		// Fetch regeneration
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(xpRegenerationProbability, template.getConfigurationSection("xp_regeneration"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(xpMaxProbability, template.getConfigurationSection("xp_max"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(healthRegenerationProbability, template.getConfigurationSection("health_regeneration"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(hungerRegenerationProbability, template.getConfigurationSection("hunger_regeneration"), levelIndex, nextLevelIndex, distance);
		
		// Fetch haste
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(hasteProbability, template.getConfigurationSection("haste"), levelIndex, nextLevelIndex, distance);
		
		// Fetch power
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(powerProbability, template.getConfigurationSection("power"), levelIndex, nextLevelIndex, distance);		
	}
	
	public boolean randomizeWand(Wand wand, boolean additive) {
		// Add random spells to the wand
		boolean addedSpells = false;
		Set<String> wandSpells = wand.getSpells();
		LinkedList<WeightedPair<String>> remainingSpells = new LinkedList<WeightedPair<String>>();
        for (WeightedPair<String> spell : spellProbability) {
            if (!wandSpells.contains(spell.getValue())) {
                remainingSpells.add(spell);
            }
        }

		SpellTemplate firstSpell = null;		
		if (remainingSpells.size() > 0) {
			Integer spellCount = RandomUtils.weightedRandom(spellCountProbability);
			int retries = 10;
			for (int i = 0; i < spellCount; i++) {
				String spellKey = RandomUtils.weightedRandom(remainingSpells);
				
				if (wand.addSpell(spellKey)) {	
					if (firstSpell == null) {
						firstSpell = wand.getMaster().getSpellTemplate(spellKey);
					}
					addedSpells = true;
				} else {
					// Try again up to a certain number if we picked one the wand already had.
					if (retries-- > 0) i--;
				}
			}
		}
		
		// Look through all spells for the max XP casting cost
		// Also look for any material-using spells
		boolean needsMaterials = false;
		int maxXpCost = 0;
		Set<String> spells = wand.getSpells();
		for (String spellName : spells) {
			SpellTemplate spell = wand.getMaster().getSpellTemplate(spellName);
			if (spell != null) {
				needsMaterials = needsMaterials || (spell instanceof BrushSpell) && !((BrushSpell)spell).hasBrushOverride();
				Collection<CastingCost> costs = spell.getCosts();
				if (costs != null) {
					for (CastingCost cost : costs) {
						maxXpCost = Math.max(maxXpCost, cost.getXP());
					}
				}
			}
		}
		
		// Add random materials
		boolean addedMaterials = false;
		Set<String> wandMaterials = wand.getBrushes();
		LinkedList<WeightedPair<String>> remainingMaterials = new LinkedList<WeightedPair<String>>();
        for (WeightedPair<String> material : materialProbability) {
            String materialKey = material.getValue();
            // Fixup |'s to :'s .... kinda hacky, but I didn't think this through unfortunately. :\
            // TODO: escape the keys as strings with '', which is probably the right way to do it.
            materialKey = materialKey.replace("|", ":");
            if (!wandMaterials.contains(material.getValue()) && MaterialBrush.isValidMaterial(materialKey, false)) {
                remainingMaterials.add(material);
            }
        }

		if (needsMaterials && remainingMaterials.size() > 0) {
			int currentMaterialCount = wand.getBrushes().size();
			Integer materialCount = RandomUtils.weightedRandom(materialCountProbability);
			
			// Make sure the wand has at least one material.
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
		
		// Add random wand properties
		boolean addedProperties = false;
		Integer propertyCount = propertyCountProbability.size() == 0 ? 0 : RandomUtils.weightedRandom(propertyCountProbability);
		ConfigurationSection wandProperties = new MemoryConfiguration();
		double costReduction = wand.getCostReduction();
		
		while (propertyCount-- > 0) {
			int randomProperty = (int)(Math.random() * 11);
			switch (randomProperty) {
			case 0: 
				if (costReductionProbability.size() > 0 && costReduction < path.getMaxCostReduction()) {
					addedProperties = true;
					costReduction = Math.min(path.getMaxCostReduction(), costReduction + RandomUtils.weightedRandom(costReductionProbability));
					wandProperties.set("cost_reduction", costReduction);
				}
				break;
			case 1:
				float power = wand.getPower();
				if (powerProbability.size() > 0 && power < path.getMaxPower()) {
					addedProperties = true;
					wandProperties.set("power", (Double)(double)(Math.min(path.getMaxPower(), power + RandomUtils.weightedRandom(powerProbability))));
				}
				break;
			case 2:
				float damageReduction = wand.getDamageReduction();
				if (damageReductionProbability.size() > 0 && damageReduction < path.getMaxDamageReduction()) {
					addedProperties = true;
					wandProperties.set("protection", (Double)(double)(Math.min(path.getMaxDamageReduction(), damageReduction + RandomUtils.weightedRandom(damageReductionProbability))));
				}
				break;
			case 3:
				float damageReductionPhysical = wand.getDamageReductionPhysical();
				if (damageReductionPhysicalProbability.size() > 0 && damageReductionPhysical < path.getMaxDamageReductionPhysical()) {
					addedProperties = true;
					wandProperties.set("protection_physical", (Double)(double)(Math.min(path.getMaxDamageReductionPhysical(), damageReductionPhysical + RandomUtils.weightedRandom(damageReductionPhysicalProbability))));
				}
				break;
			case 4:
				float damageReductionProjectiles = wand.getDamageReductionProjectiles();
				if (damageReductionProjectilesProbability.size() > 0 && damageReductionProjectiles < path.getMaxDamageReductionProjectiles()) {
					addedProperties = true;
					wandProperties.set("protection_projectiles", (Double)(double)(Math.min(path.getMaxDamageReductionProjectiles(), damageReductionProjectiles + RandomUtils.weightedRandom(damageReductionProjectilesProbability))));
				}
				break;
			case 5:
				float damageReductionFalling = wand.getDamageReductionFalling();
				if (damageReductionFallingProbability.size() > 0 && damageReductionFalling < path.getMaxDamageReductionFalling()) {
					addedProperties = true;
					wandProperties.set("protection_falling", (Double)(double)(Math.min(path.getMaxDamageReductionFalling(), damageReductionFalling + RandomUtils.weightedRandom(damageReductionFallingProbability))));
				}
				break;
			case 6:
				float damageReductionFire = wand.getDamageReductionFire();
				if (damageReductionFireProbability.size() > 0 && damageReductionFire < path.getMaxDamageReductionFire()) {
					addedProperties = true;
					wandProperties.set("protection_fire", (Double)(double)(Math.min(path.getMaxDamageReductionFire(), damageReductionFire + RandomUtils.weightedRandom(damageReductionFireProbability))));
				}
				break;
			case 7:
				float damageReductionExplosions = wand.getDamageReductionExplosions();
				if (damageReductionExplosionsProbability.size() > 0 && damageReductionExplosions < path.getMaxDamageReductionExplosions()) {
					addedProperties = true;
					wandProperties.set("protection_explosions", (Double)(double)(Math.min(path.getMaxDamageReductionExplosions(), damageReductionExplosions + RandomUtils.weightedRandom(damageReductionExplosionsProbability))));
				}
				break;
			case 8:
				float healthRegeneration = wand.getHealthRegeneration();
				if (healthRegenerationProbability.size() > 0 && healthRegeneration < path.getMaxHealthRegeneration()) {
					addedProperties = true;
					wandProperties.set("health_regeneration", (Integer)(int)(Math.min(path.getMaxHealthRegeneration(), healthRegeneration + RandomUtils.weightedRandom(healthRegenerationProbability))));
				}
				break;
			case 9:
				float hungerRegeneration = wand.getHungerRegeneration();
				if (hungerRegenerationProbability.size() > 0 && hungerRegeneration < path.getMaxHungerRegeneration()) {
					addedProperties = true;
					wandProperties.set("hunger_regeneration", (Integer)(int)(Math.min(path.getMaxHungerRegeneration(), hungerRegeneration + RandomUtils.weightedRandom(hungerRegenerationProbability))));
				}
				break;
            case 10:
                float haste = wand.getHaste();
                if (hasteProbability.size() > 0 && haste < path.getMaxHaste()) {
                    addedProperties = true;
                    wandProperties.set("haste", (Double)(double)(Math.min(path.getMaxHaste(), haste + RandomUtils.weightedRandom(hasteProbability))));
                }
                break;
			}
		}
		
		// The mana system is considered separate from other properties

		if (costReduction > 1) {
			// Cost-Free wands don't need mana.
			wandProperties.set("xp_regeneration", 0);
			wandProperties.set("xp_max", 0);
			wandProperties.set("xp", 0);
		} else {
			int xpRegeneration = wand.getXpRegeneration();
			if (xpRegenerationProbability.size() > 0 && xpRegeneration < path.getMaxXpRegeneration()) {
				addedProperties = true;
				wandProperties.set("xp_regeneration", (Integer)(int)(Math.min(path.getMaxXpRegeneration(), xpRegeneration + RandomUtils.weightedRandom(xpRegenerationProbability))));
			}
			int xpMax = wand.getXpMax();
			if (xpMaxProbability.size() > 0 && xpMax < path.getMaxMaxXp()) {
				// Make sure the wand has at least enough xp to cast the highest costing spell it has.
				xpMax = (Integer)(int)(Math.min(path.getMaxMaxXp(), xpMax + RandomUtils.weightedRandom(xpMaxProbability)));
				xpMax = Math.max(maxXpCost, xpMax);
				wandProperties.set("xp_max", xpMax);
				addedProperties = true;
			}
			
			// Refill the wand's xp, why not
			wandProperties.set("xp", xpMax);
		}
		
		// Add or set uses to the wand
		if (additive) {
			// Only add uses to a wand if it already has some.
			int wandUses = wand.getUses();
			if (wandUses > 0 && wandUses < path.getMaxUses() && addUseProbability.size() > 0) {
				wandProperties.set("uses", Math.min(path.getMaxUses(), wandUses + RandomUtils.weightedRandom(addUseProbability)));
				addedProperties = true;
			}
		} else if (useProbability.size() > 0) {
			wandProperties.set("uses", Math.min(path.getMaxUses(), RandomUtils.weightedRandom(useProbability)));
		}

		// Set properties. This also updates name and lore.
		wand.loadProperties(wandProperties);
		
		return addedMaterials || addedSpells || addedProperties;
	}
}
