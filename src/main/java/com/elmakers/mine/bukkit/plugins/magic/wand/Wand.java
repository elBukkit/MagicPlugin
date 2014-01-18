package com.elmakers.mine.bukkit.plugins.magic.wand;

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
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.magic.CastingCost;
import com.elmakers.mine.bukkit.plugins.magic.CostReducer;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.Messages;
import com.elmakers.mine.bukkit.utilities.borrowed.Configuration;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;
import com.elmakers.mine.bukkit.utilities.borrowed.MaterialAndData;

public class Wand implements CostReducer {
	public final static int inventorySize = 27;
	public final static int hotbarSize = 9;
	
	private ItemStack item;
	private MagicController controller;
	private Mage mage;
	
	// Cached state
	private String id;
	private Inventory hotbar;
	private List<Inventory> inventories;
	
	private String activeSpell = "";
	private String activeMaterial = "";
	private String wandName = "";
	private String description = "";
	private String owner = "";
	
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
	private boolean modifiable = true;
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
	public static Material CopyMaterial = Material.SUGAR;
	public static Material CloneMaterial = Material.NETHER_STALK;
	public static Material ReplicateMaterial = Material.PUMPKIN_SEEDS;

	public static final String ERASE_MATERIAL_KEY = "erase";
	public static final String COPY_MATERIAL_KEY = "copy";
	public static final String CLONE_MATERIAL_KEY = "clone";
	public static final String REPLICATE_MATERIAL_KEY = "replicate";
	
	// Legacy
	private static final int ERASE_MATERIAL_ID = 0;
	private static final int COPY_MATERIAL_ID = -1;
	private static final int CLONE_MATERIAL_ID = -2;
	
	// Wand configurations
	protected static Map<String, ConfigurationNode> wandTemplates = new HashMap<String, ConfigurationNode>();
	private static final String propertiesFileName = "wands.yml";
	private static final String propertiesFileNameDefaults = "wands.defaults.yml";
	
	// Inventory functionality
	int openInventoryPage = 0;
	boolean inventoryIsOpen = false;
	
	private Wand() {
		hotbar = InventoryUtils.createInventory(null, 9, "Wand");
		inventories = new ArrayList<Inventory>();
	}
	
	public Wand(MagicController spells) {
		this();
		this.controller = spells;
		item = new ItemStack(WandMaterial);
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		item = InventoryUtils.getCopy(item);
		ItemMeta itemMeta = item.getItemMeta();
		item.setItemMeta(itemMeta);

		InventoryUtils.addGlow(item);
		id = UUID.randomUUID().toString();
		wandName = Messages.get("wand.default_name");
		updateName();
		saveState();
	}
	
	public Wand(MagicController spells, ItemStack item) {
		this();
		this.item = item;
		this.controller = spells;
		loadState();
	}

	public void setActiveSpell(String activeSpell) {
		this.activeSpell = activeSpell;
		updateName();
		updateInventoryNames(true);
		saveState();
	}

	public void setActiveMaterial(Material material, byte data) {
		String materialKey = getMaterialKey(material, data);
		setActiveMaterial(materialKey);
	}
	
