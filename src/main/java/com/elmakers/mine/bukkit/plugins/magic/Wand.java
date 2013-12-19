package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
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
	private final static int inventorySize = 27;
	private final static int hotbarSize = 9;
	
	private ItemStack item;
	private Spells spells;
	private PlayerSpells activePlayer;
	
	// Cached state
	private String id;
	private Inventory hotbar;
	private List<Inventory> inventories;
	
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
	
	private int effectColor = 0;
	
	private float defaultWalkSpeed = 0.2f;
	private float defaultFlySpeed = 0.1f;
	private float speedIncrease = 0;
	
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
	
	// Inventory functionality
	Integer openInventoryPage;
	
	private Wand() {
		hotbar = InventoryUtils.createInventory(null, 9, "Wand");
		inventories = new ArrayList<Inventory>();
	}
	
	public Wand(Spells spells) {
		this();
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
		this();
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
		String materialKey = "";
		if (material == CopyMaterial) {
			materialKey = "-1:0";
		} else if (material == EraseMaterial) {
			materialKey = "0:0";
		} else {
			materialKey = material.getId() + ":" + data;
		}
		setActiveMaterial(materialKey);
	}
	protected void setActiveMaterial(String materialKey) {
		this.activeMaterial = materialKey;
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
	
	public int getExperience() {
		return xp;
	}
	
	public void removeExperience(int amount) {
		xp = Math.max(0,  xp - amount);
		updateMana();
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
		return cooldownReduction;
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
	
	protected List<Inventory> getAllInventories() {
		List<Inventory> allInventories = new ArrayList<Inventory>(inventories.size() + 1);
		allInventories.add(hotbar);
		allInventories.addAll(inventories);
		return allInventories;
	}
	
	protected Set<String> getSpells() {
		return getSpells(false);
	}
	
	protected Set<String> getSpells(boolean includePositions) {
		Set<String> spellNames = new TreeSet<String>();
		List<Inventory> allInventories = getAllInventories();
		int index = 0;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int i = 0; i < items.length; i++) {
				if (items[i] != null && !isWand(items[i])) {
					if (isSpell(items[i])) {
						String spellName = getSpell(items[i]);
						if (includePositions) {
							spellName += "@" + index;
						}
						spellNames.add(spellName);
					}
				}	
				index++;
			}
		}
		return spellNames;
	}
	
	protected String getSpellString() {
		return StringUtils.join(getSpells(true), "|");
	}

	protected Set<String> getMaterialNames() {
		return getMaterialNames(false);
	}
	
	@SuppressWarnings("deprecation")
	protected Set<String> getMaterialNames(boolean includePositions) {
		Set<String> materialNames = new TreeSet<String>();
		List<Inventory> allInventories = new ArrayList<Inventory>(inventories.size() + 1);
		allInventories.add(hotbar);
		allInventories.addAll(inventories);
		int index = 0;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int i = 0; i < items.length; i++) {
				if (items[i] != null && !isWand(items[i])) {
					if (!isSpell(items[i])) {
						Material material = items[i].getType();
						if (material != Material.AIR) {
							String materialKey = material.getId() + ":" + items[i].getData().getData();
							if (material == EraseMaterial) {
								materialKey = "0:0"; 
							} else if (material == CopyMaterial) {
								materialKey = "-1:0"; 
							}
							if (includePositions) {
								materialKey += "@" + index;
							}
							materialNames.add(materialKey);
						}
					}
				}	
				index++;
			}
		}
		return materialNames;	
	}
	
	protected String getMaterialString() {
		return StringUtils.join(getMaterialNames(true), "|");		
	}
	
	protected Integer parseSlot(String[] pieces) {
		Integer slot = null;
		if (pieces.length > 0) {
			try {
				slot = Integer.parseInt(pieces[1]);
			} catch (Exception ex) {
				slot = null;
			}
			if (slot != null && slot < 0) {
				slot = null;
			}
		}
		return slot;
	}
	
	protected void addToInventory(ItemStack itemStack) {
		List<Inventory> allInventories = getAllInventories();
		boolean added = false;
		// Set the wand item
		Integer selectedItem = null;
		if (activePlayer != null && activePlayer.getPlayer() != null) {
			selectedItem = activePlayer.getPlayer().getInventory().getHeldItemSlot();
			hotbar.setItem(selectedItem, item);
		}
		for (Inventory inventory : allInventories) {
			HashMap<Integer, ItemStack> returned = inventory.addItem(itemStack);
			if (returned.size() == 0) {
				added = true;
				break;
			}
		}
		if (!added) {
			Inventory newInventory = InventoryUtils.createInventory(null, inventorySize, "Wand");
			newInventory.addItem(itemStack);
			inventories.add(newInventory);
		}
		if (selectedItem != null) {
			hotbar.setItem(selectedItem, null);
		}
	}
	
	protected Inventory getInventory(Integer slot) {
		Inventory inventory = hotbar;
		if (slot >= hotbarSize) {
			int inventoryIndex = (slot - hotbarSize) / inventorySize;
			while (inventoryIndex >= inventories.size()) {
				inventories.add(InventoryUtils.createInventory(null, inventorySize, "Wand"));
			}
			inventory = inventories.get(inventoryIndex);
		}
		
		return inventory;
	}
	
	protected int getInventorySlot(Integer slot) {
		if (slot < hotbarSize) {
			return slot;
		}
		
		return ((slot - hotbarSize) % inventorySize);
	}
	
	protected void addToInventory(ItemStack itemStack, Integer slot) {
		if (slot == null) {
			addToInventory(itemStack);
			return;
		}
		
		Inventory inventory = getInventory(slot);
		slot = getInventorySlot(slot);
		
		ItemStack existing = inventory.getItem(slot);
		inventory.setItem(slot, itemStack);
		
		if (existing != null && existing.getType() != Material.AIR) {
			addToInventory(existing);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void parseInventoryStrings(String spellString, String materialString) {
		hotbar.clear();
		inventories.clear();
		String[] spellNames = StringUtils.split(spellString, "|");
		for (String spellName : spellNames) {
			String[] pieces = spellName.split("@");
			Integer slot = parseSlot(pieces);
			ItemStack itemStack = createSpellItem(pieces[0]);
			if (itemStack == null) continue;
			addToInventory(itemStack, slot);
		}
		String[] materialNames = StringUtils.split(materialString, "|");
		for (String materialName : materialNames) {
			String[] pieces = materialName.split("@");
			Integer slot = parseSlot(pieces);
			
			String[] nameParts = StringUtils.split(pieces[0], ":");
			int typeId = Integer.parseInt(nameParts[0]);
			if (typeId == 0) {
				typeId = EraseMaterial.getId();
			} else if (typeId == -1) {
				typeId = CopyMaterial.getId();
			}
			byte dataId = nameParts.length > 1 ? Byte.parseByte(nameParts[1]) : 0;		
			ItemStack itemStack = createMaterialItem(typeId, dataId);
			addToInventory(itemStack, slot);
		}
		hasInventory = spellNames.length + materialNames.length > 1;
	}
	
	protected ItemStack createSpellItem(String spellName) {
		Spell spell = spells.getSpell(spellName);
		if (spell == null) return null;
		
		ItemStack itemStack = new ItemStack(spell.getMaterial(), 1);
		itemStack = InventoryUtils.getCopy(itemStack);
		updateSpellName(itemStack, spell, true);
		return itemStack;
	}
	
	@SuppressWarnings("deprecation")
	protected ItemStack createMaterialItem(int typeId, byte dataId) {
		if (typeId == 0) {
			typeId = EraseMaterial.getId();
		} else if (typeId == -1) {
			typeId = CopyMaterial.getId();
		}		
		ItemStack itemStack = new ItemStack(typeId, 1, (short)0, (byte)dataId);		
		itemStack = InventoryUtils.getCopy(itemStack);
		ItemMeta meta = itemStack.getItemMeta();
		if (typeId == EraseMaterial.getId()) {
			typeId = EraseMaterial.getId();
			List<String> lore = new ArrayList<String>();
			lore.add("Fills with Air");
			meta.setLore(lore);
		} else if (typeId == CopyMaterial.getId()) {
			typeId = EraseMaterial.getId();
			List<String> lore = new ArrayList<String>();
			lore.add("Fills with the target material");
			meta.setLore(lore);
		} else {
			List<String> lore = new ArrayList<String>();
			Material material = Material.getMaterial(typeId);
			if (material != null) {
				lore.add(ChatColor.GRAY + getMaterialName(material, (byte)dataId));
			}
			lore.add(ChatColor.LIGHT_PURPLE + "Magic building material");
			meta.setLore(lore);
		}
		meta.setDisplayName(getActiveWandName(Material.getMaterial(typeId)));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	protected void saveState() {
		Object wandNode = InventoryUtils.createNode(item, "wand");
		
		InventoryUtils.setMeta(wandNode, "id", id);
		String wandMaterials = getMaterialString();
		String wandSpells = getSpellString();
		InventoryUtils.setMeta(wandNode, "materials", wandMaterials);
		InventoryUtils.setMeta(wandNode, "spells", wandSpells);
		InventoryUtils.setMeta(wandNode, "active_spell", activeSpell);
		InventoryUtils.setMeta(wandNode, "active_material", activeMaterial);
		InventoryUtils.setMeta(wandNode, "name", wandName);
	
		InventoryUtils.setMeta(wandNode, "cost_reduction", floatFormat.format(costReduction));
		InventoryUtils.setMeta(wandNode, "cooldown_reduction", floatFormat.format(cooldownReduction));
		InventoryUtils.setMeta(wandNode, "power", floatFormat.format(power));
		InventoryUtils.setMeta(wandNode, "protection", floatFormat.format(damageReduction));
		InventoryUtils.setMeta(wandNode, "protection_physical", floatFormat.format(damageReductionPhysical));
		InventoryUtils.setMeta(wandNode, "protection_projectiles", floatFormat.format(damageReductionProjectiles));
		InventoryUtils.setMeta(wandNode, "protection_falling", floatFormat.format(damageReductionFalling));
		InventoryUtils.setMeta(wandNode, "protection_fire", floatFormat.format(damageReductionFire));
		InventoryUtils.setMeta(wandNode, "protection_explosions", floatFormat.format(damageReductionExplosions));
		InventoryUtils.setMeta(wandNode, "haste", floatFormat.format(speedIncrease));
		InventoryUtils.setMeta(wandNode, "xp", Integer.toString(xp));
		InventoryUtils.setMeta(wandNode, "xp_regeneration", Integer.toString(xpRegeneration));
		InventoryUtils.setMeta(wandNode, "xp_max", Integer.toString(xpMax));
		InventoryUtils.setMeta(wandNode, "health_regeneration", Integer.toString(healthRegeneration));
		InventoryUtils.setMeta(wandNode, "hunger_regeneration", Integer.toString(hungerRegeneration));
		InventoryUtils.setMeta(wandNode, "uses", Integer.toString(uses));
		InventoryUtils.setMeta(wandNode, "has_inventory", Integer.toString((hasInventory ? 1 : 0)));
		InventoryUtils.setMeta(wandNode, "effect_color", Integer.toString(effectColor, 16));
	}
	
	
	protected void loadState() {
		Object wandNode = InventoryUtils.getNode(item, "wand");
		if (wandNode == null) {
			spells.getPlugin().getLogger().warning("Found a wand with missing NBT data. This may be an old wand, or something may have wiped its data");
            return;
		}
		
		// Don't generate a UUID unless we need to, not sure how expensive that is.
		id = InventoryUtils.getMeta(wandNode, "id");
		id = id == null || id.length() == 0 ? UUID.randomUUID().toString() : id;
		wandName = InventoryUtils.getMeta(wandNode, "name", wandName);
		
		String wandMaterials = InventoryUtils.getMeta(wandNode, "materials", "");
		String wandSpells = InventoryUtils.getMeta(wandNode, "spells", "");
		parseInventoryStrings(wandSpells, wandMaterials);
		activeSpell = InventoryUtils.getMeta(wandNode, "active_spell", activeSpell);
		activeMaterial = InventoryUtils.getMeta(wandNode, "active_material", activeMaterial);
		
		costReduction = Float.parseFloat(InventoryUtils.getMeta(wandNode, "cost_reduction", floatFormat.format(costReduction)));
		cooldownReduction = Float.parseFloat(InventoryUtils.getMeta(wandNode, "cooldown_reduction", floatFormat.format(cooldownReduction)));
		power = Float.parseFloat(InventoryUtils.getMeta(wandNode, "power", floatFormat.format(power)));
		damageReduction = Float.parseFloat(InventoryUtils.getMeta(wandNode, "protection", floatFormat.format(damageReduction)));
		damageReductionPhysical = Float.parseFloat(InventoryUtils.getMeta(wandNode, "protection_physical", floatFormat.format(damageReductionPhysical)));
		damageReductionProjectiles = Float.parseFloat(InventoryUtils.getMeta(wandNode, "protection_projectiles", floatFormat.format(damageReductionProjectiles)));
		damageReductionFalling = Float.parseFloat(InventoryUtils.getMeta(wandNode, "protection_falling", floatFormat.format(damageReductionFalling)));
		damageReductionFire = Float.parseFloat(InventoryUtils.getMeta(wandNode, "protection_fire", floatFormat.format(damageReductionFire)));
		damageReductionExplosions = Float.parseFloat(InventoryUtils.getMeta(wandNode, "protection_explosions", floatFormat.format(damageReductionExplosions)));
		speedIncrease = Float.parseFloat(InventoryUtils.getMeta(wandNode, "haste", floatFormat.format(speedIncrease)));
		xp = Integer.parseInt(InventoryUtils.getMeta(wandNode, "xp", Integer.toString(xp)));
		xpRegeneration = Integer.parseInt(InventoryUtils.getMeta(wandNode, "xp_regeneration", Integer.toString(xpRegeneration)));
		xpMax = Integer.parseInt(InventoryUtils.getMeta(wandNode, "xp_max", Integer.toString(xpMax)));
		healthRegeneration = Integer.parseInt(InventoryUtils.getMeta(wandNode, "health_regeneration", Integer.toString(healthRegeneration)));
		hungerRegeneration = Integer.parseInt(InventoryUtils.getMeta(wandNode, "hunger_regeneration", Integer.toString(hungerRegeneration)));
		uses = Integer.parseInt(InventoryUtils.getMeta(wandNode, "uses", Integer.toString(uses)));
		hasInventory = Integer.parseInt(InventoryUtils.getMeta(wandNode, "has_inventory", (hasInventory ? "1" : "0"))) != 0;
		effectColor = Integer.parseInt(InventoryUtils.getMeta(wandNode, "effect_color", Integer.toString(effectColor, 16)), 16);
		
		// This is done here as an extra safety measure.
		// A walk speed too high will cause a server error.
		speedIncrease = Math.min(WandLevel.maxSpeedIncrease, speedIncrease);
	}
	
	@SuppressWarnings("deprecation")
	public void removeMaterial(Material material, byte data) {
		if (isInventoryOpen()) {
			saveInventory();
		}
		Integer id = material.getId();
		String materialString = id.toString();
		materialString += ":" + data;
		if (materialString.equals(activeMaterial)) {
			activeMaterial = null;
		}
		
		List<Inventory> allInventories = getAllInventories();
		boolean found = false;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int index = 0; index < items.length; index++) {
				ItemStack itemStack = items[index];
				if (itemStack != null && itemStack.getType() != Material.AIR && !isWand(itemStack) && !isSpell(itemStack)) {
					if (itemStack.getType() == material && data == itemStack.getData().getData()) {
						found = true;
						inventory.setItem(index, null);
					} else if (activeMaterial == null) {
						activeMaterial = "" + itemStack.getTypeId() + ":" + (itemStack.getData().getData());
					}
					if (found && activeMaterial != null) {
						break;
					}
				}
			}
		}
		updateActiveMaterial();
		updateInventoryNames(true);
		updateName();
		updateLore();
		saveState();
		if (isInventoryOpen()) {
			updateInventory();
		}
	}
	
	public boolean addMaterial(Material material, byte data) {
		return addMaterial(material, data, true);
	}
	
	public boolean hasMaterial(int materialId, byte data) {
		String materialName = materialId + ":" + data;
		return getMaterialNames().contains(materialName);
	}

	@SuppressWarnings("deprecation")
	public boolean hasMaterial(Material material, byte data) {
		return hasMaterial(material.getId(), data);
	}
	
	public boolean hasSpell(String spellName) {
		return getSpells().contains(spellName);
	}
	
	public boolean addMaterial(String materialName, boolean makeActive) {
		Integer materialId = null;
		byte data = 0;
		try {
			String[] pieces = materialName.split(":");
			data = pieces.length > 1 ? Byte.parseByte(pieces[1]) : 0;
			materialId =Integer.parseInt(pieces[0]);
		} catch (Exception ex) {
			materialId = null;
		}
		if (materialId == null) {
			return false;
		}
		boolean addedNew = !hasMaterial(materialId, data);
		if (addedNew) {
			addToInventory(createMaterialItem(materialId, data));
		}
		if (activeMaterial == null || activeMaterial.length() == 0 || makeActive) {
			activeMaterial = materialName;
		}
		updateActiveMaterial();
		updateInventoryNames(true);
		updateName();
		updateLore();
		saveState();
		if (isInventoryOpen()) {
			updateInventory();
		}
		hasInventory = getSpells().size() + getMaterialNames().size() > 1;
		
		return addedNew;
	}
	
	@SuppressWarnings("deprecation")
	public boolean addMaterial(Material material, byte data, boolean makeActive) {
		if (isInventoryOpen()) {
			saveInventory();
		}
		Integer id = material.getId();
		String materialString = id.toString();
		if (material == EraseMaterial) {
			materialString = "0";
		} else if (material == CopyMaterial) {
			materialString = "-1";
		}
		materialString += ":" + data;
		return addMaterial(materialString, makeActive);
	}
	
	public void removeSpell(String spellName) {
		if (isInventoryOpen()) {
			saveInventory();
		}
		if (spellName.equals(activeSpell)) {
			activeSpell = null;
		}
		
		List<Inventory> allInventories = getAllInventories();
		boolean found = false;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int index = 0; index < items.length; index++) {
				ItemStack itemStack = items[index];
				if (itemStack != null && itemStack.getType() != Material.AIR && !isWand(itemStack) && isSpell(itemStack)) {
					if (getSpell(itemStack).equals(spellName)) {
						found = true;
						inventory.setItem(index, null);
					} else if (activeSpell == null) {
						activeSpell = getSpell(itemStack);
					}
					if (found && activeSpell != null) {
						break;
					}
				}
			}
		}
		updateInventoryNames(true);
		updateName();
		updateLore();
		saveState();
		if (isInventoryOpen()) {
			updateInventory();
		}
	}
	
	public boolean addSpell(String spellName, boolean makeActive) {
		if (isInventoryOpen()) {
			saveInventory();
		}
		boolean addedNew = !hasSpell(spellName);
		if (addedNew) {
			addToInventory(createSpellItem(spellName));
		}
		if (activeSpell == null || activeSpell.length() == 0 || makeActive) {
			activeSpell = spellName;
		}
		hasInventory = getSpells().size() + getMaterialNames().size() > 1;
		updateInventoryNames(true);
		updateName();
		updateLore();
		saveState();
		if (isInventoryOpen()) {
			updateInventory();
		}
		
		return addedNew;
	}
	
	public boolean addSpell(String spellName) {
		return addSpell(spellName, true);
	}

	private String getActiveWandName(Spell spell, String materialName) {
		// Build wand name
		String name = wandName;
		
		// Add active spell to description
		if (spell != null) {
			if (materialName != null) {
				materialName = materialName.replace('_', ' ');
				name = ChatColor.GOLD + spell.getName() + ChatColor.GRAY + " " + materialName + ChatColor.WHITE + " (" + wandName + ")";
			} else {
				name = ChatColor.GOLD + spell.getName() + ChatColor.WHITE + " (" + wandName + ")";
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
		return getMaterialName(material, (byte)0);
	}
	
	@SuppressWarnings("deprecation")
	private String getMaterialName(Material material, byte data) {
		String materialName = null;
		
		if (material == EraseMaterial) {
			materialName = "erase";
		} else if (material == CopyMaterial) {
			materialName = "copy";
		} else {
			materialName = material.name().toLowerCase();
			// I started doing this the "right" way by looking at MaterialData
			// But I don't feel like waiting for Bukkit to update their classes.
			// This also seems super ugly and messy.. if this is the replacement for "magic numbers", count me out :P
			/*
			Class<? extends MaterialData> materialData = material.getData();
			if (Dye.class.isAssignableFrom(materialData)) {
				Dye dye = new Dye(material, data);
				materialName += " " + dye.getColor().name();
			} else if (Dye.class.isAssignableFrom(materialData)) {
				Dye dye = new Dye(material, data);
				materialName += " " + dye.getColor().name();
			}
			*/
			
			if (material == Material.CARPET || material == Material.STAINED_GLASS || material == Material.STAINED_CLAY || material == Material.STAINED_GLASS_PANE || material == Material.WOOL) {
				// Note that getByDyeData doesn't work for stained glass or clay. Kind of misleading?
				DyeColor color = DyeColor.getByWoolData(data);
				materialName = color.name().toLowerCase().replace('_', ' ') + " " + materialName;
			} else if (material == Material.WOOD || material == Material.LOG || material == Material.SAPLING || material == Material.LEAVES) {
				TreeSpecies treeSpecies = TreeSpecies.getByData(data);
				materialName = treeSpecies.name().toLowerCase().replace('_', ' ') + " " + materialName;
			}
		}
		materialName = materialName.replace('_', ' ');
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
		Spell spell = null;
		if (hasInventory) {
			spell = spells.getSpell(activeSpell);
		}
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
		updateLore(getSpells().size(), getMaterialNames().size());
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
		if (xpRegeneration > 0) {
			lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "Mana: " + xpMax);
			lore.add(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + getLevelString("Mana Regeneration", xpRegeneration / WandLevel.maxXpRegeneration));
		}
		if (costReduction > 0) lore.add(ChatColor.AQUA + getLevelString("Cost Reduction", costReduction));
		if (cooldownReduction > 0) lore.add(ChatColor.AQUA + getLevelString("Cooldown Reduction", cooldownReduction));
		if (power > 0) lore.add(ChatColor.AQUA + getLevelString("Power", power));
		if (speedIncrease > 0) lore.add(ChatColor.AQUA + getLevelString("Haste", speedIncrease / WandLevel.maxSpeedIncrease));
		if (damageReduction > 0) lore.add(ChatColor.AQUA + getLevelString("Protection", damageReduction));
		if (damageReduction < 1) {
			if (damageReductionPhysical > 0) lore.add(ChatColor.AQUA + getLevelString("Physical Protection", damageReductionPhysical));
			if (damageReductionProjectiles > 0) lore.add(ChatColor.AQUA + getLevelString("Projectile Protection", damageReductionProjectiles));
			if (damageReductionFalling > 0) lore.add(ChatColor.AQUA + getLevelString("Fall Protection", damageReductionFalling));
			if (damageReductionFire > 0) lore.add(ChatColor.AQUA + getLevelString("Fire Protection", damageReductionFire));
			if (damageReductionExplosions > 0) lore.add(ChatColor.AQUA + getLevelString("Blast Protection", damageReductionExplosions));
		}
		if (healthRegeneration > 0) lore.add(ChatColor.AQUA + getLevelString("Health Regeneration", healthRegeneration / WandLevel.maxRegeneration));
		if (hungerRegeneration > 0) lore.add(ChatColor.AQUA + getLevelString("Anti-Hunger", hungerRegeneration / WandLevel.maxRegeneration));
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		InventoryUtils.addGlow(item);

		// Reset spell list and wand config
		saveState();
	}
	
	public int getRemainingUses() {
		return uses;
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

	public static String getSpell(ItemStack item) {
		if (!isSpell(item)) return null;
		
		Object spellNode = InventoryUtils.getNode(item, "spell");
		return InventoryUtils.getMeta(spellNode, "key");
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
			Spell spell = activePlayer.getSpell(getSpell(item));
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
	private void updateInventory() {
		if (activePlayer == null) return;
		if (!isInventoryOpen()) return;
		if (activePlayer.getPlayer() == null) return;
		if (!activePlayer.hasStoredInventory()) return;
		
		// Clear the player's inventory
		Player player = activePlayer.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		playerInventory.clear();
		
		// First add the wand and check the hotbar for conflicts
		int currentSlot = playerInventory.getHeldItemSlot();
		ItemStack existingHotbar = hotbar.getItem(currentSlot);
		if (existingHotbar != null && existingHotbar.getType() != Material.AIR && !isWand(existingHotbar)) {
			addToInventory(existingHotbar);
			hotbar.setItem(currentSlot, null);
		}
		playerInventory.setItem(currentSlot, item);
		
		// Set hotbar
		for (int hotbarSlot = 0; hotbarSlot < hotbarSize; hotbarSlot++) {
			if (hotbarSlot != currentSlot) {
				playerInventory.setItem(hotbarSlot, hotbar.getItem(hotbarSlot));
			}
		}
		
		// Set inventory from current page
		if (openInventoryPage < inventories.size()) {
			Inventory inventory = inventories.get(openInventoryPage);
			ItemStack[] contents = inventory.getContents();
			for (int i = 0; i < contents.length; i++) {
				playerInventory.setItem(i + hotbarSize, contents[i]);
			}	
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
	
	protected Inventory getOpenInventory() {
		if (openInventoryPage == null) {
			openInventoryPage = 0;
		}
		while (openInventoryPage >= inventories.size()) {
			inventories.add(InventoryUtils.createInventory(null, inventorySize, "Wand"));
		}
		return inventories.get(openInventoryPage);
	}
	
	public void saveInventory() {
		if (activePlayer == null) return;
		if (activePlayer == null) return;
		if (!isInventoryOpen()) return;
		if (activePlayer.getPlayer() == null) return;
		if (!activePlayer.hasStoredInventory()) return;
		
		// Fill in the hotbar
		Player player = activePlayer.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		for (int i = 0; i < hotbarSize; i++) {
			ItemStack playerItem = playerInventory.getItem(i);
			if (isWand(playerItem)) {
				playerItem = null;
			}
			hotbar.setItem(i, playerItem);
		}
		
		// Fill in the active inventory page
		Inventory openInventory = getOpenInventory();
		for (int i = 0; i < openInventory.getSize(); i++) {
			openInventory.setItem(i, playerInventory.getItem(i + hotbarSize));
		}
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
					wand.addSpell((String)spellName);
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

		wand.setName(wandName);
		
		return wand;
	}
	
	public void add(Wand other) {
		costReduction = Math.max(costReduction, other.costReduction);
		power = Math.max(power, other.power);
		damageReduction = Math.max(damageReduction, other.damageReduction);
		damageReductionPhysical = Math.max(damageReductionPhysical, other.damageReductionPhysical);
		damageReductionProjectiles = Math.max(damageReductionProjectiles, other.damageReductionProjectiles);
		damageReductionFalling = Math.max(damageReductionFalling, other.damageReductionFalling);
		damageReductionFire = Math.max(damageReductionFire, other.damageReductionFire);
		damageReductionExplosions = Math.max(damageReductionExplosions, other.damageReductionExplosions);
		xpRegeneration = Math.max(xpRegeneration, other.xpRegeneration);
		xpMax = Math.max(xpMax, other.xpMax);
		xp = Math.max(xp, other.xp);
		healthRegeneration = Math.max(healthRegeneration, other.healthRegeneration);
		hungerRegeneration = Math.max(hungerRegeneration, other.hungerRegeneration);
		speedIncrease = Math.max(speedIncrease, other.speedIncrease);
		effectColor = Math.max(effectColor, other.effectColor);
		
		// Eliminate limited-use wands
		if (uses == 0 || other.uses == 0) {
			uses = 0;
		} else {
			// Otherwise add them
			uses = uses + other.uses;
		}
		
		// Add spells
		Set<String> spells = other.getSpells();
		for (String spell : spells) {
			addSpell(spell, false);
		}

		// Add materials
		Set<String> materials = other.getMaterialNames();
		for (String material : materials) {
			addMaterial(material, false);
		}

		saveState();
		updateName();
		updateLore();
	}
	
	public void configureProperties(ConfigurationNode wandConfig) {
		costReduction = (float)wandConfig.getDouble("cost_reduction", costReduction);
		cooldownReduction = (float)wandConfig.getDouble("cooldown_reduction", cooldownReduction);
		power = (float)wandConfig.getDouble("power", power);
		damageReduction = (float)wandConfig.getDouble("protection", damageReduction);
		damageReductionPhysical = (float)wandConfig.getDouble("protection_physical", damageReductionPhysical);
		damageReductionProjectiles = (float)wandConfig.getDouble("protection_projectiles", damageReductionPhysical);
		damageReductionFalling = (float)wandConfig.getDouble("protection_falling", damageReductionFalling);
		damageReductionFire = (float)wandConfig.getDouble("protection_fire", damageReductionFire);
		damageReductionExplosions = (float)wandConfig.getDouble("protection_explosions", damageReductionExplosions);
		xpRegeneration = wandConfig.getInt("xp_regeneration", xpRegeneration);
		xpMax = wandConfig.getInt("xp_max", xpMax);
		xp = wandConfig.getInt("xp", xp);
		healthRegeneration = wandConfig.getInt("health_regeneration", healthRegeneration);
		hungerRegeneration = wandConfig.getInt("hunger_regeneration", hungerRegeneration);
		uses = wandConfig.getInt("uses", uses);
		effectColor = Integer.parseInt(wandConfig.getString("effect_color", "0"), 16);
	
		// Make sure to adjust the player's walk speed if it changes and this wand is active.
		float oldWalkSpeedIncrease = speedIncrease;
		speedIncrease = (float)wandConfig.getDouble("haste", speedIncrease);
		if (activePlayer != null && speedIncrease != oldWalkSpeedIncrease) {
			Player player = activePlayer.getPlayer();
			player.setWalkSpeed(defaultWalkSpeed + speedIncrease);
			player.setFlySpeed(defaultFlySpeed + speedIncrease);
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
	public void toggleInventory() {
		if (!hasInventory) {
			return;
		}
		if (!isInventoryOpen()) {
			openInventoryPage = 0;
			openInventory();
		} else {
			saveInventory();
			int newInventoryPage = openInventoryPage + 1;
			if (newInventoryPage >= inventories.size()) {
				closeInventory();
			} else {
				saveInventory();
				openInventoryPage = newInventoryPage;
				updateInventory();
				if (activePlayer != null) {
					activePlayer.playSound(Sound.CHEST_OPEN, 0.3f, 1.5f);
					activePlayer.getPlayer().updateInventory();
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void openInventory() {
		if (activePlayer == null) return;
		if (activePlayer.hasStoredInventory()) return;
		if (activePlayer.storeInventory()) {
			activePlayer.playSound(Sound.CHEST_OPEN, 0.4f, 0.2f);
			updateInventory();
			activePlayer.getPlayer().updateInventory();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void closeInventory() {
		if (!isInventoryOpen()) return;
		saveInventory();
		openInventoryPage = null;
		if (activePlayer != null) {
			activePlayer.playSound(Sound.CHEST_CLOSE, 0.4f, 0.2f);
			activePlayer.restoreInventory();
			activePlayer.getPlayer().updateInventory();
		}
		saveState();
	}
	
	public void activate(PlayerSpells playerSpells) {
		activePlayer = playerSpells;
		Player player = activePlayer.getPlayer();
		if (speedIncrease > 0) {
			try {
				player.setWalkSpeed(defaultWalkSpeed + speedIncrease);
				player.setFlySpeed(defaultFlySpeed + speedIncrease);
			} catch(Exception ex2) {
				try {
					player.setWalkSpeed(defaultWalkSpeed);
					player.setFlySpeed(defaultFlySpeed);
				}  catch(Exception ex) {
					
				}
			}
		}
		activePlayer.setActiveWand(this);
		if (xpRegeneration > 0) {
			storedXpLevel = player.getLevel();
			storedXpProgress = player.getExp();
			storedXp = 0;
			updateMana();
		}
		updateActiveMaterial();
		updateName();
		
		if (effectColor != 0) {
			InventoryUtils.addPotionEffect(player, effectColor);
		}
	}
	
	protected void updateMana() {
		if (activePlayer != null && xpMax > 0 && xpRegeneration > 0) {
			Player player = activePlayer.getPlayer();
			player.setLevel(0);
			player.setExp((float)xp / (float)xpMax);
		}
	}
	
	public boolean isInventoryOpen() {
		return activePlayer != null && openInventoryPage != null;
	}
	
	public void deactivate() {
		if (activePlayer == null) return;
		saveState();

		if (effectColor > 0) {
			InventoryUtils.removePotionEffect(activePlayer.getPlayer());
		}
		
		// This is a tying wands together with other spells, potentially
		// But with the way the mana system works, this seems like the safest route.
		activePlayer.deactivateAllSpells();
		
		if (isInventoryOpen()) {
			closeInventory();
		}
		if (xpRegeneration > 0) {
			activePlayer.player.setExp(storedXpProgress);
			activePlayer.player.setLevel(storedXpLevel);
			activePlayer.player.giveExp(storedXp);
			storedXp = 0;
			storedXpProgress = 0;
			storedXpLevel = 0;
		}
		if (speedIncrease > 0) {
			try {
				activePlayer.getPlayer().setWalkSpeed(defaultWalkSpeed);
				activePlayer.getPlayer().setFlySpeed(defaultFlySpeed);
			}  catch(Exception ex) {
				
			}
		}
		activePlayer.setActiveWand(null);
		activePlayer = null;
	}
	
	public Spell getActiveSpell() {
		if (activePlayer == null) return null;
		return activePlayer.getSpell(activeSpell);
	}
	
	public boolean cast() {
		Spell spell = getActiveSpell();
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
			uses--;
			if (uses <= 0) {
				Player player = activePlayer.getPlayer();
				deactivate();
				activePlayer.playSound(Sound.ITEM_BREAK, 1.0f, 0.8f);
				PlayerInventory playerInventory = player.getInventory();
				playerInventory.setItemInHand(new ItemStack(Material.AIR, 1));
				player.updateInventory();
			} else {
				updateName();
				updateLore();
				saveState();
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
			xp = Math.min(xpMax, xp + xpRegeneration);
			updateMana();
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
	
	public void cycleSpells() {
		Set<String> spellsSet = getSpells();
		String[] spells = (String[])spellsSet.toArray();
		if (spells.length == 0) return;
		if (activeSpell == null) {
			activeSpell = spells[0].split("@")[0];
			return;
		}
		
		int spellIndex = 0;
		for (int i = 0; i < spells.length; i++) {
			if (spells[i].split("@")[0].equals(activeSpell)) {
				spellIndex = i;
				break;
			}
		}
		
		spellIndex = (spellIndex + 1) % spells.length;
		setActiveSpell(spells[spellIndex].split("@")[0]);
	}
	
	public void cycleMaterials() {
		Set<String> materialsSet = getMaterialNames();
		String[] materials = (String[])materialsSet.toArray();
		if (materials.length == 0) return;
		if (activeMaterial == null) {
			activeMaterial = materials[0].split("@")[0];
			return;
		}
		
		int materialIndex = 0;
		for (int i = 0; i < materials.length; i++) {
			if (materials[i].split("@")[0].equals(activeMaterial)) {
				materialIndex = i;
				break;
			}
		}
		
		materialIndex = (materialIndex + 1) % materials.length;
		setActiveMaterial(materials[materialIndex].split("@")[0]);
	}
	
	public boolean hasExperience() {
		return xpRegeneration > 0;
	}
}
