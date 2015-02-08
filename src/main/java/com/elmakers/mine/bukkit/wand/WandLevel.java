package com.elmakers.mine.bukkit.wand;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;
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

	private LinkedList<WeightedPair<Integer>> spellCountProbability = new LinkedList<WeightedPair<Integer>>();
	private LinkedList<WeightedPair<Integer>> materialCountProbability = new LinkedList<WeightedPair<Integer>>();
	private LinkedList<WeightedPair<String>> spellProbability = new LinkedList<WeightedPair<String>>();
	private LinkedList<WeightedPair<String>> materialProbability = new LinkedList<WeightedPair<String>>();
	private LinkedList<WeightedPair<Integer>> useProbability = new LinkedList<WeightedPair<Integer>>();
	private LinkedList<WeightedPair<Integer>> addUseProbability = new LinkedList<WeightedPair<Integer>>();

	private LinkedList<WeightedPair<Integer>> propertyCountProbability = new LinkedList<WeightedPair<Integer>>();
	private LinkedList<WeightedPair<Float>> costReductionProbability = new LinkedList<WeightedPair<Float>>();
	private LinkedList<WeightedPair<Float>> powerProbability = new LinkedList<WeightedPair<Float>>();
	private LinkedList<WeightedPair<Float>> damageReductionProbability = new LinkedList<WeightedPair<Float>>();
	private LinkedList<WeightedPair<Float>> damageReductionPhysicalProbability = new LinkedList<WeightedPair<Float>>();
	private LinkedList<WeightedPair<Float>> damageReductionProjectilesProbability = new LinkedList<WeightedPair<Float>>();
	private LinkedList<WeightedPair<Float>> damageReductionFallingProbability = new LinkedList<WeightedPair<Float>>();
	private LinkedList<WeightedPair<Float>> damageReductionFireProbability = new LinkedList<WeightedPair<Float>>();
	private LinkedList<WeightedPair<Float>> damageReductionExplosionsProbability = new LinkedList<WeightedPair<Float>>();
	
	private LinkedList<WeightedPair<Integer>> xpRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	private LinkedList<WeightedPair<Integer>> xpMaxProbability = new LinkedList<WeightedPair<Integer>>();
	private LinkedList<WeightedPair<Integer>> healthRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	private LinkedList<WeightedPair<Integer>> hungerRegenerationProbability = new LinkedList<WeightedPair<Integer>>();
	
	private LinkedList<WeightedPair<Float>> hasteProbability = new LinkedList<WeightedPair<Float>>();

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
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(xpRegenerationProbability, template.getConfigurationSection("mana_regeneration"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(xpMaxProbability, template.getConfigurationSection("mana_max"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(healthRegenerationProbability, template.getConfigurationSection("health_regeneration"), levelIndex, nextLevelIndex, distance);
		com.elmakers.mine.bukkit.utility.RandomUtils.populateIntegerProbabilityMap(hungerRegenerationProbability, template.getConfigurationSection("hunger_regeneration"), levelIndex, nextLevelIndex, distance);
		
		// Fetch haste
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(hasteProbability, template.getConfigurationSection("haste"), levelIndex, nextLevelIndex, distance);
		
		// Fetch power
		com.elmakers.mine.bukkit.utility.RandomUtils.populateFloatProbabilityMap(powerProbability, template.getConfigurationSection("power"), levelIndex, nextLevelIndex, distance);		
	}

    public void add(WandLevel other) {
        spellProbability = RandomUtils.merge(spellProbability, other.spellProbability);
        materialProbability = RandomUtils.merge(materialProbability, other.materialProbability);

        this.materialCountProbability = materialCountProbability.isEmpty() ? other.materialCountProbability : materialCountProbability;
        this.spellCountProbability = spellCountProbability.isEmpty() ? other.spellCountProbability : spellCountProbability;
        this.useProbability = useProbability.isEmpty() ? other.useProbability : useProbability;
        this.addUseProbability = addUseProbability.isEmpty() ? other.addUseProbability : addUseProbability;
        this.propertyCountProbability = propertyCountProbability.isEmpty() ? other.propertyCountProbability : propertyCountProbability;
        this.costReductionProbability = costReductionProbability.isEmpty() ? other.costReductionProbability : costReductionProbability;
        this.powerProbability = powerProbability.isEmpty() ? other.powerProbability : powerProbability;
        this.damageReductionProbability = damageReductionProbability.isEmpty() ? other.damageReductionProbability : damageReductionProbability;
        this.damageReductionPhysicalProbability = damageReductionPhysicalProbability.isEmpty() ? other.damageReductionPhysicalProbability : damageReductionPhysicalProbability;
        this.damageReductionProjectilesProbability = damageReductionProjectilesProbability.isEmpty() ? other.damageReductionProjectilesProbability : damageReductionProjectilesProbability;
        this.damageReductionFallingProbability = damageReductionFallingProbability.isEmpty() ? other.damageReductionFallingProbability : damageReductionFallingProbability;
        this.damageReductionFireProbability = damageReductionFireProbability.isEmpty() ? other.damageReductionFireProbability : damageReductionFireProbability;
        this.damageReductionExplosionsProbability = damageReductionExplosionsProbability.isEmpty() ? other.damageReductionExplosionsProbability : damageReductionExplosionsProbability;
        this.xpRegenerationProbability = xpRegenerationProbability.isEmpty() ? other.xpRegenerationProbability : xpRegenerationProbability;
        this.xpMaxProbability = xpMaxProbability.isEmpty() ? other.xpMaxProbability : xpMaxProbability;
        this.healthRegenerationProbability = healthRegenerationProbability.isEmpty() ? other.healthRegenerationProbability : healthRegenerationProbability;
        this.hungerRegenerationProbability = hungerRegenerationProbability.isEmpty() ? other.hungerRegenerationProbability : hungerRegenerationProbability;
    }

    protected void sendAddMessage(Mage mage,  Wand wand, String messageKey, String nameParam) {
        if (mage == null) return;

        String message = mage.getController().getMessages().get(messageKey)
            .replace("$name", nameParam)
            .replace("$wand", wand.getName());
        mage.sendMessage(message);
    }
	
	public boolean randomizeWand(Mage mage, Wand wand, boolean additive) {
		// Add random spells to the wand
        if (mage == null) {
            mage = wand.getActivePlayer();
        }
        Messages messages = wand.getController().getMessages();
		boolean addedSpells = false;
		LinkedList<WeightedPair<String>> remainingSpells = new LinkedList<WeightedPair<String>>();
        for (WeightedPair<String> spell : spellProbability) {
            if (!wand.hasSpell(spell.getValue())) {
                remainingSpells.add(spell);
            }
        }

		SpellTemplate firstSpell = null;
		if (remainingSpells.size() > 0) {
			Integer spellCount = RandomUtils.weightedRandom(spellCountProbability);
			for (int i = 0; spellCount != null && i < spellCount; i++) {
				String spellKey = RandomUtils.weightedRandom(remainingSpells);
                SpellTemplate currentSpell = wand.getBaseSpell(spellKey);
				if (wand.addSpell(spellKey)) {
                    SpellTemplate spell = wand.getMaster().getSpellTemplate(spellKey);
                    if (mage != null && spell != null) {
                        if (currentSpell != null) {
                            String levelDescription = spell.getLevelDescription();
                            if (levelDescription == null || levelDescription.isEmpty()) {
                                levelDescription = spell.getName();
                            }
                            mage.sendMessage(messages.get("wand.spell_upgraded").replace("$name", currentSpell.getName()).replace("$wand", wand.getName()).replace("$level", levelDescription));
                        } else {
                            mage.sendMessage(messages.get("wand.spell_added").replace("$name", spell.getName()).replace("$wand", wand.getName()));
                        }
                    }
					if (firstSpell == null) {
						firstSpell = spell;
					}
					addedSpells = true;
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
				needsMaterials = needsMaterials || spell.usesBrush();
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
                    if (mage != null) {
                        mage.sendMessage(messages.get("wand.brush_added").replace("$wand", wand.getName()).replace("$name", MaterialAndData.getMaterialName(materialKey)));
                    }
				}
			}
		}
		
		// Add random wand properties
		boolean addedProperties = false;
		Integer propertyCount = propertyCountProbability.size() == 0 ? 0 : RandomUtils.weightedRandom(propertyCountProbability);
		ConfigurationSection wandProperties = new MemoryConfiguration();
		double costReduction = wand.getCostReduction();
		
		while (propertyCount != null && propertyCount-- > 0) {
			int randomProperty = (int)(Math.random() * 11);
			switch (randomProperty) {
			case 0: 
				if (costReductionProbability.size() > 0 && costReduction < path.getMaxCostReduction()) {
					addedProperties = true;
					costReduction = Math.min(path.getMaxCostReduction(), costReduction + RandomUtils.weightedRandom(costReductionProbability));
					wandProperties.set("cost_reduction", costReduction);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.cost_reduction", (float)costReduction));
				}
				break;
			case 1:
				double power = wand.getPower();
				if (powerProbability.size() > 0 && power < path.getMaxPower()) {
					addedProperties = true;
                    power = Math.min(path.getMaxPower(), power + RandomUtils.weightedRandom(powerProbability));
					wandProperties.set("power", power);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.power", (float)power));
				}
				break;
			case 2:
				double damageReduction = wand.getDamageReduction();
				if (damageReductionProbability.size() > 0 && damageReduction < path.getMaxDamageReduction()) {
					addedProperties = true;
                    damageReduction = Math.min(path.getMaxDamageReduction(), damageReduction + RandomUtils.weightedRandom(damageReductionProbability));
					wandProperties.set("protection", damageReduction);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.protection", (float)damageReduction));
				}
				break;
			case 3:
				double damageReductionPhysical = wand.getDamageReductionPhysical();
				if (damageReductionPhysicalProbability.size() > 0 && damageReductionPhysical < path.getMaxDamageReductionPhysical()) {
					addedProperties = true;
                    damageReductionPhysical = Math.min(path.getMaxDamageReductionPhysical(), damageReductionPhysical + RandomUtils.weightedRandom(damageReductionPhysicalProbability));
					wandProperties.set("protection_physical", damageReductionPhysical);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.protection_physical", (float)damageReductionPhysical));
                }
				break;
			case 4:
                double damageReductionProjectiles = wand.getDamageReductionProjectiles();
				if (damageReductionProjectilesProbability.size() > 0 && damageReductionProjectiles < path.getMaxDamageReductionProjectiles()) {
					addedProperties = true;
                    damageReductionProjectiles = Math.min(path.getMaxDamageReductionProjectiles(), damageReductionProjectiles + RandomUtils.weightedRandom(damageReductionProjectilesProbability));
					wandProperties.set("protection_projectiles", damageReductionProjectiles);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.protection_projectile", (float)damageReductionProjectiles));
				}
				break;
			case 5:
                double damageReductionFalling = wand.getDamageReductionFalling();
				if (damageReductionFallingProbability.size() > 0 && damageReductionFalling < path.getMaxDamageReductionFalling()) {
					addedProperties = true;
                    damageReductionFalling = Math.min(path.getMaxDamageReductionFalling(), damageReductionFalling + RandomUtils.weightedRandom(damageReductionFallingProbability));
					wandProperties.set("protection_falling", damageReductionFalling);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.protection_fall", (float)damageReductionFalling));
				}
				break;
			case 6:
                double damageReductionFire = wand.getDamageReductionFire();
				if (damageReductionFireProbability.size() > 0 && damageReductionFire < path.getMaxDamageReductionFire()) {
					addedProperties = true;
                    damageReductionFire = Math.min(path.getMaxDamageReductionFire(), damageReductionFire + RandomUtils.weightedRandom(damageReductionFireProbability));
					wandProperties.set("protection_fire", damageReductionFire);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.protection_fire", (float)damageReductionFire));
				}
				break;
			case 7:
                double damageReductionExplosions = wand.getDamageReductionExplosions();
				if (damageReductionExplosionsProbability.size() > 0 && damageReductionExplosions < path.getMaxDamageReductionExplosions()) {
					addedProperties = true;
                    damageReductionExplosions = Math.min(path.getMaxDamageReductionExplosions(), damageReductionExplosions + RandomUtils.weightedRandom(damageReductionExplosionsProbability));
					wandProperties.set("protection_explosions", damageReductionExplosions);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.protection_blast", (float)damageReductionExplosions));
				}
				break;
			case 8:
                double healthRegeneration = wand.getHealthRegeneration();
				if (healthRegenerationProbability.size() > 0 && healthRegeneration < path.getMaxHealthRegeneration()) {
					addedProperties = true;
                    healthRegeneration = Math.min(path.getMaxHealthRegeneration(), healthRegeneration + RandomUtils.weightedRandom(healthRegenerationProbability));
					wandProperties.set("health_regeneration", healthRegeneration);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.health_regeneration", (float)healthRegeneration));
				}
				break;
			case 9:
                double hungerRegeneration = wand.getHungerRegeneration();
				if (hungerRegenerationProbability.size() > 0 && hungerRegeneration < path.getMaxHungerRegeneration()) {
					addedProperties = true;
                    hungerRegeneration = Math.min(path.getMaxHungerRegeneration(), hungerRegeneration + RandomUtils.weightedRandom(hungerRegenerationProbability));
					wandProperties.set("hunger_regeneration", hungerRegeneration);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.hunger_regeneration", (float)hungerRegeneration));
				}
				break;
            case 10:
                double haste = wand.getHaste();
                if (hasteProbability.size() > 0 && haste < path.getMaxHaste()) {
                    addedProperties = true;
                    haste = Math.min(path.getMaxHaste(), haste + RandomUtils.weightedRandom(hasteProbability));
                    wandProperties.set("haste", haste);
                    sendAddMessage(mage, wand, "wand.upgraded_property", Wand.getLevelString(messages, "wand.haste", (float)haste));
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
                xpRegeneration = Math.min(path.getMaxXpRegeneration(), xpRegeneration + RandomUtils.weightedRandom(xpRegenerationProbability));
                wandProperties.set("xp_regeneration", xpRegeneration);

                String updateString = messages.get("wand.mana_regeneration");
                updateString = updateString.replace("$amount", Integer.toString(xpRegeneration));
                sendAddMessage(mage, wand, "wand.upgraded_property", updateString);
			}
			int xpMax = wand.getXpMax();
			if (xpMaxProbability.size() > 0 && xpMax < path.getMaxMaxXp()) {
				xpMax = (Integer)(int)(Math.min(path.getMaxMaxXp(), xpMax + RandomUtils.weightedRandom(xpMaxProbability)));
                if (path.getMatchSpellMana()) {
                    // Make sure the wand has at least enough xp to cast the highest costing spell it has.
                    xpMax = Math.max(maxXpCost, xpMax);
                }
				wandProperties.set("xp_max", xpMax);
                addedProperties = true;

                String updateString = messages.get("wand.mana_amount");
                updateString = updateString.replace("$amount", Integer.toString(xpMax));
                sendAddMessage(mage, wand, "wand.upgraded_property", updateString);
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

		// Set properties.
		wand.loadProperties(wandProperties);
		return addedMaterials || addedSpells || addedProperties;
	}
}
