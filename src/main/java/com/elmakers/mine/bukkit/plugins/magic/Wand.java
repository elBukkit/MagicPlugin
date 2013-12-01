package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.Configuration;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Wand implements CostReducer {
	private ItemStack item;
	private Spells spells;
	private PlayerSpells activePlayer;
	
	// Cached state
	private String id;
	private String wandSpells = "";
	private String wandMaterials = "";
	
	private String activeSpell = "";
	private String activeMaterial = "";
	private String wandName = "";
	
	private float costReduction = 0;
	private float cooldownReduction = 0;
	private float damageReduction = 0;
	private float damageReductionPhysical = 0;
	private float damageReductionProjectiles = 0;
	private float damageReductionFalling = 0;
	private float damageReductionFire = 0;
	private float damageReductionExplosions = 0;
	private float power = 0;
	private boolean hasInventory = false;
	private int uses = 0;
	private int xp = 0;
	
	private int xpRegeneration = 0;
	private int xpMax = 50;
	private int healthRegeneration = 0;
	private int hungerRegeneration = 0;
	
	private float defaultWalkSpeed = 0.2f;
	private float walkSpeedIncrease = 0;
	
	private int storedXpLevel = 0;
	private int storedXp = 0;
	private float storedXpProgress = 0;
	
	private static DecimalFormat floatFormat = new DecimalFormat("#.###");
	
	public static Material WandMaterial = Material.BLAZE_ROD;
	public static Material EnchantableWandMaterial = Material.WOOD_SWORD;
	public static Material EraseMaterial = Material.SULPHUR;
	public static Material CopyMaterial = Material.PUMPKIN_SEEDS;
	
	// Wand configurations
	protected static Map<String, ConfigurationNode> wandTemplates = new HashMap<String, ConfigurationNode>();
	private static final String propertiesFileName = "wands.yml";
	private static final String propertiesFileNameDefaults = "wands.defaults.yml";
	private static final String defaultWandName = "Wand";
	
	public Wand(Spells spells) {
		this.spells = spells;
		item = new ItemStack(WandMaterial);
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		item = InventoryUtils.getCopy(item);
		ItemMeta itemMeta = item.getItemMeta();
		item.setItemMeta(itemMeta);

		InventoryUtils.addGlow(item);
		id = UUID.randomUUID().toString();
		wandName = defaultWandName;
		updateName();
		saveState();
	}
	
	public Wand(Spells spells, ItemStack item) {
		this.item = item;
		this.spells = spells;
		loadState();
	}

	public void setActiveSpell(String activeSpell) {
		this.activeSpell = activeSpell;
		updateName();
		updateInventoryNames(true);
		saveState();
	}

	@SuppressWarnings("deprecation")
	public void setActiveMaterial(Material material, byte data) {
		if (material == CopyMaterial) {
			this.activeMaterial = "-1:0";
		} else if (material == EraseMaterial) {
			this.activeMaterial = "0:0";
		} else {
			this.activeMaterial = material.getId() + ":" + data;
		}
		updateName();
		updateActiveMaterial();
		updateInventoryNames(true);
		saveState();
	}
	
	public int getXpRegeneration() {
		return xpRegeneration;
	}

	public int getXpMax() {
		return xpMax;
	}

	public int getHealthRegeneration() {
		return healthRegeneration;
	}

	public int getHungerRegeneration() {
		return hungerRegeneration;
	}

	public float getCostReduction() {
		return costReduction;
	}

	public float getCooldownReduction() {
		return costReduction;
	}
	
	public boolean getHasInventory() {
		return hasInventory;
	}

	public float getPower() {
		return power;
	}

	public float getDamageReduction() {
		return damageReduction;
	}

	public float getDamageReductionPhysical() {
		return damageReductionPhysical;
	}
	
	public float getDamageReductionProjectiles() {
		return damageReductionProjectiles;
	}

	public float getDamageReductionFalling() {
		return damageReductionFalling;
	}

	public float getDamageReductionFire() {
		return damageReductionFire;
	}

	public float getDamageReductionExplosions() {
		return damageReductionExplosions;
	}

	public int getUses() {
		return uses;
	}
	
	public String getName() {
		return wandName;
	}
	
	protected void setName(String name) {
		wandName = name;
		updateName();
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public void setItem(ItemStack item) {
		this.item = item;
	}

	protected void saveState() {
		Object wandNode = InventoryUtils.createNode(item, "wand");
		
		InventoryUtils.setMeta(wandNode, "id", id);
		InventoryUtils.setMeta(wandNode, "materials", wandMaterials);
		InventoryUtils.setMeta(wandNode, "spells", wandSpells);
		InventoryUtils.setMeta(wandNode, "active_spell", activeSpell);
		InventoryUtils.setMeta(wandNode, "active_material", activeMaterial);
		InventoryUtils.setMeta(wandNode, "name", wandName);
	
		InventoryUtils.setMeta(wandNode, "cost_reduction", floatFormat.format(costReduction));
		InventoryUtils.setMeta(wandNode, "cooldown_reduction", floatFormat.format(cooldownReduction));
		InventoryUtils.setMeta(wandNode, "power", floatFormat.format(power));
		InventoryUtils.setMeta(wandNode, "damage_reduction", floatFormat.format(damageReduction));
		InventoryUtils.setMeta(wandNode, "damage_reduction_physical", floatFormat.format(damageReductionPhysical));
		InventoryUtils.setMeta(wandNode, "damage_reduction_projectiles", floatFormat.format(damageReductionProjectiles));
		InventoryUtils.setMeta(wandNode, "damage_reduction_falling", floatFormat.format(damageReductionFalling));
		InventoryUtils.setMeta(wandNode, "damage_reduction_fire", floatFormat.format(damageReductionFire));
		InventoryUtils.setMeta(wandNode, "damage_reduction_explosions", floatFormat.format(damageReductionExplosions));
		InventoryUtils.setMeta(wandNode, "cooldown_reduction", floatFormat.format(cooldownReduction));
		InventoryUtils.setMeta(wandNode, "haste", floatFormat.format(walkSpeedIncrease));
		InventoryUtils.setMeta(wandNode, "xp", Integer.toString(xp));
		InventoryUtils.setMeta(wandNode, "xp_regeneration", Integer.toString(xpRegeneration));
		InventoryUtils.setMeta(wandNode, "xp_max", Integer.toString(xpMax));
		InventoryUtils.setMeta(wandNode, "health_regeneration", Integer.toString(healthRegeneration));
		InventoryUtils.setMeta(wandNode, "hunger_regeneration", Integer.toString(hungerRegeneration));
		InventoryUtils.setMeta(wandNode, "uses", Integer.toString(uses));
		InventoryUtils.setMeta(wandNode, "has_inventory", Integer.toString((hasInventory ? 1 : 0)));
	}
	
	protected void loadOldState(ItemStack item) {
		id = InventoryUtils.getMeta(item, "magic_wand_id");
        id = id == null || id.length() == 0 ? UUID.randomUUID().toString() : id;
        String wandSettings = InventoryUtils.getMeta(item, "magic_wand");
        wandSettings = wandSettings == null ? "" : wandSettings;
        wandMaterials = InventoryUtils.getMeta(item, "magic_materials");
        wandMaterials = wandMaterials == null ? "" : wandMaterials;
        wandSpells = InventoryUtils.getMeta(item, "magic_spells");
        wandSpells = wandSpells == null ? "" : wandSpells;
        activeSpell = InventoryUtils.getMeta(item, "magic_active_spell");
        activeSpell = activeSpell == null ? "" : activeSpell;
        activeMaterial = InventoryUtils.getMeta(item, "magic_active_material");
        activeMaterial = activeMaterial == null ? "" : activeMaterial;
        wandName = InventoryUtils.getMeta(item, "magic_wand_name");
        wandName = wandName == null ? defaultWandName : wandName;
        
        String[] wandPairs = StringUtils.split(wandSettings, "&");
        for (String pair : wandPairs) {
                String[] keyValue = StringUtils.split(pair, "=");
                if (keyValue.length == 2) {
                        String key = keyValue[0];
                        float value = Float.parseFloat(keyValue[1]);
                        if (key.equalsIgnoreCase("cr")) {
                                costReduction = value;
                        } else if (key.equalsIgnoreCase("cdr")) {
                                 cooldownReduction = value;
                        } else if (key.equalsIgnoreCase("dr")) {
                                damageReduction = value;
                        } else if (key.equalsIgnoreCase("drph")) {
                                damageReductionPhysical = value;
                        } else if (key.equalsIgnoreCase("drpr")) {
                                damageReductionProjectiles = value;
                        } else if (key.equalsIgnoreCase("drfa")) {
                                damageReductionFalling = value;
                        } else if (key.equalsIgnoreCase("drfi")) {
                                damageReductionFire = value;
                        } else if (key.equalsIgnoreCase("drex")) {
                                damageReductionExplosions = value;
                        } else if (key.equalsIgnoreCase("uses")) {
                                uses = (int)value;
                        } else if (key.equalsIgnoreCase("xpre")) {
                                xpRegeneration = (int)value;
                        } else if (key.equalsIgnoreCase("xpmax")) {
                                xpMax = (int)value;
                        } else if (key.equalsIgnoreCase("hereg")) {
                                healthRegeneration = (int)value;
                        } else if (key.equalsIgnoreCase("hureg")) {
                                hungerRegeneration = (int)value;
                        } else if (key.equalsIgnoreCase("hasi")) {
                                hasInventory = (int)value != 0;
                        }
                }
        }
	}
	
	protected void loadState() {
		Object wandNode = InventoryUtils.getNode(item, "wand");
		if (wandNode == null) {
			// Try to update old wands, this could be removed eventually.
			loadOldState(item);
            return;
		}
		
		// Don't generate a UUID unless we need to, not sure how expensive that is.
		id = InventoryUtils.getMeta(wandNode, "id");
		id = id == null || id.length() == 0 ? UUID.randomUUID().toString() : id;
		
		wandMaterials = InventoryUtils.getMeta(wandNode, "materials", wandMaterials);
		wandSpells = InventoryUtils.getMeta(wandNode, "spells", wandSpells);
		activeSpell = InventoryUtils.getMeta(wandNode, "active_spell", activeSpell);
		activeMaterial = InventoryUtils.getMeta(wandNode, "active_material", activeMaterial);
		wandName = InventoryUtils.getMeta(wandNode, "name", wandName);
		
		costReduction = Float.parseFloat(InventoryUtils.getMeta(wandNode, "cost_reduction", floatFormat.format(costReduction)));
		cooldownReduction = Float.parseFloat(InventoryUtils.getMeta(wandNode, "cooldown_reduction", floatFormat.format(cooldownReduction)));
		power = Float.parseFloat(InventoryUtils.getMeta(wandNode, "power", floatFormat.format(power)));
		damageReduction = Float.parseFloat(InventoryUtils.getMeta(wandNode, "damage_reduction", floatFormat.format(damageReduction)));
		damageReductionPhysical = Float.parseFloat(InventoryUtils.getMeta(wandNode, "damage_reduction_physical", floatFormat.format(damageReductionPhysical)));
		damageReductionProjectiles = Float.parseFloat(InventoryUtils.getMeta(wandNode, "damage_reduction_projectiles", floatFormat.format(damageReductionProjectiles)));
		damageReductionFalling = Float.parseFloat(InventoryUtils.getMeta(wandNode, "damage_reduction_falling", floatFormat.format(damageReductionFalling)));
		damageReductionFire = Float.parseFloat(InventoryUtils.getMeta(wandNode, "damage_reduction_fire", floatFormat.format(damageReductionFire)));
		damageReductionExplosions = Float.parseFloat(InventoryUtils.getMeta(wandNode, "damage_reduction_explosions", floatFormat.format(damageReductionExplosions)));
		cooldownReduction = Float.parseFloat(InventoryUtils.getMeta(wandNode, "cooldown_reduction", floatFormat.format(cooldownReduction)));
		walkSpeedIncrease = Float.parseFloat(InventoryUtils.getMeta(wandNode, "haste", floatFormat.format(walkSpeedIncrease)));
		xp = Integer.parseInt(InventoryUtils.getMeta(wandNode, "xp", Integer.toString(xp)));
		xpRegeneration = Integer.parseInt(InventoryUtils.getMeta(wandNode, "xp_regeneration", Integer.toString(xpRegeneration)));
		xpMax = Integer.parseInt(InventoryUtils.getMeta(wandNode, "xp_max", Integer.toString(xpMax)));
		healthRegeneration = Integer.parseInt(InventoryUtils.getMeta(wandNode, "health_regeneration", Integer.toString(healthRegeneration)));
		hungerRegeneration = Integer.parseInt(InventoryUtils.getMeta(wandNode, "hunger_regeneration", Integer.toString(hungerRegeneration)));
		uses = Integer.parseInt(InventoryUtils.getMeta(wandNode, "uses", Integer.toString(uses)));
		hasInventory = Integer.parseInt(InventoryUtils.getMeta(wandNode, "has_inventory", (hasInventory ? "1" : "0"))) != 0;
		
		// This is done here as an extra safety measure.
		// A walk speed too high will cause a server error.
		walkSpeedIncrease = Math.min(WandLevel.maxWalkSpeedIncrease, walkSpeedIncrease);
	}
	
	@SuppressWarnings("deprecation")
	public void removeMaterial(Material material, byte data) {
		Integer id = material.getId();
		String materialString = id.toString();
		materialString += ":" + data;

		String[] materials = getMaterials();
		String fallbackActiveMaterial = "";
		List<String> materialMap = new LinkedList<String>();
		for (int i = 0; i < materials.length; i++) {	
			String[] pieces = StringUtils.split(materials[i], "@");
			if (pieces.length > 0 && !pieces[0].equals(materialString)) {
				fallbackActiveMaterial = materials[i];
				materialMap.add(materials[i]);
			}
		}
		setMaterials(materialMap);
		
		if (materialString.equalsIgnoreCase(activeMaterial)) {
			activeMaterial = fallbackActiveMaterial;
			updateActiveMaterial();
			updateName();
			if (isInventoryOpen()) {
				updateInventory();
			}
		}
	}
	
	public boolean addMaterial(Material material, byte data) {
		return addMaterial(material, data, true);
	}
	
	@SuppressWarnings("deprecation")
	public boolean addMaterial(Material material, byte data, boolean makeActive) {
		Integer id = material.getId();
		String materialString = id.toString();
		if (material == EraseMaterial) {
			materialString = "0";
		} else if (material == CopyMaterial) {
			materialString = "-1";
		}
		materialString += ":" + data;

		String[] materials = getMaterials();
		Set<String> materialMap = new TreeSet<String>();
		for (int i = 0; i < materials.length; i++) {	
			String[] pieces = StringUtils.split(materials[i], "@");
			if (pieces.length > 0 && !pieces[0].equals(materialString)) {
				materialMap.add(materials[i]);
			}
		}
		if (makeActive || activeMaterial == null || activeMaterial.length() == 0) {
			activeMaterial = materialString;
		}
		boolean addedNew = !materialMap.contains(materialString);
		materialMap.add(materialString);
		setMaterials(materialMap);
		updateActiveMaterial();
		updateName();
		if (isInventoryOpen()) {
			updateInventory();
		}
		
		return addedNew;
	}
	
	private void setMaterials(Collection<String> materialNames) {
		wandMaterials = StringUtils.join(materialNames, "|");

		// Set new spells count
		setMaterialCount(materialNames.size());
		String[] spellNames = getSpells();
		hasInventory = (spellNames.length + materialNames.size() > 1);
		
		saveState();
	}
	
	public String[] getMaterials() {
		return StringUtils.split(wandMaterials, "|");
	}
	
	private boolean addSpells(Collection<String> spellNames) {
		String[] spells = getSpells();
		Set<String> spellMap = new TreeSet<String>();
		boolean addedNew = false;
		for (String spell : spells) {
			spellMap.add(spell);
		}
		for (String spellName : spellNames) {
			if (activeSpell == null || activeSpell.length() == 0) {
				activeSpell = spellName;
			}
			if (!spellMap.contains(spellName)) {
				addedNew = true;
			}
			spellMap.add(spellName);
			addedNew = true;
		}
				
		setSpells(spellMap);
		
		return addedNew;
	}
	
	public String[] getSpells() {
		return StringUtils.split(wandSpells, "|");
	}

	public void removeSpell(String spellName) {
		String[] spells = getSpells();
		List<String> spellMap = new LinkedList<String>();
		String fallbackActive = "";
		for (int i = 0; i < spells.length; i++) {
			String[] pieces = StringUtils.split(spells[i], "@");
			if (pieces.length > 0 && !pieces[0].equals(spellName)) {
				fallbackActive = spells[i];
				spellMap.add(spells[i]);
			}
		}
		setSpells(spellMap);
		
		if (spellName.equalsIgnoreCase(activeSpell)) {
			activeSpell = fallbackActive;
			updateName();
			if (isInventoryOpen()) {
				updateInventory();
			}
		}
	}
	
	public boolean addSpell(String spellName, boolean makeDefault) {
		if (makeDefault) {
			activeSpell = spellName;
		}
		List<String> names = new ArrayList<String>();
		names.add(spellName);
		boolean addedNew = addSpells(names);
		updateName();
		if (isInventoryOpen()) {
			updateInventory();
		}
		
		return addedNew;
	}
	
	public boolean addSpell(String spellName) {
		return addSpell(spellName, true);
	}
	
	private void setSpells(Collection<String> spellNames) {
		wandSpells = StringUtils.join(spellNames, "|");

		// Set new spells count
		setSpellCount(spellNames.size());
		String[] materials = getMaterials();
		hasInventory = (spellNames.size() + materials.length > 1);

		saveState();
	}
	
	private void setSpellCount(int spellCount) {
		updateLore(spellCount, getMaterials().length);
	}
	
	private void setMaterialCount(int materialCount) {
		updateLore(getSpells().length, materialCount);
	}

	private String getActiveWandName(Spell spell, String materialName) {
		// Build wand name
		String name = wandName;
		
		// Add active spell to description
		if (hasInventory) {		
			if (spell != null) {
				if (materialName != null) {
					materialName = materialName.replace('_', ' ');
					name = ChatColor.GOLD + spell.getName() + ChatColor.GRAY + " " + materialName + ChatColor.WHITE + " (" + wandName + ")";
				} else {
					name = ChatColor.GOLD + spell.getName() + ChatColor.WHITE + " (" + wandName + ")";
				}
			}
		}
		int remaining = getRemainingUses();
		if (remaining > 0) {
			name = name + " : " + ChatColor.RED + "" + remaining + " Uses ";
		}
		return name;
	}
	
	@SuppressWarnings("deprecation")
	private String getActiveWandName(Spell spell) {
		String[] pieces = StringUtils.split(activeMaterial, ":");
		String materialName = null;
		
		if (spell != null && spell.usesMaterial() && !spell.hasMaterialOverride() && pieces.length > 0 && pieces[0].length() > 0) {
			int materialId = Integer.parseInt(pieces[0]);
			if (materialId == 0) {
				materialName = "erase";
			} else if (materialId == -1) {
				materialName = "copy";
			} else {
				Material material = Material.getMaterial(materialId);
				materialName = material.name().toLowerCase();;
			}
		}
		return getActiveWandName(spell, materialName);
	}
	
	private String getMaterialName(Material material) {
		String materialName = null;
		
		if (material == EraseMaterial) {
			materialName = "erase";
		} else if (material == CopyMaterial) {
			materialName = "copy";
		} else {
			materialName = material.name().toLowerCase();
		}
		return materialName;
	}
	
	private String getActiveWandName(Material material) {
		Spell spell = spells.getSpell(activeSpell);
		String materialName = null;
		
		if (spell != null && spell.usesMaterial() && !spell.hasMaterialOverride() && material != null) {
			materialName = getMaterialName(material);
		}
		return getActiveWandName(spell, materialName);
	}
	
	private String getActiveWandName() {
		Spell spell = spells.getSpell(activeSpell);
		return getActiveWandName(spell);
	}
	
	public void updateName(boolean isActive) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(isActive ? getActiveWandName() : wandName);
		item.setItemMeta(meta);
		
		// Reset Enchantment glow
		InventoryUtils.addGlow(item);

		// The all-important last step of restoring the meta state, something
		// the Anvil will blow away.
		saveState();
	}
	
	private void updateName() {
		updateName(true);
	}
	
	private String getLevelString(String prefix, float amount) {
		String suffix = "I";

		if (amount >= 1) {
			suffix = "X";
		} else if (amount > 0.8) {
			suffix = "V";
		} else if (amount > 0.6) {
			suffix = "IV";
		} else if (amount > 0.4) {
			suffix = "III";
		} else if (amount > 0.2) {
			suffix = "II";
		}
		return prefix + " " + suffix;
	}
	
	private void updateLore() {
		updateLore(getSpells().length, getMaterials().length);
	}

	private void updateLore(int spellCount, int materialCount) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		
		Spell spell = spells.getSpell(activeSpell);
		if (spell != null && spellCount == 1 && materialCount <= 1) {
			addSpellLore(spell, lore);
		} else {
			lore.add("Knows " + spellCount +" Spells");
			if (materialCount > 0) {
				lore.add("Has " + materialCount +" Materials");
			}
		}
		int remaining = getRemainingUses();
		if (remaining > 0) {
			lore.add(ChatColor.RED + "" + remaining + " Uses Remaining");
		}
		if (costReduction > 0) lore.add(ChatColor.GOLD + getLevelString("Cost Reduction", costReduction));
		if (cooldownReduction > 0) lore.add(ChatColor.GOLD + getLevelString("Cooldown Reduction", cooldownReduction));
		if (walkSpeedIncrease > 0) lore.add(ChatColor.GOLD + getLevelString("Haste", walkSpeedIncrease / WandLevel.maxWalkSpeedIncrease));
		if (xpRegeneration > 0) lore.add(ChatColor.GOLD + getLevelString("Mana Regeneration", xpRegeneration / WandLevel.maxXpRegeneration));
		if (power > 0) lore.add(ChatColor.GOLD + getLevelString("Power", power));
		if (damageReduction > 0) lore.add(ChatColor.GOLD + getLevelString("Protection", damageReduction));
		if (damageReduction < 1) {
			if (damageReductionPhysical > 0) lore.add(ChatColor.GOLD + getLevelString("Physical Protection", damageReductionPhysical));
			if (damageReductionProjectiles > 0) lore.add(ChatColor.GOLD + getLevelString("Projectile Protection", damageReductionProjectiles));
			if (damageReductionFalling > 0) lore.add(ChatColor.GOLD + getLevelString("Fall Protection", damageReductionFalling));
			if (damageReductionFire > 0) lore.add(ChatColor.GOLD + getLevelString("Fire Protection", damageReductionFire));
			if (damageReductionExplosions > 0) lore.add(ChatColor.GOLD + getLevelString("Blast Protection", damageReductionExplosions));
		}
		if (healthRegeneration > 0) lore.add(ChatColor.GOLD + getLevelString("Health Regeneration", healthRegeneration / WandLevel.maxRegeneration));
		if (hungerRegeneration > 0) lore.add(ChatColor.GOLD + getLevelString("Anti-Hunger", hungerRegeneration / WandLevel.maxRegeneration));
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		InventoryUtils.addGlow(item);

		// Reset spell list and wand config
		saveState();
	}
	
	public int getRemainingUses() {
		int remaining = 0;
		if (uses > 0) {
			short durability = item.getDurability();
			remaining = Math.max(0, uses - durability);
		}
		return remaining;
	}
	
	public void makeEnchantable(boolean enchantable) {
		item.setType(enchantable ? EnchantableWandMaterial : WandMaterial);
		updateName();
	}
	
	public static boolean hasActiveWand(Player player) {
		ItemStack activeItem =  player.getInventory().getItemInHand();
		return isWand(activeItem);
	}
	
	public static Wand getActiveWand(Spells spells, Player player) {
		ItemStack activeItem =  player.getInventory().getItemInHand();
		if (isWand(activeItem)) {
			return new Wand(spells, activeItem);
		}
		
		return null;
	}

	public static boolean isWand(ItemStack item) {
		// Special-case here for porting old wands. Could be removed eventually.
		return item != null && (item.getType() == WandMaterial || item.getType() == EnchantableWandMaterial) && (InventoryUtils.hasMeta(item, "wand") || InventoryUtils.hasMeta(item, "magic_wand"));
	}

	public static boolean isSpell(ItemStack item) {
		return item != null && item.getType() != WandMaterial && InventoryUtils.hasMeta(item, "spell");
	}

	protected void updateInventory() {
		if (activePlayer == null) return;
		updateInventory(activePlayer.getPlayer().getInventory().getHeldItemSlot());
	}
	
	public void updateInventoryNames(boolean activeNames) {
		if (activePlayer == null || !isInventoryOpen()) return;
		
		ItemStack[] contents = activePlayer.getPlayer().getInventory().getContents();
		for (ItemStack item : contents) {
			if (item == null || item.getType() == Material.AIR || isWand(item)) continue;
			updateInventoryName(item, activeNames);
		}
	}

	protected void updateInventoryName(ItemStack item, boolean activeName) {
		if (isSpell(item)) {
			Spell spell = activePlayer.getSpell(item.getType());
			if (spell != null) {
				updateSpellName(item, spell, activeName);
			}
			
		} else {
			updateMaterialName(item, activeName);
		}
	}
	
	protected void updateSpellName(ItemStack itemStack, Spell spell, boolean activeName) {
		ItemMeta meta = itemStack.getItemMeta();
		if (activeName) {
			meta.setDisplayName(getActiveWandName(spell));
		} else {
			meta.setDisplayName(ChatColor.GOLD + spell.getName());
		}
		List<String> lore = new ArrayList<String>();
		addSpellLore(spell, lore);
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		InventoryUtils.addGlow(itemStack);
		Object spellNode = InventoryUtils.createNode(itemStack, "spell");
		InventoryUtils.setMeta(spellNode, "key", spell.getKey());
	}
	
	protected void updateMaterialName(ItemStack itemStack, boolean activeName) {
		ItemMeta meta = itemStack.getItemMeta();
		if (activeName) {
			meta.setDisplayName(getActiveWandName(itemStack.getType()));
		} else {
			meta.setDisplayName(getMaterialName(itemStack.getType()));
		}
		itemStack.setItemMeta(meta);
	}
	
	@SuppressWarnings("deprecation")
	private void updateInventory(int itemSlot) {
		if (activePlayer == null) return;
		
		Player player = activePlayer.getPlayer();
		Inventory inventory = player.getInventory();
		inventory.clear();
		inventory.setItem(itemSlot, item);
		String[] spells = StringUtils.split(wandSpells, "|");

		// Gather up all spells and materials
		// Spells saved in a specific slot are put directly there.
		Queue<ItemStack> unpositionedSpells = new LinkedList<ItemStack>();
		for (int i = 0; i < spells.length; i++) {
			String[] parts = StringUtils.split(spells[i], "@");
			String spellName = parts[0];
			Spell spell = activePlayer.getSpell(spellName);
			if (spell == null) continue;
			
			ItemStack itemStack = new ItemStack(spell.getMaterial(), 1);
			itemStack = InventoryUtils.getCopy(itemStack);
			updateSpellName(itemStack, spell, true);
			
			int slot = parts.length > 1 ? Integer.parseInt(parts[1]) : itemSlot;
			if (parts.length > 1 && slot != itemSlot) {
				inventory.setItem(slot, itemStack);
			} else {
				unpositionedSpells.add(itemStack);
			}
		}
		
		String[] materials = StringUtils.split(wandMaterials, "|");

		// Materials saved in a specific spot are added to a hashmap for later processing
		Queue<ItemStack> unpositionedMaterials = new LinkedList<ItemStack>();
		HashMap<Integer, ItemStack> positioned = new HashMap<Integer, ItemStack>();
		ItemStack eraseStack = null;
		ItemStack copyStack = null;
		for (int i = 0; i < materials.length; i++) {
			String[] parts = StringUtils.split(materials[i], "@");
			String[] nameParts = StringUtils.split(parts[0], ":");
			int typeId = Integer.parseInt(nameParts[0]);
			if (typeId == 0) {
				typeId = EraseMaterial.getId();
			} else if (typeId == -1) {
				typeId = CopyMaterial.getId();
			}
			int dataId = nameParts.length > 1 ? Integer.parseInt(nameParts[1]) : 0;
			
			ItemStack itemStack = new ItemStack(typeId, 1, (short)0, (byte)dataId);		
			itemStack = InventoryUtils.getCopy(itemStack);
			ItemMeta meta = itemStack.getItemMeta();
			if (typeId == EraseMaterial.getId()) {
				List<String> lore = new ArrayList<String>();
				lore.add("Fills with Air");
				meta.setLore(lore);
			} else if (typeId == CopyMaterial.getId()) {
				List<String> lore = new ArrayList<String>();
				lore.add("Fills with the target material");
				meta.setLore(lore);
			} else {
				List<String> lore = new ArrayList<String>();
				lore.add("Magic building material");
				meta.setLore(lore);
			}
			meta.setDisplayName(getActiveWandName(Material.getMaterial(typeId)));
			itemStack.setItemMeta(meta);
			
			int slot = parts.length > 1 ? Integer.parseInt(parts[1]) : itemSlot;
			ItemStack existing = inventory.getItem(slot);
			if (parts.length > 1 && slot != itemSlot && (existing == null || existing.getType() == Material.AIR)) {
				positioned.put((Integer)slot, itemStack);
			} else {
				if (itemStack.getType() == EraseMaterial) {
					eraseStack = itemStack;
				} else if (itemStack.getType() == CopyMaterial) {
					copyStack = itemStack;
				} else {
					unpositionedMaterials.add(itemStack);
				}
			}
		}
		
		// This is here to try and leave some room for materials, if present
		// in a newly-created wand.
		
		if (unpositionedMaterials.size() > 0) {
			int materialSpaces = Math.min(unpositionedMaterials.size(), 3);
			int remainingSpaces = 8 - materialSpaces;
			while (unpositionedSpells.size() > 0 && remainingSpaces > 0) {
				inventory.addItem(unpositionedSpells.remove());
				remainingSpaces--;
			}
		}
		
		
		// Put the new materials first, then the mapped materials
		// TODO: Investigate if all of this is necessary now with the new inventory system.
		if (eraseStack != null) {
			addMaterialToInventory(inventory, eraseStack);
		}
		if (copyStack != null) {
			addMaterialToInventory(inventory, copyStack);
		}
		
		// Add the rest of the unpositioned spells
		for (ItemStack stack : unpositionedSpells) {
			inventory.addItem(stack);
		}
		
		// Add mapped materials, but if there is already something there just
		// toss it in the inventory.
		for (Entry<Integer, ItemStack> entry : positioned.entrySet()) {
			int slot = entry.getKey();
			ItemStack itemStack = entry.getValue();
			ItemStack existing = inventory.getItem(slot);
			if (slot != itemSlot && (existing == null || existing.getType() == Material.AIR)) {
				inventory.setItem(slot, itemStack);
			} else {
				addMaterialToInventory(inventory, itemStack);
			}
		}
		
		for (ItemStack stack : unpositionedMaterials) {
			addMaterialToInventory(inventory, stack);
		}

		updateName();
		player.updateInventory();
	}
	
	protected void addSpellLore(Spell spell, List<String> lore) {
		lore.add(spell.getDescription());
		List<CastingCost> costs = spell.getCosts();
		if (costs != null) {
			for (CastingCost cost : costs) {
				if (cost.hasCosts(this)) {
					lore.add(ChatColor.YELLOW + "Costs " + cost.getFullDescription(this));
				}
			}
		}
	}
	
	private static void addMaterialToInventory(Inventory inventory, ItemStack stack) {
		// First try to put it in the main bar, starting from the right.
		for (int i = 8; i >= 0; i--) {
			ItemStack existing = inventory.getItem(i);
			if (existing == null || existing.getType() == Material.AIR) {
				inventory.setItem(i, stack);
				return;
			}
		}
		
		inventory.addItem(stack);
	}
	
	@SuppressWarnings("deprecation")
	public void saveInventory() {
		if (activePlayer == null) return;
		PlayerInventory inventory = activePlayer.getPlayer().getInventory();
		
		// Rebuild spell inventory, save in wand.
		// Never add/remove a spell or material from the wand, just re-arrange them.
		String[] currentSpells = getSpells();
		String[] currentMaterials = getMaterials();
		
		// Map current Wand inventory
		HashMap<String, Integer> spellMap = new HashMap<String, Integer>();
		HashMap<String, Integer> materialMap = new HashMap<String, Integer>();
		
		for (String spell : currentSpells) {
			String[] pieces = StringUtils.split(spell, "@");
			Integer position = pieces.length > 1 ? Integer.parseInt(pieces[1]) : null;
			spellMap.put(pieces[0], position);
		}
		for (String material : currentMaterials) {
			String[] pieces = StringUtils.split(material, "@");
			Integer position = pieces.length > 1 ? Integer.parseInt(pieces[1]) : null;
			materialMap.put(pieces[0], position);
		}
		
		// re-arrange based on player inventory contents
		ItemStack[] items = inventory.getContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null || isWand(items[i])) continue;
			Material material = items[i].getType();
			if (isSpell(items[i])) {
				Spell spell = activePlayer.getSpell(material);
				if (spell != null && spellMap.containsKey(spell.getKey())) {
					spellMap.put(spell.getKey(), i);
				}
			} else {
				Set<Material> buildingMaterials = activePlayer.getMaster().getBuildingMaterials();
				if (material != Material.AIR && (buildingMaterials.contains(material) || material == EraseMaterial)) {
					String materialKey = material.getId() + ":" + items[i].getData().getData();
					if (material == EraseMaterial) {
						materialKey = "0:0"; 
					} else if (material == CopyMaterial) {
						materialKey = "-1:0"; 
					}
					if (materialMap.containsKey(materialKey)) {
						materialMap.put(materialKey, i);
					}
				}
			}
		}
		
		// Pack up into lists and set to wand
		List<String> spellNames = new ArrayList<String>();
		List<String> materialNames = new ArrayList<String>();
		
		for (Entry<String, Integer> spellEntry : spellMap.entrySet()) {
			Integer position = spellEntry.getValue();
			String spellName = spellEntry.getKey();
			if (position != null) {
				spellName += "@" + position;
			}
			spellNames.add(spellName);
		}
		for (Entry<String, Integer> materialEntry : materialMap.entrySet()) {
			Integer position = materialEntry.getValue();
			String materialName = materialEntry.getKey();
			if (position != null) {
				materialName += "@" + position;
			}
			materialNames.add(materialName);
		}
		
		setSpells(spellNames);
		setMaterials(materialNames);
		saveState();
	}

	public static boolean isActive(Player player) {
		ItemStack activeItem = player.getInventory().getItemInHand();
		return isWand(activeItem);
	}
	
	protected void randomize(int level, boolean additive) {
		if (!wandTemplates.containsKey("random")) return;	
		ConfigurationNode randomTemplate = wandTemplates.get("random");
		if (!additive && randomTemplate.containsKey("name")) {
			wandName = randomTemplate.getString("name");
		}
		WandLevel.randomizeWand(this, additive, level);
	}
	
	public static Wand createWand(Spells spells, String templateName) {
		Wand wand = new Wand(spells);
		List<String> defaultSpells = new ArrayList<String>();
		String wandName = defaultWandName;

		// See if there is a template with this key
		if (templateName != null && templateName.length() > 0) {
			if ((templateName.equals("random") || templateName.startsWith("random(")) && wandTemplates.containsKey("random")) {
				int level = 1;
				if (!templateName.equals("random")) {
					String randomLevel = templateName.substring(templateName.indexOf('(') + 1, templateName.length() - 1);
					level = Integer.parseInt(randomLevel);
				}
				wand.randomize(level, false);
				return wand;
			}
			
			if (!wandTemplates.containsKey(templateName)) {
				return null;
			}
			ConfigurationNode wandConfig = wandTemplates.get(templateName);
			wandName = wandConfig.getString("name", wandName);
			List<Object> spellList = wandConfig.getList("spells");
			if (spellList != null) {
				for (Object spellName : spellList) {
					defaultSpells.add((String)spellName);
				}
			}
			List<Object> materialList = wandConfig.getList("materials");
			if (materialList != null) {
				for (Object materialNameAndData : materialList) {
					String[] materialParts = StringUtils.split((String)materialNameAndData, ':');
					String materialName = materialParts[0];
					byte data = 0;
					if (materialParts.length > 1) {
						data = Byte.parseByte(materialParts[1]);
					}
					
					if (materialName.equals("erase")) {
						wand.addMaterial(EraseMaterial, (byte)0, false);
					} else if (materialName.equals("copy") || materialName.equals("clone")) {
						wand.addMaterial(CopyMaterial, (byte)0, false);
					} else {
						wand.addMaterial(ConfigurationNode.toMaterial(materialName), data, false);
					}
				}
			}
			
			wand.configureProperties(wandConfig);
		}

		wand.addSpells(defaultSpells);
		wand.setName(wandName);
		
		return wand;
	}
	
	public void configureProperties(ConfigurationNode wandConfig) {
		costReduction = (float)wandConfig.getDouble("cost_reduction", costReduction);
		power = (float)wandConfig.getDouble("power", power);
		damageReduction = (float)wandConfig.getDouble("damage_reduction", damageReduction);
		damageReductionPhysical = (float)wandConfig.getDouble("damage_reduction_physical", damageReductionPhysical);
		damageReductionProjectiles = (float)wandConfig.getDouble("damage_reduction_projectiles", damageReductionPhysical);
		damageReductionFalling = (float)wandConfig.getDouble("damage_reduction_falling", damageReductionFalling);
		damageReductionFire = (float)wandConfig.getDouble("damage_reduction_fire", damageReductionFire);
		damageReductionExplosions = (float)wandConfig.getDouble("damage_reduction_explosions", damageReductionExplosions);
		xpRegeneration = wandConfig.getInt("xp_regeneration", xpRegeneration);
		xpMax = wandConfig.getInt("xp_max", xpMax);
		xp = wandConfig.getInt("xp", xp);
		healthRegeneration = wandConfig.getInt("health_regeneration", healthRegeneration);
		hungerRegeneration = wandConfig.getInt("hunger_regeneration", hungerRegeneration);
		uses = wandConfig.getInt("uses", uses);
	
		// Make sure to adjust the player's walk speed if it changes and this wand is active.
		float oldWalkSpeedIncrease = walkSpeedIncrease;
		walkSpeedIncrease = (float)wandConfig.getDouble("haste", walkSpeedIncrease);
		if (activePlayer != null && walkSpeedIncrease != oldWalkSpeedIncrease) {
			Player player = activePlayer.getPlayer();
			player.setWalkSpeed(player.getWalkSpeed() + walkSpeedIncrease - oldWalkSpeedIncrease);
		}
		
		saveState();
		updateName();
		updateLore();
	}
	
	public static void reset(Plugin plugin) {
		File dataFolder = plugin.getDataFolder();
		File propertiesFile = new File(dataFolder, propertiesFileName);
		propertiesFile.delete();
	}
	
	public static void load(Plugin plugin) {
		File dataFolder = plugin.getDataFolder();
		File propertiesFile = new File(dataFolder, propertiesFileName);
		if (!propertiesFile.exists())
		{
			File oldDefaults = new File(dataFolder, propertiesFileNameDefaults);
			oldDefaults.delete();
			plugin.saveResource(propertiesFileNameDefaults, false);
			loadProperties(plugin.getResource(propertiesFileNameDefaults));
		} else {
			loadProperties(propertiesFile);
		}
	}
	
	private static void loadProperties(File propertiesFile)
	{
		loadProperties(new Configuration(propertiesFile));
	}
	
	private static void loadProperties(InputStream properties)
	{
		loadProperties(new Configuration(properties));
	}
	
	private static void loadProperties(Configuration properties)
	{
		properties.load();
		wandTemplates.clear();

		ConfigurationNode wandList = properties.getNode("wands");
		if (wandList == null) return;

		List<String> wandKeys = wandList.getKeys();
		for (String key : wandKeys)
		{
			ConfigurationNode wandNode = wandList.getNode(key);
			wandNode.setProperty("key", key);
			wandTemplates.put(key,  wandNode);
			if (key.equals("random")) {
				WandLevel.mapLevels(wandNode);
			}
		}
	}
	
	public static Collection<ConfigurationNode> getWandTemplates() {
		return wandTemplates.values();
	}

	@SuppressWarnings("deprecation")
	private void updateActiveMaterial() {
		if (activePlayer == null) return;
		
		if (activeMaterial == null) {
			activePlayer.clearBuildingMaterial();
		} else {
			String[] pieces = StringUtils.split(activeMaterial, ":");
			if (pieces.length > 0) {
				byte data = 0;
				if (pieces.length > 1) {
					data = Byte.parseByte(pieces[1]);
				}
				int materialId = Integer.parseInt(pieces[0]);
				Material material = null;
				if (materialId == 0) {
					material = EraseMaterial;
				} else if (materialId == -1) {
					material = CopyMaterial;
				} else {
					material = Material.getMaterial(materialId);
				}
				activePlayer.setBuildingMaterial(material, data);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void openInventory(int itemSlot) {
		if (activePlayer == null) return;
		if (activePlayer.storeInventory(itemSlot, item)) {
			updateInventory(itemSlot);
			activePlayer.getPlayer().updateInventory();
		}
	}
	
	public void openInventory() {
		if (activePlayer == null) return;
		openInventory(activePlayer.getPlayer().getInventory().getHeldItemSlot());
	}
	
	@SuppressWarnings("deprecation")
	private void closeInventory(int itemSlot) {
		saveInventory();
		if (activePlayer != null) {
			activePlayer.restoreInventory(itemSlot, item);
			activePlayer.getPlayer().updateInventory();
		}
	}
	
	public void closeInventory() {
		if (activePlayer == null) return;
		closeInventory(activePlayer.getPlayer().getInventory().getHeldItemSlot());
	}
	
	public void activate(PlayerSpells playerSpells) {
		activePlayer = playerSpells;
		Player player = activePlayer.getPlayer();
		if (walkSpeedIncrease > 0) {
			try {
				player.setWalkSpeed(defaultWalkSpeed + walkSpeedIncrease);
			} catch(Exception ex2) {
				try {
					player.setWalkSpeed(defaultWalkSpeed);
				}  catch(Exception ex) {
					
				}
			}
		}
		activePlayer.setActiveWand(this);
		if (xpRegeneration > 0) {
			storedXpLevel = player.getLevel();
			storedXpProgress = player.getExp();
			storedXp = 0;
			player.setLevel(0);
			player.setExp(0);
			player.giveExp(xp);
		}
		updateActiveMaterial();
		updateName();
	}
	
	public boolean isInventoryOpen() {
		return activePlayer != null && activePlayer.hasStoredInventory();
	}
	
	public void deactivate() {
		if (activePlayer == null) return;
		
		// This is a tying wands together with other spells, potentially
		// But with the way the mana system works, this seems like the safest route.
		activePlayer.deactivateAllSpells();
		
		deactivate(activePlayer.getPlayer().getInventory().getHeldItemSlot());
	}
	
	public void deactivate(int itemSlot) {
		if (activePlayer == null) return;
		if (isInventoryOpen()) {
			closeInventory(itemSlot);
		}
		if (xpRegeneration > 0) {
			xp = activePlayer.getExperience();
			activePlayer.player.setExp(storedXpProgress);
			activePlayer.player.setLevel(storedXpLevel);
			activePlayer.player.giveExp(storedXp);
			storedXp = 0;
			storedXpProgress = 0;
			storedXpLevel = 0;
		}
		if (walkSpeedIncrease > 0) {
			try {
				activePlayer.getPlayer().setWalkSpeed(defaultWalkSpeed);
			}  catch(Exception ex) {
				
			}
		}
		activePlayer.setActiveWand(null);
		activePlayer = null;
		saveState();
	}
	
	public boolean cast() {
		if (activePlayer == null) return false;
		Spell spell = activePlayer.getSpell(activeSpell);
		if (spell != null) {
			if (spell.cast()) {
				use();
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	protected void use() {
		if (activePlayer == null) return;
		if (uses > 0) {
			short durability = item.getDurability();
			if (durability >= uses - 1) {
				Player player = activePlayer.getPlayer();
				deactivate();
				PlayerInventory playerInventory = player.getInventory();
				playerInventory.setItemInHand(new ItemStack(Material.AIR, 1));
				player.updateInventory();
			} else {
				item.setDurability((short)(durability + 1));
				updateName();
				updateLore(getSpells().length, getMaterials().length);
			}
		}
	}
	
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		if (activePlayer == null) return;
		
		if (xpRegeneration > 0) {
			storedXp += event.getAmount();
			event.setAmount(0);
		}
	}
	
	public void processRegeneration() {
		if (activePlayer == null) return;
		
		Player player = activePlayer.getPlayer();
		if (xpRegeneration > 0) {
			int playerExperience = activePlayer.getExperience();
			if (playerExperience < xpMax) {
				player.giveExp(Math.min(xpRegeneration, xpMax - playerExperience));
			}
		}
		double maxHealth = player.getMaxHealth();
		if (healthRegeneration > 0 && player.getHealth() < maxHealth) {
			player.setHealth(Math.min(maxHealth, player.getHealth() + healthRegeneration));
		}
		double maxFoodLevel = 20;
		if (hungerRegeneration > 0 && player.getFoodLevel() < maxFoodLevel) {
			player.setExhaustion(0);
			player.setFoodLevel(Math.min(20, player.getFoodLevel() + hungerRegeneration));
		}
		if (damageReductionFire > 0 && player.getFireTicks() > 0) {
			player.setFireTicks(0);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof Wand)) return false;
		
		Wand otherWand =  ((Wand)other);
		if (this.id == null || otherWand.id == null) return false;
		
		return otherWand.id.equals(this.id);
	}
	
	public Spells getMaster() {
		return spells;
	}
}
