package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
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
	
	private static Material WandMaterial = Material.STICK;
	public static Material EraseMaterial = Material.SULPHUR;
	
	// Wand configurations
	protected static Map<String, ConfigurationNode> wandTemplates = new HashMap<String, ConfigurationNode>();
	private static final String propertiesFileName = "wands";
	private static final String propertiesFileNameDefaults = "wands.defaults.yml";
	
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
		saveState();
	}
	
	public Wand(ItemStack item) {
		this.item = item;
		loadState();
	}
	
	protected void saveState() {
		InventoryUtils.setMeta(item, "magic_wand", wandSettings);
		InventoryUtils.setMeta(item, "magic_materials", wandMaterials);
		InventoryUtils.setMeta(item, "magic_spells", wandSpells);
	}
	
	protected void loadState() {
		wandSettings = InventoryUtils.getMeta(item, "magic_wand");
		wandSettings = wandSettings == null ? "" : wandSettings;
		wandMaterials = InventoryUtils.getMeta(item, "magic_materials");
		wandMaterials = wandMaterials == null ? "" : wandMaterials;
		wandSpells = InventoryUtils.getMeta(item, "magic_spells");
		wandSpells = wandSpells == null ? "" : wandSpells;
	}
	
	public ItemStack getItem() {
		return item;
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
	
	public String getName() {
		return item.getItemMeta().getDisplayName();
	}
	
	public String getBaseName() {
		String name = getName();
		int beginningOfWand = name.indexOf("(");
		if (beginningOfWand > 0) {
			name = name.substring(beginningOfWand + 1, name.length() - 1);
		}
		return name;
	}
	
	public void updateActiveName(PlayerSpells playerSpells) {
		Player player = playerSpells.getPlayer();
		Spell spell = playerSpells.getMaster().getActiveSpell(player);
		ItemStack activeMaterial = player.getInventory().getItem(8);
		String baseName = getBaseName();
		String materialName = "";
		if (spell != null && activeMaterial != null && activeMaterial.getType() != Material.AIR && !isSpell(activeMaterial) && !isWand(activeMaterial)) {
			materialName = activeMaterial.getType() == Wand.EraseMaterial ? "erase" : activeMaterial.getType().name().toLowerCase();
			materialName = " : " + materialName;
 		}
		if (spell != null) {
			setName(spell.getName() + materialName + " (" + baseName + ")");
		} else {
			setName(getBaseName());
		}
	}
	
	public void setName(String name) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		
		// Reset Enchantment glow
		InventoryUtils.addGlow(item);

		// The all-important last step of restoring the meta state, something
		// the Anvil will blow away.
		saveState();
	}

	protected void updateLore(int spellCount, int materialCount) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add("Knows " + spellCount +" Spells");
		if (materialCount > 0) {
			lore.add("Has " + materialCount +" Materials");
		}
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		InventoryUtils.addGlow(item);

		// Reset spell list and wand config
		saveState();
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
		return item != null && item.getType() == Material.STICK && InventoryUtils.getMeta(item, "magic_wand").length() > 0;
	}

	public static boolean isSpell(ItemStack item) {
		return item != null && item.getType() != Material.STICK && InventoryUtils.getMeta(item, "magic_spell").length() > 0;
	}
	
	public void updateInventory(PlayerSpells playerSpells) {
		updateInventory(playerSpells, playerSpells.getPlayer().getInventory().getHeldItemSlot());
	}

	@SuppressWarnings("deprecation")
	protected void updateInventory(PlayerSpells playerSpells, int itemSlot) {
		Player player = playerSpells.getPlayer();
		Inventory inventory = player.getInventory();
		inventory.clear();
		inventory.setItem(itemSlot, item);
		String[] spells = StringUtils.split(wandSpells, "|");

		List<ItemStack> unpositioned = new ArrayList<ItemStack>();
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
			meta.setLore(lore);
			itemStack.setItemMeta(meta);
			InventoryUtils.addGlow(itemStack);
			InventoryUtils.setMeta(itemStack, "magic_spell", spells[i]);
			
			int slot = parts.length > 1 ? Integer.parseInt(parts[1]) : itemSlot;
			if (parts.length > 1 && slot != itemSlot) {
				inventory.setItem(slot, itemStack);
			} else {
				unpositioned.add(itemStack);
			}
		}
		
		for (ItemStack stack : unpositioned) {
			inventory.addItem(stack);
		}
		String[] materials = StringUtils.split(wandMaterials, "|");

		unpositioned.clear();
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
					unpositioned.add(itemStack);
				}
			}
		}
		
		// Put the new stuff first, then the mapped stuff
		if (eraseStack != null) {
			addMaterialToInventory(inventory, eraseStack);
		}
		
		for (ItemStack stack : unpositioned) {
			addMaterialToInventory(inventory, stack);
		}
		
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

		updateActiveName(playerSpells);
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
	public void saveInventory(PlayerSpells playerSpells) {
		PlayerInventory inventory = playerSpells.getPlayer().getInventory();
		
		// Rebuild spell inventory, save in wand.
		ItemStack[] items = inventory.getContents();
		List<String> spellNames = new ArrayList<String>();
		List<String> materialNames = new ArrayList<String>();
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) continue;
			Material material = items[i].getType();
			if (isSpell(items[i])) {
				Spell spell = playerSpells.getSpell(material);
				if (spell != null) {
					spellNames.add(spell.getKey() + "@" + i);
				}
			} else {
				List<Material> buildingMaterials = playerSpells.getMaster().getBuildingMaterials();
				if (material != Material.AIR && (buildingMaterials.contains(material) || material == EraseMaterial)) {
					String materialkey = (material == EraseMaterial) ? "0:0" : material.getId() + ":" + items[i].getData().getData();
					materialNames.add(materialkey + "@" + i);
				}
			}
		}
		setSpells(spellNames);
		setMaterials(materialNames);
		saveState();
		setName(getBaseName());
	}

	public static boolean isActive(Player player) {
		ItemStack activeItem = player.getInventory().getItemInHand();
		return isWand(activeItem);
	}
	
	public static Wand createWand(String templateName) {
		Wand wand = new Wand();
		List<String> defaultSpells = new ArrayList<String>();
		String defaultName = "Wand";

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
		}
		
		wand.setName(defaultName);
		wand.addSpells(defaultSpells);
		
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
}
