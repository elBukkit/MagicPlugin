package com.elmakers.mine.bukkit.plugins.magic.wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.blocks.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.CastingCost;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.RandomUtils;
import com.elmakers.mine.bukkit.utilities.WeightedPair;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class WandLevel {
	private static TreeMap<Integer, WandLevel> levelMap = null;
	private static int[] levels = null;
	
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
	
	// TODO- Config-driven!
	public static final int maxUses = 500;
	public static final int maxMaxXp = 1000;
	public static final int maxXpRegeneration = 50;
	public static final int maxRegeneration = 5;
	public static final float maxReduction = 0.9f;
	public static final float maxProtection = 0.9f;
	public static float maxFlySpeedIncrease = 0.1f;
	public static float maxWalkSpeedIncrease = 0.4f;
	public static float maxFlySpeed = 0.8f;
	public static float maxWalkSpeed = 0.8f;
	public static float maxPower = 1.0f;
	
	public static WandLevel getLevel(int level) {
		if (levelMap == null) return null;
		
		if (!levelMap.containsKey(level)) {
			if (level > levelMap.lastKey()) {
				return levelMap.lastEntry().getValue();
			}
			
			return levelMap.firstEntry().getValue();
		}
		
		return levelMap.get(level);
	}
	
	public static void mapLevels(ConfigurationNode template) {
		// Parse defined levels
		levelMap = new TreeMap<Integer, WandLevel>();
		String[] levelStrings = StringUtils.split(template.getString("levels"), ",");
		levels = new int[levelStrings.length];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = Integer.parseInt(levelStrings[i]);
		}
		
		for (int level = 1; level <= levels[levels.length - 1]; level++) {
			levelMap.put(level, new WandLevel(level, template));
		}
	}
	
	private WandLevel(int level, ConfigurationNode template) {
		int levelIndex = 0;
		int nextLevelIndex = 0;
		float distance = 1;
		for (levelIndex = 0; levelIndex < levels.length; levelIndex++) {
			if (level == levels[levelIndex] || levelIndex == levels.length - 1) {
				nextLevelIndex = levelIndex;
				distance = 0;
				break;
			}
			
			if (level > levels[levelIndex]) {
				nextLevelIndex = levelIndex + 1;
				int previousLevel = levels[levelIndex];
				int nextLevel = levels[nextLevelIndex];				
				distance = (float)(level - previousLevel) / (float)(nextLevel - previousLevel);
			}
		}
		
		// Fetch spell probabilities
		RandomUtils.populateStringProbabilityMap(spellProbability, template.getNode("spells"), levelIndex, nextLevelIndex, distance);
		
		// Fetch spell count probabilities
		RandomUtils.populateIntegerProbabilityMap(spellCountProbability, template.getNode("spell_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch material probabilities
		RandomUtils.populateStringProbabilityMap(materialProbability, template.getNode("materials"), levelIndex, nextLevelIndex, distance);
		
		// Fetch material count probabilities
		RandomUtils.populateIntegerProbabilityMap(materialCountProbability, template.getNode("material_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch uses
		RandomUtils.populateIntegerProbabilityMap(useProbability, template.getNode("uses"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateIntegerProbabilityMap(addUseProbability, template.getNode("add_uses"), levelIndex, nextLevelIndex, distance);
		
		// Fetch property count probability
		RandomUtils.populateIntegerProbabilityMap(propertyCountProbability, template.getNode("property_count"), levelIndex, nextLevelIndex, distance);
		
		// Fetch cost and damage reduction
		RandomUtils.populateFloatProbabilityMap(costReductionProbability, template.getNode("cost_reduction"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionProbability, template.getNode("protection"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionPhysicalProbability, template.getNode("protection_physical"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionFallingProbability, template.getNode("protection_falling"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionProjectilesProbability, template.getNode("protection_projectiles"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionFireProbability, template.getNode("protection_fire"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionExplosionsProbability, template.getNode("protection_explosions"), levelIndex, nextLevelIndex, distance);

		// Fetch regeneration
		RandomUtils.populateIntegerProbabilityMap(xpRegenerationProbability, template.getNode("xp_regeneration"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateIntegerProbabilityMap(xpMaxProbability, template.getNode("xp_max"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateIntegerProbabilityMap(healthRegenerationProbability, template.getNode("health_regeneration"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateIntegerProbabilityMap(hungerRegenerationProbability, template.getNode("hunger_regeneration"), levelIndex, nextLevelIndex, distance);
		
		// Fetch haste
		RandomUtils.populateFloatProbabilityMap(hasteProbability, template.getNode("haste"), levelIndex, nextLevelIndex, distance);
		
		// Fetch power
		RandomUtils.populateFloatProbabilityMap(powerProbability, template.getNode("power"), levelIndex, nextLevelIndex, distance);		
	}
	
	private boolean randomizeWand(Wand wand, boolean additive) {
		// Add random spells to the wand
		boolean addedSpells = false;
		Set<String> wandSpells = wand.getSpells();
		LinkedList<WeightedPair<String>> remainingSpells = new LinkedList<WeightedPair<String>>();
		for (WeightedPair<String> spell : spellProbability) {
			if (!wandSpells.contains(spell.getValue())) {
				remainingSpells.add(spell);
			}
		}
		
		Spell firstSpell = null;		
		if (remainingSpells.size() > 0) {
			Integer spellCount = RandomUtils.weightedRandom(spellCountProbability);
			int retries = 10;
			for (int i = 0; i < spellCount; i++) {
				String spellKey = RandomUtils.weightedRandom(remainingSpells);
				
				if (wand.addSpell(spellKey, false)) {	
					if (firstSpell == null) {
						firstSpell = wand.getMaster().getSpell(spellKey);
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
			Spell spell = wand.getMaster().getSpell(spellName);
			if (spell != null) {
				needsMaterials = needsMaterials || (spell instanceof BrushSpell) && !((BrushSpell)spell).hasBrushOverride();
				List<CastingCost> costs = spell.getCosts();
				if (costs != null) {
					for (CastingCost cost : costs) {
						maxXpCost = Math.max(maxXpCost, cost.getXP());
					}
				}
			}
		}
		
		// Add random materials
		boolean addedMaterials = false;
		Set<String> wandMaterials = wand.getMaterialKeys();
		LinkedList<WeightedPair<String>> remainingMaterials = new LinkedList<WeightedPair<String>>();
		for (WeightedPair<String> material : materialProbability) {
			String materialKey = material.getValue();
			// Fixup @'s to :'s .... kinda hacky, but I didn't think this through unfortunately. :\
			materialKey = materialKey.replace("|", ":");
			if (!wandMaterials.contains(material.getValue()) && MaterialBrush.isValidMaterial(materialKey, false)) {
				remainingMaterials.add(material);
			}
		}
		if (needsMaterials && remainingMaterials.size() > 0) {
			int currentMaterialCount = wand.getMaterialKeys().size();
			Integer materialCount = RandomUtils.weightedRandom(materialCountProbability);
			
			// Make sure the wand has at least one material.
			if (currentMaterialCount == 0) {
				materialCount = Math.max(1, materialCount);
			}
			int retries = 100;
			for (int i = 0; i < materialCount; i++) {
				String materialKey = RandomUtils.weightedRandom(remainingMaterials);
				materialKey = materialKey.replace("|", ":");
				if (!wand.addMaterial(materialKey, false, false)) {
					// Try again up to a certain number if we picked one the wand already had.
					if (retries-- > 0) i--;
				} else {
					addedMaterials = true;
				}
			}
		}
		
		// Add random wand properties
		Integer propertyCount = RandomUtils.weightedRandom(propertyCountProbability);
		ConfigurationNode wandProperties = new ConfigurationNode();
		double costReduction = wand.getCostReduction();
		
		while (propertyCount-- > 0) {
			int randomProperty = (int)(Math.random() * 10);
			switch (randomProperty) {
			case 0: 
				if (costReduction < maxReduction) {
					costReduction = Math.min(maxReduction, costReduction + RandomUtils.weightedRandom(costReductionProbability));
					wandProperties.setProperty("cost_reduction", costReduction);
				}
				break;
			case 1:
				float power = wand.getPower();
				if (power < maxPower) {
					wandProperties.setProperty("power", (Double)(double)(Math.min(maxPower, power + RandomUtils.weightedRandom(powerProbability))));
				}
				break;
			case 2:
				float damageReduction = wand.getDamageReduction();
				if (damageReduction < maxReduction) {
					wandProperties.setProperty("protection", (Double)(double)(Math.min(maxProtection, damageReduction + RandomUtils.weightedRandom(damageReductionProbability))));
				}
				break;
			case 3:
				float damageReductionPhysical = wand.getDamageReductionPhysical();
				if (damageReductionPhysical < maxReduction) {
					wandProperties.setProperty("protection_physical", (Double)(double)(Math.min(maxProtection, damageReductionPhysical + RandomUtils.weightedRandom(damageReductionPhysicalProbability))));
				}
				break;
			case 4:
				float damageReductionProjectiles = wand.getDamageReductionProjectiles();
				if (damageReductionProjectiles < maxReduction) {
					wandProperties.setProperty("protection_projectiles", (Double)(double)(Math.min(maxProtection, damageReductionProjectiles + RandomUtils.weightedRandom(damageReductionProjectilesProbability))));
				}
				break;
			case 5:
				float damageReductionFalling = wand.getDamageReductionFalling();
				if (damageReductionFalling < maxReduction) {
					wandProperties.setProperty("protection_falling", (Double)(double)(Math.min(maxProtection, damageReductionFalling + RandomUtils.weightedRandom(damageReductionFallingProbability))));
				}
				break;
			case 6:
				float damageReductionFire = wand.getDamageReductionFire();
				if (damageReductionFire < maxReduction) {
					wandProperties.setProperty("protection_fire", (Double)(double)(Math.min(maxProtection, damageReductionFire + RandomUtils.weightedRandom(damageReductionFireProbability))));
				}
				break;
			case 7:
				float damageReductionExplosions = wand.getDamageReductionExplosions();
				if (damageReductionExplosions < maxReduction) {
					wandProperties.setProperty("protection_explosions", (Double)(double)(Math.min(maxProtection, damageReductionExplosions + RandomUtils.weightedRandom(damageReductionExplosionsProbability))));
				}
				break;
			case 10:
				int healthRegeneration = wand.getHealthRegeneration();
				if (healthRegeneration < maxRegeneration) {
					wandProperties.setProperty("health_regeneration", (Integer)(int)(Math.min(maxRegeneration, healthRegeneration + RandomUtils.weightedRandom(healthRegenerationProbability))));
				}
				break;
			case 11:
				int hungerRegeneration = wand.getHungerRegeneration();
				if (hungerRegeneration < maxRegeneration) {
					wandProperties.setProperty("hunger_regeneration", (Integer)(int)(Math.min(maxRegeneration, hungerRegeneration + RandomUtils.weightedRandom(hungerRegenerationProbability))));
				}
				break;
			}
		}
		
		// The mana system is considered separate from other properties

		if (costReduction >= 1) {
			// Cost-Free wands don't need mana.
			wandProperties.setProperty("xp_regeneration", 0);
			wandProperties.setProperty("xp_max", 0);
			wandProperties.setProperty("xp", 0);
		} else {
			int xpRegeneration = wand.getXpRegeneration();
			if (xpRegeneration < maxXpRegeneration) {
				wandProperties.setProperty("xp_regeneration", (Integer)(int)(Math.min(maxXpRegeneration, xpRegeneration + RandomUtils.weightedRandom(xpRegenerationProbability))));
			}
			int xpMax = wand.getXpMax();
			if (xpMax < maxMaxXp) {
				// Make sure the wand has at least enough xp to cast the highest costing spell it has.
				xpMax = (Integer)(int)(Math.min(maxMaxXp, xpMax + RandomUtils.weightedRandom(xpMaxProbability)));
				xpMax = Math.max(maxXpCost, xpMax);
				wandProperties.setProperty("xp_max", xpMax);
			}
			
			// Refill the wand's xp, why not
			wandProperties.setProperty("xp", xpMax);
		}
		
		// Add or set uses to the wand
		if (additive) {
			// Only add uses to a wand if it already has some.
			int wandUses = wand.getUses();
			if (wandUses > 0 && wandUses < maxUses) {
				wandProperties.setProperty("uses", Math.min(maxUses, wandUses + RandomUtils.weightedRandom(addUseProbability)));
			}
		} else {
			wandProperties.setProperty("uses", Math.min(maxUses, RandomUtils.weightedRandom(useProbability)));
			
			// If we are creating a new wand, make a templatized name
			// based on the first spell that was added to it.
			String spellName = "Nothing";
			if (firstSpell != null) {
				spellName = firstSpell.getName();
			} 
			String updatedName = wand.getName();
			wand.setName(updatedName.replace("{Spell}", spellName));
		}

		// Set properties. This also updates name and lore.
		wand.loadProperties(wandProperties);
		
		return addedMaterials || addedSpells;
	}
	
	public static boolean randomizeWand(Wand wand, boolean additive, int level) {
		WandLevel wandLevel = getLevel(level);
		return wandLevel.randomizeWand(wand, additive);
	}
	
	public static Set<Integer> getLevels() {
		if (levels == null) return null;
		
		return levelMap.keySet();
	}
	
	public static int getMaxLevel() {
		if (levels == null) return 0;
		
		return levels[levels.length - 1];
	}
}
