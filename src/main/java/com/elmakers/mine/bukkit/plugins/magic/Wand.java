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

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.Configuration;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Wand {
	private ItemStack item;
	
	// Cached state
	private String wandSettings;
	private String wandSpells;
	private String wandMaterials;
	
	private String activeSpell;
	private String activeMaterial;
	private String wandName;
	
	private float costReduction = 0;
	private float damageReduction = 0;
	private float damageReductionPhysical = 0;
	private float damageReductionProjectiles = 0;
	private float damageReductionFalling = 0;
	private float damageReductionFire = 0;
	private float damageReductionExplosions = 0;
	private boolean hasInventory = true;
	private int uses = 0;
	
	private int xpRegeneration = 0;
	private int xpMax = 0;
	private int healthRegeneration = 0;
	private int hungerRegeneration = 0;
	
	private static DecimalFormat floatFormat = new DecimalFormat("#.###");
	
	private static Material WandMaterial = Material.STICK;
	public static Material EraseMaterial = Material.SULPHUR;
	
	// Wand configurations
	protected static Map<String, ConfigurationNode> wandTemplates = new HashMap<String, ConfigurationNode>();
	private static final String propertiesFileName = "wands.yml";
	private static final String propertiesFileNameDefaults = "wands.defaults.yml";
	private static final String defaultWandName = "Wand";
	
	public Wand() {
		item = new ItemStack(WandMaterial);
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		item = InventoryUtils.getCopy(item);
		ItemMeta itemMeta = item.getItemMeta();
		item.setItemMeta(itemMeta);

		InventoryUtils.addGlow(item);
		wandSettings = " ";
		wandSpells = "";
		wandMaterials = "";
		activeSpell = "";
		activeMaterial = "";
		wandName = defaultWandName;
		saveState();
	}
	
	public Wand(ItemStack item) {
		this.item = item;
		loadState();
	}

	public void setActiveSpell(PlayerSpells playerSpells, String activeSpell) {
		this.activeSpell = activeSpell;
		updateName(playerSpells);
		saveState();
	}

	public void setActiveMaterial(PlayerSpells playerSpells, Material material, byte data) {
		this.activeMaterial = material.name().toLowerCase() + ":" + data;
		updateName(playerSpells);
		if (hasActiveWand(playerSpells.getPlayer())) {
			updateActiveMaterial(playerSpells);
		}
		saveState();
	}
	
	public int getXpRegeneration() {
		return xpRegeneration;
	}

	public void setXPRegeneration(int xpRegeneration) {
		this.xpRegeneration = xpRegeneration;
		updateWandSettings();
	}

	public int getXPMax() {
		return xpMax;
	}

	public void setXPMax(int xpMax) {
		this.xpMax = xpMax;
		updateWandSettings();
	}

	public int getHealthRegeneration() {
		return healthRegeneration;
	}

	public void setHealthRegeneration(int healthRegeneration) {
		this.healthRegeneration = healthRegeneration;
		updateWandSettings();
	}

	public int getHungerRegeneration() {
		return hungerRegeneration;
	}

	public void setHungerRegeneration(int hungerRegeneration) {
		this.hungerRegeneration = hungerRegeneration;
		updateWandSettings();
	}

	public float getCostReduction() {
		return costReduction;
	}
	
	public boolean getHasInventory() {
		return hasInventory;
	}
	
	public void setHasInventory(boolean hasInventory) {
		this.hasInventory = hasInventory;
		updateWandSettings();
	}

	public void setCostReduction(float costReduction) {
		this.costReduction = costReduction;
		updateWandSettings();
	}

	public float getDamageReduction() {
		return damageReduction;
	}

	public void setDamageReduction(float damageReduction) {
		this.damageReduction = damageReduction;
		updateWandSettings();
	}

	public float getDamageReductionPhysical() {
		return damageReductionPhysical;
	}

	public void setDamageReductionPhysical(float damageReductionPhysical) {
		this.damageReductionPhysical = damageReductionPhysical;
		updateWandSettings();
	}

	public float getDamageReductionProjectiles() {
		return damageReductionProjectiles;
	}

	public void setDamageReductionProjectiles(float damageReductionProjectiles) {
		this.damageReductionProjectiles = damageReductionProjectiles;
		updateWandSettings();
	}

	public float getDamageReductionFalling() {
		return damageReductionFalling;
	}

	public void setDamageReductionFalling(float damageReductionFalling) {
		this.damageReductionFalling = damageReductionFalling;
		updateWandSettings();
	}

	public float getDamageReductionFire() {
		return damageReductionFire;
	}

	public void setDamageReductionFire(float damageReductionFire) {
		this.damageReductionFire = damageReductionFire;
		updateWandSettings();
	}

	public float getDamageReductionExplosions() {
		return damageReductionExplosions;
	}

	public void setDamageReductionExplosions(float damageReductionExplosions) {
		this.damageReductionExplosions = damageReductionExplosions;
		updateWandSettings();
	}

	public int getUses() {
		return uses;
	}

	public void setUses(int uses) {
		this.uses = uses;
		updateWandSettings();
	}
	
	public String getName() {
		return wandName;
	}
	
	protected void setName(String name) {
		wandName = name;
	}
	
	public ItemStack getItem() {
		return item;
	}

	protected void saveState() {
		updateWandSettings();
		InventoryUtils.setMeta(item, "magic_wand", wandSettings);
		InventoryUtils.setMeta(item, "magic_materials", wandMaterials);
		InventoryUtils.setMeta(item, "magic_spells", wandSpells);
		InventoryUtils.setMeta(item, "magic_active_spell", activeSpell);
		InventoryUtils.setMeta(item, "magic_active_material", activeMaterial);
		InventoryUtils.setMeta(item, "magic_wand_name", wandName);
	}
	
	protected void updateWandSettings() {
		wandSettings = "cr=" + floatFormat.format(costReduction) + 
		 "&dr=" + floatFormat.format(damageReduction) +
		 "&drph=" + floatFormat.format(damageReductionPhysical) +
		 "&drpr=" + floatFormat.format(damageReductionProjectiles) +
		 "&drfa=" + floatFormat.format(damageReductionFalling) +
		 "&drfi=" + floatFormat.format(damageReductionFire) +
		 "&drex=" + floatFormat.format(damageReductionExplosions) +
		 "&drex=" + floatFormat.format(damageReductionExplosions) +
		 "&xpre=" + xpRegeneration +
		 "&xpmax=" + xpMax +
		 "&hereg=" + healthRegeneration +
		 "&hureg=" + hungerRegeneration +
		 "&uses=" + uses +
		 "&hasi=" + (hasInventory ? 1 : 0);
	}
	
	protected void loadState() {
		wandSettings = InventoryUtils.getMeta(item, "magic_wand");
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
	
	@SuppressWarnings("deprecation")
	public void removeMaterial(Material material, byte data) {
		Integer id = material.getId();
		String materialString = id.toString();
		materialString += ":" + data;

		String[] materials = getMaterials();
		List<String> materialMap = new LinkedList<String>();
		for (int i = 0; i < materials.length; i++) {	
			String[] pieces = StringUtils.split(materials[i], "@");
			if (!pieces[0].equals(materialString)) {
				materialMap.add(materials[i]);
			}
		}
		setMaterials(materialMap);
		
		if (materialString.equalsIgnoreCase(activeMaterial)) {
			activeMaterial = "";
		}
	}
	
	public void addMaterial(Material material, int data) {
		addMaterial(material, (byte)data);
	}
	
	public void addMaterial(Material material) {
		addMaterial(material, 0);
	}
	
	@SuppressWarnings("deprecation")
	public void addMaterial(Material material, byte data) {
		Integer id = material.getId();
		String materialString = id.toString();
		materialString += ":" + data;

		String[] materials = getMaterials();
		List<String> materialMap = new LinkedList<String>();
		for (int i = 0; i < materials.length; i++) {	
			String[] pieces = StringUtils.split(materials[i], "@");
			if (!pieces[0].equals(materialString)) {
				materialMap.add(materials[i]);
			}
		}
		if (activeMaterial.length() == 0) {
			activeMaterial = materialString;
		}
		materialMap.add(materialString);
		setMaterials(materialMap);
	}
	
	protected void setMaterials(Collection<String> materialNames) {
		wandMaterials = StringUtils.join(materialNames, "|");

		// Set new spells count
		setMaterialCount(materialNames.size());

		saveState();
	}
	
	public String[] getMaterials() {
		return StringUtils.split(wandMaterials, "|");
	}
	
	public void addSpells(Collection<String> spellNames) {
		String[] spells = getSpells();
		List<String> spellMap = new LinkedList<String>();
		for (String spell : spells) {
			spellMap.add(spell);
			String[] pieces = StringUtils.split(spell, "@");
			spellNames.remove(pieces[0]);
		}
		for (String spellName : spellNames) { 	
			if (activeSpell.length() == 0) {
				activeSpell = spellName;
			}
			spellMap.add(spellName);
		}
				
		setSpells(spellMap);
	}
	
	public String[] getSpells() {
		return StringUtils.split(wandSpells, "|");
	}

	public void removeSpell(String spellName) {
		String[] spells = getSpells();
		List<String> spellMap = new LinkedList<String>();
		for (int i = 0; i < spells.length; i++) {
			String[] pieces = StringUtils.split(spells[i], "@");
			if (!pieces[0].equals(spellName)) {
				spellMap.add(spells[i]);
			}
		}
		setSpells(spellMap);
		
		if (spellName.equalsIgnoreCase(activeSpell)) {
			activeSpell = "";
		}
	}
	
	public void addSpell(String spellName) {
		List<String> names = new ArrayList<String>();
		names.add(spellName);
		addSpells(names);
	}
	
	public void setSpells(Collection<String> spellNames) {
		wandSpells = StringUtils.join(spellNames, "|");

		// Set new spells count
		setSpellCount(spellNames.size());

		saveState();
	}
	
	public void setSpellCount(int spellCount) {
		updateLore(spellCount, getMaterials().length);
	}
	
	public void setMaterialCount(int materialCount) {
		updateLore(getSpells().length, materialCount);
	}
	
	public void setName(String name, PlayerSpells playerSpells) {
		setName(name);
		updateName(playerSpells);
	}
	
	protected void updateName(PlayerSpells playerSpells) {
		// Build wand name
		String name = wandName;
		
		// Add active spell to description
		if (hasInventory) {
			Spell spell = playerSpells.getSpell(activeSpell);
			if (spell != null) {
				Material material = Material.getMaterial(activeMaterial);
				if (material != null && spell.usesMaterial()) {
					String materialName = material == Wand.EraseMaterial ? "erase" : material.name().toLowerCase();
					name = ChatColor.GOLD + spell.getName() + ChatColor.GRAY + materialName + ChatColor.WHITE + " (" + wandName + ")";
				} else {
					name = ChatColor.GOLD + spell.getName() + ChatColor.WHITE + " (" + wandName + ")";
				}
			}
		}
		int remaining = getRemainingUses();
		if (remaining > 0) {
			name = name + " : " + ChatColor.RED + "" + remaining + " Uses ";
		}
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		
		// Reset Enchantment glow
		InventoryUtils.addGlow(item);

		// The all-important last step of restoring the meta state, something
		// the Anvil will blow away.
		saveState();
	}
	
	protected String getLevelString(String prefix, float amount) {
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

	protected void updateLore(int spellCount, int materialCount) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add("Knows " + spellCount +" Spells");
		if (materialCount > 0) {
			lore.add("Has " + materialCount +" Materials");
		}
		int remaining = getRemainingUses();
		if (remaining > 0) {
			lore.add(ChatColor.RED + "" + remaining + " Uses Remaining");
		}
		if (costReduction > 0) lore.add(ChatColor.GOLD + getLevelString("Cost Reduction", costReduction));
		if (damageReduction > 0) lore.add(ChatColor.GOLD + getLevelString("Protection", damageReduction));
		if (damageReductionPhysical > 0) lore.add(ChatColor.GOLD + getLevelString("Physical Protection", damageReductionPhysical));
		if (damageReductionProjectiles > 0) lore.add(ChatColor.GOLD + getLevelString("Projectile Protection", damageReductionProjectiles));
		if (damageReductionFalling > 0) lore.add(ChatColor.GOLD + getLevelString("Fall Protection", damageReductionFalling));
		if (damageReductionFire > 0) lore.add(ChatColor.GOLD + getLevelString("Fire Protection", damageReductionFire));
		if (damageReductionExplosions > 0) lore.add(ChatColor.GOLD + getLevelString("Blast Protection", damageReductionExplosions));
		if (xpRegeneration > 0) lore.add(ChatColor.GOLD + getLevelString("XP Protection", xpRegeneration / 100));
		if (healthRegeneration > 0) lore.add(ChatColor.GOLD + "Health Regeneration");
		if (hungerRegeneration > 0) lore.add(ChatColor.GOLD + "No Hunger");
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
	
	public static Wand getActiveWand(Player player) {
		ItemStack activeItem =  player.getInventory().getItemInHand();
		return isWand(activeItem) ? new Wand(activeItem) : null;
	}
	
	public static boolean hasActiveWand(Player player) {
		ItemStack activeItem =  player.getInventory().getItemInHand();
		return isWand(activeItem);
	}

	public static boolean isWand(ItemStack item) {
		return item != null && item.getType() == Material.STICK && InventoryUtils.getMeta(item, "magic_wand", "").length() > 0;
	}

	public static boolean isSpell(ItemStack item) {
		return item != null && item.getType() != Material.STICK && InventoryUtils.getMeta(item, "magic_spell", "").length() > 0;
	}

	@SuppressWarnings("deprecation")
	protected void updateInventory(PlayerSpells playerSpells, int itemSlot) {
		Player player = playerSpells.getPlayer();
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
			Spell spell = playerSpells.getSpell(spellName);
			if (spell == null) continue;
			
			ItemStack itemStack = new ItemStack(spell.getMaterial(), 1);
			itemStack = InventoryUtils.getCopy(itemStack);
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(spell.getName());
			List<String> lore = new ArrayList<String>();
			lore.add(spell.getCategory());
			lore.add(spell.getDescription());
			List<CastingCost> costs = spell.getCosts();
			if (costs != null) {
				for (CastingCost cost : costs) {
					if (cost.hasCosts(playerSpells)) {
						lore.add(ChatColor.YELLOW + "Costs " + cost.getFullDescription(playerSpells));
					}
				}
			}
			meta.setLore(lore);
			itemStack.setItemMeta(meta);
			InventoryUtils.addGlow(itemStack);
			InventoryUtils.setMeta(itemStack, "magic_spell", spells[i]);
			
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
		for (int i = 0; i < materials.length; i++) {
			String[] parts = StringUtils.split(materials[i], "@");
			String[] nameParts = StringUtils.split(parts[0], ":");
			int typeId = Integer.parseInt(nameParts[0]);
			if (typeId == 0) {
				typeId = EraseMaterial.getId();
			}
			int dataId = nameParts.length > 1 ? Integer.parseInt(nameParts[1]) : 0;
			
			ItemStack itemStack = new ItemStack(typeId, 1, (short)0, (byte)dataId);		
			itemStack = InventoryUtils.getCopy(itemStack);
			ItemMeta meta = itemStack.getItemMeta();
			if (typeId == EraseMaterial.getId()) {
				meta.setDisplayName("Erase");
				List<String> lore = new ArrayList<String>();
				lore.add("Fills with Air");
				meta.setLore(lore);
			} else {
				List<String> lore = new ArrayList<String>();
				lore.add("Magic building material");
				meta.setLore(lore);
			}
			itemStack.setItemMeta(meta);
			
			int slot = parts.length > 1 ? Integer.parseInt(parts[1]) : itemSlot;
			ItemStack existing = inventory.getItem(slot);
			if (parts.length > 1 && slot != itemSlot && (existing == null || existing.getType() == Material.AIR)) {
				positioned.put((Integer)slot, itemStack);
			} else {
				if (itemStack.getType() == EraseMaterial) {
					eraseStack = itemStack;
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
		// This is so newly-added materials immediately become active.
		// Mainly for the absorb spell to work nicely.
		if (eraseStack != null) {
			addMaterialToInventory(inventory, eraseStack);
		}
		
		for (ItemStack stack : unpositionedMaterials) {
			addMaterialToInventory(inventory, stack);
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

		updateName(playerSpells);
		player.updateInventory();
	}
	
	protected static void addMaterialToInventory(Inventory inventory, ItemStack stack) {
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
	protected void saveInventory(PlayerSpells playerSpells) {
		PlayerInventory inventory = playerSpells.getPlayer().getInventory();
		
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
			if (items[i] == null) continue;
			Material material = items[i].getType();
			if (isSpell(items[i])) {
				Spell spell = playerSpells.getSpell(material);
				if (spell != null && spellMap.containsKey(spell.getKey())) {
					spellMap.put(spell.getKey(), i);
				}
			} else {
				List<Material> buildingMaterials = playerSpells.getMaster().getBuildingMaterials();
				if (material != Material.AIR && (buildingMaterials.contains(material) || material == EraseMaterial)) {
					String materialKey = (material == EraseMaterial) ? "0:0" : material.getId() + ":" + items[i].getData().getData();
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
	
	public static Wand createWand(PlayerSpells playerSpells, String templateName) {
		Wand wand = new Wand();
		List<String> defaultSpells = new ArrayList<String>();
		String defaultName = defaultWandName;

		// See if there is a template with this key
		if (wandTemplates.containsKey(templateName)) {
			ConfigurationNode wandConfig = wandTemplates.get(templateName);
			defaultName = wandConfig.getString("name", defaultName);
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
						wand.addMaterial(EraseMaterial, data);
					} else {
						wand.addMaterial(ConfigurationNode.toMaterial(materialName), data);
					}
				}
			}
			
			wand.setCostReduction((float)wandConfig.getDouble("cost_reduction", 0));
			wand.setDamageReduction((float)wandConfig.getDouble("damage_reduction", 0));
			wand.setDamageReductionPhysical((float)wandConfig.getDouble("damage_reduction_physical", 0));
			wand.setDamageReductionProjectiles((float)wandConfig.getDouble("damage_reduction_projectiles", 0));
			wand.setDamageReductionFalling((float)wandConfig.getDouble("damage_reduction_falling", 0));
			wand.setDamageReductionFire((float)wandConfig.getDouble("damage_reduction_fire", 0));
			wand.setDamageReductionExplosions((float)wandConfig.getDouble("damage_reduction_explosions", 0));
			wand.setXPRegeneration(wandConfig.getInt("xp_regeneration", 0));
			wand.setXPMax(wandConfig.getInt("xp_max", 0));
			wand.setHealthRegeneration(wandConfig.getInt("health_regeneration", 0));
			wand.setHungerRegeneration(wandConfig.getInt("hunger_regeneration", 0));
			wand.setUses((int)wandConfig.getInt("uses", 0));
		}

		wand.addSpells(defaultSpells);
		wand.setName(defaultName, playerSpells);
		
		return wand;
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
	
	protected static void loadProperties(File propertiesFile)
	{
		loadProperties(new Configuration(propertiesFile));
	}
	
	protected static void loadProperties(InputStream properties)
	{
		loadProperties(new Configuration(properties));
	}
	
	protected static void loadProperties(Configuration properties)
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
		}
	}
	
	public static Collection<ConfigurationNode> getWandTemplates() {
		return wandTemplates.values();
	}
	
	protected void updateSpellSettings(PlayerSpells spells) {
		updateSpellSettings(spells, false);
	}
	
	protected void updateSpellSettings(PlayerSpells spells, boolean clearValue) {
		spells.setDamageReduction(clearValue ? 0 : damageReduction);
		spells.setDamageReductionPhysical(clearValue ? 0 : damageReductionPhysical);
		spells.setDamageReductionProjectiles(clearValue ? 0 : damageReductionProjectiles);
		spells.setDamageReductionFalling(clearValue ? 0 : damageReductionFalling);
		spells.setDamageReductionFire(clearValue ? 0 : damageReductionFire);
		spells.setDamageReductionExplosions(clearValue ? 0 : damageReductionExplosions);
		spells.setCostReduction(clearValue ? 0 : costReduction);
		spells.setXPRegeneration(clearValue ? 0 : xpRegeneration);
		spells.setXPMax(clearValue ? 0 : xpMax);
		spells.setHealthRegeneration(clearValue ? 0 : healthRegeneration);
		spells.setHungerRegeneration(clearValue ? 0 : hungerRegeneration);
	}

	@SuppressWarnings("deprecation")
	protected void updateActiveMaterial(PlayerSpells playerSpells) {
		if (activeMaterial == null) {
			playerSpells.clearBuildingMaterial();
		} else {
			String[] pieces = StringUtils.split(activeMaterial, ":");
			byte data = 0;
			if (pieces.length > 1) {
				data = Byte.parseByte(pieces[1]);
			}
			Material material = Material.getMaterial(Integer.parseInt(pieces[0]));
			playerSpells.setBuildingMaterial(material, data);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void openInventory(PlayerSpells playerSpells, int itemSlot) {
		if (playerSpells.storeInventory(itemSlot, item)) {
			updateInventory(playerSpells, itemSlot);
			playerSpells.getPlayer().updateInventory();
		}
	}
	
	public void openInventory(PlayerSpells playerSpells) {
		openInventory(playerSpells, playerSpells.getPlayer().getInventory().getHeldItemSlot());
	}
	
	@SuppressWarnings("deprecation")
	private void closeInventory(PlayerSpells playerSpells, int itemSlot) {
		saveInventory(playerSpells);
		playerSpells.restoreInventory(itemSlot, item);
		playerSpells.getPlayer().updateInventory();
	}
	
	public void closeInventory(PlayerSpells playerSpells) {
		closeInventory(playerSpells, playerSpells.getPlayer().getInventory().getHeldItemSlot());
	}
	
	public void activate(PlayerSpells playerSpells) {
		updateSpellSettings(playerSpells);
		updateActiveMaterial(playerSpells);
	}
	
	public boolean isInventoryOpen(PlayerSpells playerSpells) {
		return playerSpells.hasStoredInventory();
	}
	
	public void deactivate(PlayerSpells playerSpells) {
		deactivate(playerSpells, playerSpells.getPlayer().getInventory().getHeldItemSlot());
	}
	
	public void deactivate(PlayerSpells playerSpells, int itemSlot) {
		if (isInventoryOpen(playerSpells)) {
			closeInventory(playerSpells, itemSlot);
		}
		
		updateSpellSettings(playerSpells, true);
	}
	
	public boolean cast(PlayerSpells playerSpells) {
		Spell spell = playerSpells.getSpell(activeSpell);
		if (spell != null) {
			if (spell.cast()) {
				use(playerSpells);
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	protected void use(PlayerSpells playerSpells) {
		if (uses > 0) {
			short durability = item.getDurability();
			if (durability >= uses - 1) {
				deactivate(playerSpells);
				PlayerInventory playerInventory = playerSpells.getPlayer().getInventory();
				playerInventory.setItemInHand(new ItemStack(Material.AIR, 1));
				playerSpells.getPlayer().updateInventory();
			} else {
				item.setDurability((short)(durability + 1));
				updateName(playerSpells);
				updateLore(getSpells().length, getMaterials().length);
			}
		}
	}
}
