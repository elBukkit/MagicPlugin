package com.elmakers.mine.bukkit.plugins.magic;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.utilities.RandomUtils;
import com.elmakers.mine.bukkit.utilities.WeightedPair;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class WandLevel {
	private static TreeMap<Integer, WandLevel> levelMap = null;
	private static int[] levels = null;
	
	private final LinkedList<WeightedPair<Integer>> spellCountProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<String>> spellProbability = new LinkedList<WeightedPair<String>>();
	private final LinkedList<WeightedPair<Integer>> useProbability = new LinkedList<WeightedPair<Integer>>();
	private final LinkedList<WeightedPair<Integer>> addUseProbability = new LinkedList<WeightedPair<Integer>>();

	private final LinkedList<WeightedPair<Float>> costReductionProbability = new LinkedList<WeightedPair<Float>>();
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
	
	public static final int maxUses = 500;
	public static final int maxMaxXp = 10000;
	public static final int maxXpRegeneration = 100;
	public static final int maxRegeneration = 20;
	public static final float maxReduction = 0.9f;
	public static final float maxProtection = 0.9f;
	public static float maxWalkSpeedIncrease = 0.7f;
	
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
		
		for (int level = 1; level < levels[levels.length - 1]; level++) {
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
		
		// Fetch uses
		RandomUtils.populateIntegerProbabilityMap(useProbability, template.getNode("uses"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateIntegerProbabilityMap(addUseProbability, template.getNode("add_uses"), levelIndex, nextLevelIndex, distance);
		
		// Fetch cost and damage reduction
		RandomUtils.populateFloatProbabilityMap(costReductionProbability, template.getNode("cost_reduction"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionProbability, template.getNode("damage_reduction"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionPhysicalProbability, template.getNode("damage_reduction_physical"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionFallingProbability, template.getNode("damage_reduction_falling"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionProjectilesProbability, template.getNode("damage_reduction_projectiles"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionFireProbability, template.getNode("damage_reduction_fire"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateFloatProbabilityMap(damageReductionExplosionsProbability, template.getNode("damage_reduction_explosions"), levelIndex, nextLevelIndex, distance);

		// Fetch regeneration
		RandomUtils.populateIntegerProbabilityMap(xpRegenerationProbability, template.getNode("xp_regeneration"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateIntegerProbabilityMap(xpMaxProbability, template.getNode("xp_max"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateIntegerProbabilityMap(healthRegenerationProbability, template.getNode("health_regeneration"), levelIndex, nextLevelIndex, distance);
		RandomUtils.populateIntegerProbabilityMap(hungerRegenerationProbability, template.getNode("hunger_regeneration"), levelIndex, nextLevelIndex, distance);
		
		// Fetch haste
		RandomUtils.populateFloatProbabilityMap(hasteProbability, template.getNode("haste"), levelIndex, nextLevelIndex, distance);
	}
	
	private void randomizeWand(Wand wand, boolean additive) {
		// Add random spells to the wand
		Spell firstSpell = null;		
		Integer spellCount = RandomUtils.weightedRandom(spellCountProbability);
		int retries = 30;
		for (int i = 0; i < spellCount; i++) {
			String spellKey = RandomUtils.weightedRandom(spellProbability);
			
			if (wand.addSpell(spellKey)) {	
				if (firstSpell == null) {
					firstSpell = wand.getMaster().getSpell(spellKey);
				}
			} else {
				// Try again up to a certain number if we picked one the wand already had.
				if (retries-- > 0) i--;
			}
		}
		
		// Look through all spells for the max XP casting cost
		int maxXpCost = 0;
		String[] spells = wand.getSpells();
		for (int i = 0; i < spells.length; i++) {
			String[] pieces = StringUtils.split(spells[i], "@");
			Spell spell = wand.getMaster().getSpell(pieces[0]);
			if (spell != null) {
				List<CastingCost> costs = spell.getCosts();
				for (CastingCost cost : costs) {
					maxXpCost = Math.max(maxXpCost, cost.getXP());
				}
			}
		}
		
		// Add random wand properties
		ConfigurationNode wandProperties = new ConfigurationNode();
		float costReduction = wand.getCostReduction();
		if (costReduction < maxReduction) {
			wandProperties.setProperty("cost_reduction", (Double)(double)(Math.min(maxReduction, costReduction + RandomUtils.weightedRandom(costReductionProbability))));
		}
		float damageReduction = wand.getDamageReduction();
		if (damageReduction < maxReduction) {
			wandProperties.setProperty("damage_reduction", (Double)(double)(Math.min(maxProtection, damageReduction + RandomUtils.weightedRandom(damageReductionProbability))));
		}
		float damageReductionPhysical = wand.getDamageReductionPhysical();
		if (damageReductionPhysical < maxReduction) {
			wandProperties.setProperty("damage_reduction_physical", (Double)(double)(Math.min(maxProtection, damageReductionPhysical + RandomUtils.weightedRandom(damageReductionPhysicalProbability))));
		}
		float damageReductionProjectiles = wand.getDamageReductionProjectiles();
		if (damageReductionProjectiles < maxReduction) {
			wandProperties.setProperty("damage_reduction_projectiles", (Double)(double)(Math.min(maxProtection, damageReductionProjectiles + RandomUtils.weightedRandom(damageReductionProjectilesProbability))));
		}
		float damageReductionFalling = wand.getDamageReductionFalling();
		if (damageReductionFalling < maxReduction) {
			wandProperties.setProperty("damage_reduction_falling", (Double)(double)(Math.min(maxProtection, damageReductionFalling + RandomUtils.weightedRandom(damageReductionFallingProbability))));
		}
		float damageReductionFire = wand.getDamageReductionFire();
		if (damageReductionFire < maxReduction) {
			wandProperties.setProperty("damage_reduction_fire", (Double)(double)(Math.min(maxProtection, damageReductionFire + RandomUtils.weightedRandom(damageReductionFireProbability))));
		}
		float damageReductionExplosions = wand.getDamageReductionExplosions();
		if (damageReductionExplosions < maxReduction) {
			wandProperties.setProperty("damage_reduction_explosions", (Double)(double)(Math.min(maxProtection, damageReductionExplosions + RandomUtils.weightedRandom(damageReductionExplosionsProbability))));
		}
		int xpRegeneration = wand.getXpRegeneration();
		if (xpRegeneration < maxXpRegeneration) {
			wandProperties.setProperty("xp_regeneration", (Integer)(int)(Math.min(maxXpRegeneration, xpRegeneration + RandomUtils.weightedRandom(xpRegenerationProbability))));
		}
		int xpMax = wand.getXpMax();
		if (xpMax < maxMaxXp) {
			// Make sure the wand has at least enough xp to cast the highest costing spell it has.
			int newMaxXp = (Integer)(int)(Math.min(maxMaxXp, xpMax + RandomUtils.weightedRandom(xpMaxProbability)));
			wandProperties.setProperty("xp_max", Math.min(maxXpCost, newMaxXp));
		}
		int healthRegeneration = wand.getHealthRegeneration();
		if (healthRegeneration < maxRegeneration) {
			wandProperties.setProperty("health_regeneration", (Integer)(int)(Math.min(maxRegeneration, healthRegeneration + RandomUtils.weightedRandom(healthRegenerationProbability))));
		}
		int hungerRegeneration = wand.getHungerRegeneration();
		if (hungerRegeneration < maxRegeneration) {
			wandProperties.setProperty("hunger_regeneration", (Integer)(int)(Math.min(maxRegeneration, hungerRegeneration + RandomUtils.weightedRandom(hungerRegenerationProbability))));
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
		wand.configureProperties(wandProperties);
	}
	
	public static void randomizeWand(Wand wand, boolean additive, int level) {
		WandLevel wandLevel = getLevel(level);
		wandLevel.randomizeWand(wand, additive);
	}
	
	public static Set<Integer> getLevels() {
		if (levels == null) return null;
		
		return levelMap.keySet();
	}
}