	public void activateMaterial(Material material, byte data) {
		setActiveMaterial(material, data);
		if (activeMaterial != null) {
			MaterialBrush brush = mage.getBrush();
			if (activeMaterial.equals(CLONE_MATERIAL_KEY) || activeMaterial.equals(REPLICATE_MATERIAL_KEY)) {
				brush.setCloneLocation(mage.getLocation());
			}
		}
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
	
	public boolean isModifiable() {
		return modifiable;
	}
	
	public boolean usesMana() {
		return xpMax > 0 && xpRegeneration > 0;
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
	
	public String getDescription() {
		return description;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setName(String name) {
		wandName = name;
		updateName();
	}
	
	protected void setDescription(String description) {
		this.description = description;
		updateLore();
	}
	
	protected void takeOwnership(Player player) {
		owner = player.getName();
	}
	
	public void takeOwnership(Player player, String name, boolean updateDescription) {
		setName(name);
		takeOwnership(player);
		if (updateDescription) {
			setDescription(Messages.get("wand.owner_description", "$name's Wand").replace("$name", owner));
		}
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
	
	public Set<String> getSpells() {
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

	public Set<String> getMaterialKeys() {
		return getMaterialKeys(false);
	}

	protected static String getMaterialKey(ItemStack itemStack) {
		return getMaterialKey(itemStack, null);
	}
	
	@SuppressWarnings("deprecation")
	protected static String getMaterialKey(ItemStack itemStack, Integer index) {
		if (itemStack == null || isSpell(itemStack) || isWand(itemStack)) {
			return null;
		}
		Material material = itemStack.getType();
		if (material == Material.AIR) {
			return null;
		}
	
		String materialKey = getMaterialKey(material, itemStack.getData().getData());

		if (index != null) {
			materialKey += "@" + index;
		}
		
		return materialKey;
	}
	
	protected Set<String> getMaterialKeys(boolean includePositions) {
		Set<String> materialNames = new TreeSet<String>();
		List<Inventory> allInventories = new ArrayList<Inventory>(inventories.size() + 1);
		allInventories.add(hotbar);
		allInventories.addAll(inventories);
		Integer index = 0;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int i = 0; i < items.length; i++) {
				String materialKey = getMaterialKey(items[i], includePositions ? index : null);
				if (materialKey != null) {
					materialNames.add(materialKey);
				}
				index++;
			}
		}
		return materialNames;	
	}
	
	protected String getMaterialString() {
		return StringUtils.join(getMaterialKeys(true), "|");		
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
		if (mage != null && mage.getPlayer() != null) {
			selectedItem = mage.getPlayer().getInventory().getHeldItemSlot();
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
	
	protected Inventory getInventoryByIndex(int inventoryIndex) {
		while (inventoryIndex >= inventories.size()) {
			inventories.add(InventoryUtils.createInventory(null, inventorySize, "Wand"));
		}
		return inventories.get(inventoryIndex);
	}
	
	protected Inventory getInventory(Integer slot) {
		Inventory inventory = hotbar;
		if (slot >= hotbarSize) {
			int inventoryIndex = (slot - hotbarSize) / inventorySize;
			inventory = getInventoryByIndex(inventoryIndex);
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
	
	protected void parseInventoryStrings(String spellString, String materialString) {
		hotbar.clear();
		inventories.clear();
		String[] spellNames = StringUtils.split(spellString, "|");
		for (String spellName : spellNames) {
			String[] pieces = spellName.split("@");
			Integer slot = parseSlot(pieces);
			ItemStack itemStack = createSpellItem(pieces[0]);
			if (itemStack == null) {
				controller.getPlugin().getLogger().warning("Unable to create spell icon for key " + pieces[0]);
				continue;
			}
			addToInventory(itemStack, slot);
		}
		String[] materialNames = StringUtils.split(materialString, "|");
		for (String materialName : materialNames) {
			String[] pieces = materialName.split("@");
			Integer slot = parseSlot(pieces);
			ItemStack itemStack = createMaterialItem(pieces[0]);
			if (itemStack == null) {
				controller.getPlugin().getLogger().warning("Unable to create material icon for key " + pieces[0]);
				continue;
			}
			addToInventory(itemStack, slot);
		}
		hasInventory = spellNames.length + materialNames.length > 1;
	}
	
	protected ItemStack createMaterialItem(String materialKey) {
		MaterialAndData material = parseMaterialKey(materialKey);
		if (material == null) {
			return null;
		}
		return createMaterialItem(material.getMaterial(), material.getData());
	}
	
	@SuppressWarnings("deprecation")
	protected ItemStack createSpellItem(String spellName) {
		Spell spell = controller.getSpell(spellName);
		if (spell == null) return null;
		MaterialAndData icon = spell.getIcon();
		if (icon == null) {
			controller.getPlugin().getLogger().warning("Unable to create spell icon for " + spell.getName() + ", missing material");	
		}
		ItemStack itemStack = null;
		ItemStack originalItemStack = null;
		try {
			originalItemStack = new ItemStack(icon.getMaterial(), 1, (short)0, (byte)icon.getData());
			itemStack = InventoryUtils.getCopy(originalItemStack);
		} catch (Exception ex) {
			itemStack = null;
		}
		if (itemStack == null) {
			controller.getPlugin().getLogger().warning("Unable to create spell icon with material " + icon.getMaterial().name());	
			return originalItemStack;
		}
		updateSpellName(itemStack, spell, true);
		return itemStack;
	}
	
	@SuppressWarnings("deprecation")
	protected ItemStack createMaterialItem(Material material, byte dataId) {
		ItemStack originalItemStack = new ItemStack(material, 1, (short)0, (byte)dataId);	
		ItemStack itemStack = InventoryUtils.getCopy(originalItemStack);
		if (itemStack == null) {
			controller.getPlugin().getLogger().warning("Unable to create material icon for " + material.name() + ": " + originalItemStack.getType());	
			return originalItemStack;
		}
		ItemMeta meta = itemStack.getItemMeta();
		if (material == EraseMaterial) {
			List<String> lore = new ArrayList<String>();
			lore.add(Messages.get("wand.erase_material_description"));
			meta.setLore(lore);
		} else if (material == CopyMaterial) {
			List<String> lore = new ArrayList<String>();
			lore.add(Messages.get("wand.copy_material_description"));
			meta.setLore(lore);
		} else if (material == CloneMaterial) {
			List<String> lore = new ArrayList<String>();
			lore.add(Messages.get("wand.clone_material_description"));
			meta.setLore(lore);
		} else if (material == ReplicateMaterial) {
			List<String> lore = new ArrayList<String>();
			lore.add(Messages.get("wand.replicate_material_description"));
			meta.setLore(lore);
		} else {
			List<String> lore = new ArrayList<String>();
			if (material != null) {
				lore.add(ChatColor.GRAY + getMaterialName(material, (byte)dataId));
			}
			lore.add(ChatColor.LIGHT_PURPLE + Messages.get("wand.building_material_description"));
			meta.setLore(lore);
		}
		meta.setDisplayName(getActiveWandName(material));
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
		InventoryUtils.setMeta(wandNode, "description", description);
		InventoryUtils.setMeta(wandNode, "owner", owner);
	
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
		InventoryUtils.setMeta(wandNode, "modifiable", Integer.toString((modifiable ? 1 : 0)));
		InventoryUtils.setMeta(wandNode, "effect_color", Integer.toString(effectColor, 16));
	}
	
	protected void loadState() {
		Object wandNode = InventoryUtils.getNode(item, "wand");
		if (wandNode == null) {
			controller.getPlugin().getLogger().warning("Found a wand with missing NBT data. This may be an old wand, or something may have wiped its data");
            return;
		}
		
		// Don't generate a UUID unless we need to, not sure how expensive that is.
		id = InventoryUtils.getMeta(wandNode, "id");
		id = id == null || id.length() == 0 ? UUID.randomUUID().toString() : id;
		wandName = InventoryUtils.getMeta(wandNode, "name", wandName);
		description = InventoryUtils.getMeta(wandNode, "description", description);
		owner = InventoryUtils.getMeta(wandNode, "owner", owner);
		
		String wandMaterials = InventoryUtils.getMeta(wandNode, "materials", "");
		String wandSpells = InventoryUtils.getMeta(wandNode, "spells", "");
		parseInventoryStrings(wandSpells, wandMaterials);
		activeSpell = InventoryUtils.getMeta(wandNode, "active_spell", activeSpell);
		activeMaterial = InventoryUtils.getMeta(wandNode, "active_material", activeMaterial);
		
		// Temporary-ish migration step
		activeMaterial = getMaterialKey(parseMaterialKey(activeMaterial));
		
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
		modifiable = Integer.parseInt(InventoryUtils.getMeta(wandNode, "modifiable", (modifiable ? "1" : "0"))) != 0;
		effectColor = Integer.parseInt(InventoryUtils.getMeta(wandNode, "effect_color", Integer.toString(effectColor, 16)), 16);
		
		// This is done here as an extra safety measure.
		// A walk speed too high will cause a server error.
		speedIncrease = Math.min(WandLevel.maxSpeedIncrease, speedIncrease);
	}

	public void describe(CommandSender sender) {
		Object wandNode = InventoryUtils.getNode(item, "wand");
		if (wandNode == null) {
			sender.sendMessage("Found a wand with missing NBT data. This may be an old wand, or something may have wiped its data");
            return;
		}
		ChatColor wandColor = modifiable ? ChatColor.AQUA : ChatColor.RED;
		sender.sendMessage(wandColor + wandName);
		if (description.length() > 0) {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.GREEN + description);
		} else {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.GREEN + "(No Description)");
		}
		if (owner.length() > 0) {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.WHITE + owner);
		} else {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.WHITE + "(No Owner)");
		}
		
		String[] keys = {"active_spell", "active_material", "xp", "xp_regeneration", "xp_max", "health_regeneration", 
					"hunger_regeneration", "uses", 
					"cost_reduction", "cooldown_reduction", "power", "protection", "protection_physical", 
					"protection_projectiles", "protection_falling", "protection_fire", "protection_explosions", 
					"haste", "has_inventory", "modifiable", "effect_color", "materials", "spells"};
		
		for (String key : keys) {
			String value = InventoryUtils.getMeta(wandNode, key);
			if (value != null && value.length() > 0) {
				sender.sendMessage(key + ": " + value);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public boolean removeMaterial(String materialKey) {
		if (!modifiable || materialKey == null) return false;
		
		if (isInventoryOpen()) {
			saveInventory();
		}
		if (materialKey.equals(activeMaterial)) {
			activeMaterial = null;
		}
		MaterialAndData materialData = parseMaterialKey(materialKey);
		
		List<Inventory> allInventories = getAllInventories();
		boolean found = false;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int index = 0; index < items.length; index++) {
				ItemStack itemStack = items[index];
				if (itemStack != null && itemStack.getType() != Material.AIR && !isWand(itemStack) && !isSpell(itemStack)) {
					if (itemStack.getType() == materialData.getMaterial() && materialData.getData() == itemStack.getData().getData()) {
						found = true;
						inventory.setItem(index, null);
					} else if (activeMaterial == null) {
						activeMaterial = materialKey;
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
		return found;
	}
	
	public boolean removeMaterial(Material material, byte data) {
		return removeMaterial(getMaterialKey(material, data));
	}
	
	public boolean addMaterial(Material material, byte data, boolean force) {
		return addMaterial(material, data, false, force);
	}
	
	public boolean addMaterial(Material material, byte data) {
		return addMaterial(material, data, false, false);
	}

	public boolean hasMaterial(String materialKey) {
		return getMaterialKeys().contains(materialKey);
	}
	
	public boolean hasSpell(String spellName) {
		return getSpells().contains(spellName);
	}
	
	public boolean addMaterial(String materialKey, boolean makeActive, boolean force) {
		if (!modifiable && !force) return false;
		
		boolean addedNew = !hasMaterial(materialKey);
		if (addedNew) {
			addToInventory(createMaterialItem(materialKey));
		}
		if (activeMaterial == null || activeMaterial.length() == 0 || makeActive) {
			activeMaterial = materialKey;
		}
		updateActiveMaterial();
		updateInventoryNames(true);
		updateName();
		updateLore();
		saveState();
		if (isInventoryOpen()) {
			updateInventory();
		}
		hasInventory = getSpells().size() + getMaterialKeys().size() > 1;
		
		return addedNew;
	}
	
	public boolean addMaterial(Material material, byte data, boolean makeActive, boolean force) {
		if (!modifiable && !force) return false;
		
		if (isInventoryOpen()) {
			saveInventory();
		}
		String materialKey = getMaterialKey(material, data);
		return addMaterial(materialKey, makeActive, force);
	}
	
	public boolean removeSpell(String spellName) {
		if (!modifiable) return false;
		
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
		return found;
	}
	
	public boolean addSpell(String spellName, boolean makeActive) {
		if (!modifiable) return false;
		
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
		hasInventory = getSpells().size() + getMaterialKeys().size() > 1;
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
		return addSpell(spellName, false);
	}

	private String getActiveWandName(Spell spell, String materialName) {
		// Build wand name
		ChatColor wandColor = modifiable ? ChatColor.AQUA : ChatColor.RED;
		String name = wandColor + wandName;
		
		// Add active spell to description
		if (spell != null) {
			if (materialName != null) {
				materialName = materialName.replace('_', ' ');
				name = ChatColor.GOLD + spell.getName() + ChatColor.GRAY + " " + materialName + ChatColor.WHITE + " (" + wandColor + wandName + ChatColor.WHITE + ")";
			} else {
				name = ChatColor.GOLD + spell.getName() + ChatColor.WHITE + " (" + wandColor + wandName + ChatColor.WHITE + ")";
			}
		}
		int remaining = getRemainingUses();
		if (remaining > 0) {
			name = name + " : " + ChatColor.RED + Messages.get("wand.uses_remaining_brief").replace("$count", ((Integer)remaining).toString());
		}
		return name;
	}
	
	private String getActiveWandName(Spell spell) {
		String[] pieces = StringUtils.split(activeMaterial, ":");
		String materialName = null;
		
		if (spell != null && spell.usesBrush() && !spell.hasBrushOverride() && pieces.length > 0 && pieces[0].length() > 0) {
			materialName = activeMaterial;
		}
		return getActiveWandName(spell, materialName);
	}
	
	private static String getMaterialKey(Material material) {
		String materialKey = null;

		if (material == EraseMaterial) {
			materialKey = "erase";
		} else if (material == CopyMaterial) {
			materialKey = "copy";
		} else if (material == CloneMaterial) {
			materialKey = "clone";
		} else if (material == ReplicateMaterial) {
			materialKey = "replicate";
		} else {
			materialKey = material.name().toLowerCase();
		}
		
		return materialKey;
	}

	private static String getMaterialKey(Material material, byte data) {
		String materialKey = getMaterialKey(material);
		if (data != 0) {
			materialKey += ":" + data;
		}
		
		return materialKey;
	}

	private static String getMaterialKey(MaterialAndData material) {
		return getMaterialKey(material.getMaterial(), material.getData());
	}
	
	private static String getMaterialName(Material material) {
		return getMaterialName(material, (byte)0);
	}
	
	@SuppressWarnings("deprecation")
	private static String getMaterialName(Material material, byte data) {
		String materialName = getMaterialKey(material, data);
		
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
		
		materialName = materialName.replace('_', ' ');
		return materialName;
	}
	
	private String getActiveWandName(Material material) {
		Spell spell = controller.getSpell(activeSpell);
		String materialName = null;
		
		if (spell != null && spell.usesBrush() && !spell.hasBrushOverride() && material != null) {
			materialName = getMaterialName(material);
		}
		return getActiveWandName(spell, materialName);
	}
	
	private String getActiveWandName() {
		Spell spell = null;
		if (hasInventory) {
			spell = controller.getSpell(activeSpell);
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
		String suffix = "";

		if (amount >= 1) {
			suffix = Messages.get("wand.enchantment_level_max");
		} else if (amount > 0.8) {
			suffix = Messages.get("wand.enchantment_level_5");
		} else if (amount > 0.6) {
			suffix = Messages.get("wand.enchantment_level_4");
		} else if (amount > 0.4) {
			suffix = Messages.get("wand.enchantment_level_3");
		} else if (amount > 0.2) {
			suffix = Messages.get("wand.enchantment_level_2");
		} else {
			 suffix = Messages.get("wand.enchantment_level_1");
		}
		return prefix + " " + suffix;
	}
	
	protected static String convertToHTML(String line) {
		int tagCount = 1;
		line = "<span style=\"color:white\">" + line;
		for (ChatColor c : ChatColor.values()) {
			tagCount += StringUtils.countMatches(line, c.toString());
			String replaceStyle = "";
			if (c == ChatColor.ITALIC) {
				replaceStyle = "font-style: italic";
			} else if (c == ChatColor.BOLD) {
				replaceStyle = "font-weight: bold";
			} else if (c == ChatColor.UNDERLINE) {
				replaceStyle = "text-decoration: underline";
			} else {
				String color = c.name().toLowerCase().replace("_", "");
				if (c == ChatColor.LIGHT_PURPLE) {
					color = "mediumpurple";
				}
				replaceStyle = "color:" + color;
			}
			line = line.replace(c.toString(), "<span style=\"" + replaceStyle + "\">");
		}
		for (int i = 0; i < tagCount; i++) {
			line += "</span>";
		}
		
		return line;
	}

	public String getHTMLDescription() {
		Collection<String> rawLore = getLore();
		Collection<String> lore = new ArrayList<String>();
		lore.add("<h2>" + convertToHTML(getActiveWandName()) + "</h2>");
 		for (String line : rawLore) {
			lore.add(convertToHTML(line));
		}
		
		return "<div style=\"background-color: black; margin: 8px; padding: 8px\">" + StringUtils.join(lore, "<br/>") + "</div>";
	}

	private List<String> getLore() {
		return getLore(getSpells().size(), getMaterialKeys().size());
	}
	
	private List<String> getLore(int spellCount, int materialCount) {
		List<String> lore = new ArrayList<String>();
		
		Spell spell = controller.getSpell(activeSpell);
		if (spell != null && spellCount == 1 && materialCount <= 1) {
			addSpellLore(spell, lore);
		} else {
			if (description.length() > 0) {
				lore.add(ChatColor.ITALIC + "" + ChatColor.GREEN + description);
			}
			
			lore.add(Messages.get("wand.spell_count").replace("$count", ((Integer)spellCount).toString()));
			if (materialCount > 0) {
				lore.add(Messages.get("wand.material_count").replace("$count", ((Integer)materialCount).toString()));
			}
		}
		int remaining = getRemainingUses();
		if (remaining > 0) {
			lore.add(ChatColor.RED + Messages.get("wand.uses_remaining").replace("$count", ((Integer)remaining).toString()));
		}
		if (xpRegeneration > 0) {
			lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + Messages.get("wand.mana_amount").replace("$amount", ((Integer)xpMax).toString()));
			lore.add(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + getLevelString(Messages.get("wand.mana_regeneration"), xpRegeneration / WandLevel.maxXpRegeneration));
		}
		if (costReduction > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.cost_reduction"), costReduction));
		if (cooldownReduction > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.cooldown_reduction"), cooldownReduction));
		if (power > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.power"), power));
		if (speedIncrease > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.haste"), speedIncrease / WandLevel.maxSpeedIncrease));
		if (damageReduction > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.protection"), damageReduction));
		if (damageReduction < 1) {
			if (damageReductionPhysical > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.protection_physical"), damageReductionPhysical));
			if (damageReductionProjectiles > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.protection_projectile"), damageReductionProjectiles));
			if (damageReductionFalling > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.protection_fall"), damageReductionFalling));
			if (damageReductionFire > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.protection_fire"), damageReductionFire));
			if (damageReductionExplosions > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.protection_blast"), damageReductionExplosions));
		}
		if (healthRegeneration > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.health_regeneration"), healthRegeneration / WandLevel.maxRegeneration));
		if (hungerRegeneration > 0) lore.add(ChatColor.AQUA + getLevelString(Messages.get("wand.hunger_regeneration"), hungerRegeneration / WandLevel.maxRegeneration));
		return lore;
	}
	
	private void updateLore() {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = getLore();
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
	
	public static Wand getActiveWand(MagicController spells, Player player) {
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
	
	public void updateInventoryNames(boolean activeHotbarNames, boolean activeAllNames) {
		if (mage == null || !isInventoryOpen()) return;
		
		ItemStack[] contents = mage.getPlayer().getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || item.getType() == Material.AIR || isWand(item)) continue;
			boolean activeName = activeAllNames || (activeHotbarNames && i < Wand.hotbarSize);
			updateInventoryName(item, activeName);
		}
	}
	
	public void updateInventoryNames(boolean activeHotbarNames) {
		updateInventoryNames(activeHotbarNames, false);
	}

	protected void updateInventoryName(ItemStack item, boolean activeName) {
		if (isSpell(item)) {
			Spell spell = mage.getSpell(getSpell(item));
			if (spell != null) {
				updateSpellName(item, spell, activeName);
			}
		} else {
			updateMaterialName(item, activeName);
		}
	}
	
	protected void updateSpellName(ItemStack itemStack, Spell spell, boolean activeName) {
		ItemMeta meta = itemStack.getItemMeta();
		String displayName = null;
		if (activeName) {
			displayName = getActiveWandName(spell);
		} else {
			displayName = ChatColor.GOLD + spell.getName();
		}
		meta.setDisplayName(displayName);
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
		if (mage == null) return;
		if (!isInventoryOpen()) return;
		if (mage.getPlayer() == null) return;
		if (!mage.hasStoredInventory()) return;
		
		// Clear the player's inventory
		Player player = mage.getPlayer();
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
		String description = spell.getDescription();
		String usage = spell.getUsage();
		if (description != null && description.length() > 0) {
			lore.add(description);
		}
		if (usage != null && usage.length() > 0) {
			lore.add(usage);
		}
		List<CastingCost> costs = spell.getCosts();
		if (costs != null) {
			for (CastingCost cost : costs) {
				if (cost.hasCosts(this)) {
					lore.add(ChatColor.YELLOW + Messages.get("wand.costs_description").replace("$description", cost.getFullDescription(this)));
				}
			}
		}
	}
	
	protected Inventory getOpenInventory() {
		while (openInventoryPage >= inventories.size()) {
			inventories.add(InventoryUtils.createInventory(null, inventorySize, "Wand"));
		}
		return inventories.get(openInventoryPage);
	}
	
	public void saveInventory() {
		if (mage == null) return;
		if (mage == null) return;
		if (!isInventoryOpen()) return;
		if (mage.getPlayer() == null) return;
		if (!mage.hasStoredInventory()) return;
		
		// Fill in the hotbar
		Player player = mage.getPlayer();
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
		if (!additive) {
			wandName = Messages.get("wands.random.name", wandName);
		}
		WandLevel.randomizeWand(this, additive, level);
	}
	
	public static Wand createWand(MagicController spells, String templateName) {
		Wand wand = new Wand(spells);
		String wandName = Messages.get("wand.default_name");
		String wandDescription = "";

		// Check for default wand
		if ((templateName == null || templateName.length() == 0) && wandTemplates.containsKey("default"))
		{
			templateName = "default";
		}
		
		// See if there is a template with this key
		if (templateName != null && templateName.length() > 0) {
			if ((templateName.equals("random") || templateName.startsWith("random(")) && wandTemplates.containsKey("random")) {
				int level = 1;
				if (!templateName.equals("random")) {
					String randomLevel = templateName.substring(templateName.indexOf('(') + 1, templateName.length() - 1);
					level = Integer.parseInt(randomLevel);
				}
				ConfigurationNode randomTemplate = wandTemplates.get("random");
				wand.modifiable = (boolean)randomTemplate.getBoolean("modifiable", true);
				wand.randomize(level, false);
				return wand;
			}
			
			if (!wandTemplates.containsKey(templateName)) {
				return null;
			}
			ConfigurationNode wandConfig = wandTemplates.get(templateName);
			wandName = Messages.get("wands." + templateName + ".name", wandName);
			wandDescription = Messages.get("wands." + templateName + ".description", wandDescription);
			List<Object> spellList = wandConfig.getList("spells");
			if (spellList != null) {
				for (Object spellName : spellList) {			
					wand.addSpell((String)spellName);
				}
			}
			List<Object> materialList = wandConfig.getList("materials");
			if (materialList != null) {
				for (Object materialKey : materialList) {
					if (!isValidMaterial((String)materialKey)) {
						spells.getPlugin().getLogger().info("Unknown material: " + materialKey);
					} else {
						wand.addMaterial((String)materialKey, false, true);
					}
				}
			}
			
			wand.configureProperties(wandConfig);
			
			if (wandConfig.getBoolean("organize", false)) {
				wand.organizeInventory();
			}
		}

		wand.setDescription(wandDescription);
		wand.setName(wandName);
		
		return wand;
	}
	
	public void add(Wand other) {
		if (!modifiable || !other.modifiable) return;
		
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
		// Mix colors?
		if (effectColor == 0) {
			effectColor = other.effectColor;
		} else if (other.effectColor != 0){
			Color color1 = Color.fromBGR(effectColor);
			Color color2 = Color.fromBGR(other.effectColor);
			Color newColor = color1.mixColors(color2);
			effectColor = newColor.asRGB();
		}
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
		Set<String> materials = other.getMaterialKeys();
		for (String material : materials) {
			addMaterial(material, false, true);
		}

		saveState();
		updateName();
		updateLore();
	}
	
	public void configureProperties(ConfigurationNode wandConfig) {
		configureProperties(wandConfig, false);
	}
	
	public void configureProperties(ConfigurationNode wandConfig, boolean safe) {
		modifiable = (boolean)wandConfig.getBoolean("modifiable", modifiable);
		float _costReduction = (float)wandConfig.getDouble("cost_reduction", costReduction);
		costReduction = safe ? Math.max(_costReduction, costReduction) : _costReduction;
		float _cooldownReduction = (float)wandConfig.getDouble("cooldown_reduction", cooldownReduction);
		cooldownReduction = safe ? Math.max(_cooldownReduction, cooldownReduction) : _cooldownReduction;
		float _power = (float)wandConfig.getDouble("power", power);
		power = safe ? Math.max(_power, power) : _power;
		float _damageReduction = (float)wandConfig.getDouble("protection", damageReduction);
		damageReduction = safe ? Math.max(_damageReduction, damageReduction) : _damageReduction;
		float _damageReductionPhysical = (float)wandConfig.getDouble("protection_physical", damageReductionPhysical);
		damageReductionPhysical = safe ? Math.max(_damageReductionPhysical, damageReductionPhysical) : _damageReductionPhysical;
		float _damageReductionProjectiles = (float)wandConfig.getDouble("protection_projectiles", damageReductionProjectiles);
		damageReductionProjectiles = safe ? Math.max(_damageReductionProjectiles, damageReductionPhysical) : _damageReductionProjectiles;
		float _damageReductionFalling = (float)wandConfig.getDouble("protection_falling", damageReductionFalling);
		damageReductionFalling = safe ? Math.max(_damageReductionFalling, damageReductionFalling) : _damageReductionFalling;
		float _damageReductionFire = (float)wandConfig.getDouble("protection_fire", damageReductionFire);
		damageReductionFire = safe ? Math.max(_damageReductionFire, damageReductionFire) : _damageReductionFire;
		float _damageReductionExplosions = (float)wandConfig.getDouble("protection_explosions", damageReductionExplosions);
		damageReductionExplosions = safe ? Math.max(_damageReductionExplosions, damageReductionExplosions) : _damageReductionExplosions;
		int _xpRegeneration = wandConfig.getInt("xp_regeneration", xpRegeneration);
		xpRegeneration = safe ? Math.max(_xpRegeneration, xpRegeneration) : _xpRegeneration;
		int _xpMax = wandConfig.getInt("xp_max", xpMax);
		xpMax = safe ? Math.max(_xpMax, xpMax) : _xpMax;
		int _xp = wandConfig.getInt("xp", xp);
		xp = safe ? Math.max(_xp, xp) : _xp;
		int _healthRegeneration = wandConfig.getInt("health_regeneration", healthRegeneration);
		healthRegeneration = safe ? Math.max(_healthRegeneration, healthRegeneration) : _healthRegeneration;
		int _hungerRegeneration = wandConfig.getInt("hunger_regeneration", hungerRegeneration);
		hungerRegeneration = safe ? Math.max(_hungerRegeneration, hungerRegeneration) : _hungerRegeneration;
		int _uses = wandConfig.getInt("uses", uses);
		uses = safe ? Math.max(_uses, uses) : _uses;
		
		effectColor = Integer.parseInt(wandConfig.getString("effect_color", "0"), 16);
	
		// Make sure to adjust the player's walk speed if it changes and this wand is active.
		float oldWalkSpeedIncrease = speedIncrease;
		speedIncrease = (float)wandConfig.getDouble("haste", speedIncrease);
		if (mage != null && speedIncrease != oldWalkSpeedIncrease) {
			Player player = mage.getPlayer();
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
		// Load properties file with wand templates
		File dataFolder = plugin.getDataFolder();
		File oldDefaults = new File(dataFolder, propertiesFileNameDefaults);
		oldDefaults.delete();
		plugin.getLogger().info("Overwriting file " + propertiesFileNameDefaults);
		plugin.saveResource(propertiesFileNameDefaults, false);
		File propertiesFile = new File(dataFolder, propertiesFileName);
		if (!propertiesFile.exists())
		{
			plugin.getLogger().info("Loading default wands from " + propertiesFileNameDefaults);
			loadProperties(plugin.getResource(propertiesFileNameDefaults));
		} else {
			plugin.getLogger().info("Loading wands from " + propertiesFile.getName());
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
	
	public static Collection<String> getWandKeys() {
		return wandTemplates.keySet();
	}
	
	public static Collection<ConfigurationNode> getWandTemplates() {
		return wandTemplates.values();
	}
	
	@SuppressWarnings("deprecation")
	public static MaterialAndData parseMaterialKey(String materialKey) {
		Material material = Material.DIRT;
		byte data = 0;
		
		if (materialKey.equals(ERASE_MATERIAL_KEY)) {
			material = EraseMaterial;
		} else if (materialKey.equals(COPY_MATERIAL_KEY)) {
			material = CopyMaterial;
		} else if (materialKey.equals(CLONE_MATERIAL_KEY)) {
			material = CloneMaterial;
		} else if (materialKey.equals(REPLICATE_MATERIAL_KEY)) {
			material = ReplicateMaterial;
		} else {
			String[] pieces = StringUtils.split(materialKey, ":");
			
			try {
				if (pieces.length > 0) {
					// Legacy material id loading
					try {
						Integer id = Integer.parseInt(pieces[0]);
						if (id == ERASE_MATERIAL_ID) {
							material = EraseMaterial;
						} else if (id == COPY_MATERIAL_ID) {
							material = CopyMaterial;
						} else if (id == CLONE_MATERIAL_ID) {
							material = CloneMaterial;
						} else {
							material = Material.getMaterial(id);
						}
					} catch (Exception ex) {
						material = Material.getMaterial(pieces[0].toUpperCase());
					}
				}
			} catch (Exception ex) {
				material = null;
			}
			try {
				if (pieces.length > 1) {
					data = Byte.parseByte(pieces[1]);
				}
			} catch (Exception ex) {
				data = 0;
			}
		}
		if (material == null) return null;
			
		return new MaterialAndData(material, data);
	}
	
	public static boolean isValidMaterial(String materialKey) {
		return parseMaterialKey(materialKey) != null;
	}

	private void updateActiveMaterial() {
		if (mage == null) return;
		
		if (activeMaterial == null) {
			mage.clearBuildingMaterial();
		} else {
			MaterialBrush brush = mage.getBrush();
			if (activeMaterial.equals(COPY_MATERIAL_KEY)) {
				brush.enableCopying();
			} else if (activeMaterial.equals(CLONE_MATERIAL_KEY)) {
				brush.enableCloning();
			} else if (activeMaterial.equals(REPLICATE_MATERIAL_KEY)) {
				brush.enableReplication();
			} else {
				MaterialAndData material = parseMaterialKey(activeMaterial);
				brush.setMaterial(material.getMaterial(), material.getData());
			}
		}
	}
	
	public void toggleInventory() {
		if (!hasInventory) {
			return;
		}
		if (!isInventoryOpen()) {
			openInventory();
		} else {
			closeInventory();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void cycleInventory() {
		if (!hasInventory) {
			return;
		}
		if (isInventoryOpen()) {
			saveInventory();
			openInventoryPage = (openInventoryPage + 1) % inventories.size();
			updateInventory();
			if (mage != null && inventories.size() > 1) {
				mage.playSound(Sound.CHEST_OPEN, 0.3f, 1.5f);
				mage.getPlayer().updateInventory();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void openInventory() {
		if (mage == null) return;
		if (mage.hasStoredInventory()) return;
		if (mage.storeInventory()) {
			inventoryIsOpen = true;
			mage.playSound(Sound.CHEST_OPEN, 0.4f, 0.2f);
			updateInventory();
			mage.getPlayer().updateInventory();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void closeInventory() {
		if (!isInventoryOpen()) return;
		saveInventory();
		inventoryIsOpen = false;
		if (mage != null) {
			mage.playSound(Sound.CHEST_CLOSE, 0.4f, 0.2f);
			mage.restoreInventory();
			mage.getPlayer().updateInventory();
			ItemStack newWandItem = mage.getPlayer().getInventory().getItemInHand();
			if (isWand(newWandItem)) {
				item = newWandItem;
				updateName();
			}
		}
		saveState();
	}
	
	public void activate(Mage playerSpells) {
		if (owner.length() == 0) {
			takeOwnership(playerSpells.getPlayer());
		}
		mage = playerSpells;
		Player player = mage.getPlayer();
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
		mage.setActiveWand(this);
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
		if (mage != null && xpMax > 0 && xpRegeneration > 0) {
			Player player = mage.getPlayer();
			player.setLevel(0);
			player.setExp((float)xp / (float)xpMax);
		}
	}
	
	public boolean isInventoryOpen() {
		return mage != null && inventoryIsOpen;
	}
	
	public void deactivate() {
		if (mage == null) return;
		saveState();

		if (effectColor > 0) {
			InventoryUtils.removePotionEffect(mage.getPlayer());
		}
		
		// This is a tying wands together with other spells, potentially
		// But with the way the mana system works, this seems like the safest route.
		mage.deactivateAllSpells();
		
		if (isInventoryOpen()) {
			closeInventory();
		}
		
		// Extra just-in-case
		mage.restoreInventory();
		
		if (xpRegeneration > 0) {
			mage.getPlayer().setExp(storedXpProgress);
			mage.getPlayer().setLevel(storedXpLevel);
			mage.getPlayer().giveExp(storedXp);
			storedXp = 0;
			storedXpProgress = 0;
			storedXpLevel = 0;
		}
		if (speedIncrease > 0) {
			try {
				mage.getPlayer().setWalkSpeed(defaultWalkSpeed);
				mage.getPlayer().setFlySpeed(defaultFlySpeed);
			}  catch(Exception ex) {
				
			}
		}
		mage.setActiveWand(null);
		mage = null;
	}
	
	public Spell getActiveSpell() {
		if (mage == null) return null;
		return mage.getSpell(activeSpell);
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
		if (mage == null) return;
		if (uses > 0) {
			uses--;
			if (uses <= 0) {
				Player player = mage.getPlayer();
				mage.playSound(Sound.ITEM_BREAK, 1.0f, 0.8f);
				PlayerInventory playerInventory = player.getInventory();
				playerInventory.setItemInHand(new ItemStack(Material.AIR, 1));
				player.updateInventory();
				deactivate();
			} else {
				updateName();
				updateLore();
				saveState();
			}
		}
	}
	
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		if (mage == null) return;
		
		if (xpRegeneration > 0) {
			storedXp += event.getAmount();
			event.setAmount(0);
		}
	}
	
	public void processRegeneration() {
		if (mage == null) return;
		
		Player player = mage.getPlayer();
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
	
	public MagicController getMaster() {
		return controller;
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
		Set<String> materialsSet = getMaterialKeys();
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
	
	public void organizeInventory() {
		WandOrganizer organizer = new WandOrganizer(this);
		organizer.organize();
		openInventoryPage = 0;
		saveState();
	}
	
	public Mage getActivePlayer() {
		return mage;
	}
	
	public String getId() {
		return this.id;
	}
	
	protected void clearInventories() {
		inventories.clear();
	}
	
	public int getEffectColor() {
		return effectColor;
	}
	
	public Inventory getHotbar() {
		return hotbar;
	}
}
