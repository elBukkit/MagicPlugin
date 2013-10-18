package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public class Wand {
	private ItemStack item;
	
	private static Material WandMaterial = Material.STICK;
	
	public Wand() {
		item = new ItemStack(WandMaterial);
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		item = InventoryUtils.getCopy(item);
		ItemMeta itemMeta = item.getItemMeta();
		item.setItemMeta(itemMeta);

		InventoryUtils.addGlow(item);
		InventoryUtils.setMeta(item, "magic_wand", "");
	}
	
	public Wand(ItemStack item) {
		this.item = item;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public void removeMateriall(Material material, byte data) {
		Integer id = material.getId();
		String materialString = id.toString();
		if (data > 0) {
			materialString += ":" + data;
		}

		String[] materials = getMaterials();
		Set<String> materialMap = new TreeSet<String>();
		for (int i = 0; i < materials.length; i++) {
			materialMap.add(materials[i]);
		}
		materialMap.remove(materialString);
		setMaterials(materialMap);
	}
	
	@SuppressWarnings("deprecation")
	public void addMaterial(Material material, byte data) {
		Integer id = material.getId();
		String materialString = id.toString();
		if (data > 0) {
			materialString += ":" + data;
		}

		String[] materials = getMaterials();
		Set<String> materialMap = new TreeSet<String>();
		for (int i = 0; i < materials.length; i++) {
			materialMap.add(materials[i]);
		}
		materialMap.add(materialString);
		setMaterials(materialMap);
	}
	
	protected void setMaterials(Collection<String> materialNames) {
		String spellString = StringUtils.join(materialNames, "|");

		// Set new spells count
		setMaterialCount(materialNames.size());

		// Set new spells string
		InventoryUtils.setMeta(item, "magic_materials", spellString);
	}
	
	public String[] getMaterials() {
		String materialsString = InventoryUtils.getMeta(item, "magic_materials");
		if (materialsString == null) materialsString = "";

		return StringUtils.split(materialsString, "|");
	}
	
	public void addSpells(Collection<String> spellNames) {
		String[] spells = getSpells();
		Set<String> spellMap = new TreeSet<String>();
		for (String spell : spells) {
			spellMap.add(spell);
		}
		for (String spellName : spellNames) { 	
			spellMap.add(spellName);
		}
				
		setSpells(spellMap);
	}
	
	public String[] getSpells() {
		String spellString = InventoryUtils.getMeta(item, "magic_spells");
		if (spellString == null) spellString = "";

		return StringUtils.split(spellString, "|");
	}

	public void removeSpell(String spellName) {
		String[] spells = getSpells();
		Set<String> spellMap = new TreeSet<String>();
		for (int i = 0; i < spells.length; i++) {
			spellMap.add(spells[i]);
		}
		spellMap.remove(spellName);
		setSpells(spellMap);
	}
	
	public void addSpell(String spellName) {
		List<String> names = new ArrayList<String>();
		names.add(spellName);
		addSpells(names);
	}
	
	public void setSpells(Collection<String> spellNames) {
		String spellString = StringUtils.join(spellNames, "|");

		// Set new spells count
		setSpellCount(spellNames.size());

		// Set new spells string
		InventoryUtils.setMeta(item, "magic_spells", spellString);
	}
	
	public void setSpellCount(int spellCount) {
		updateLore(spellCount, getMaterials().length);
	}
	
	public void setMaterialCount(int materialCount) {
		updateLore(getSpells().length, materialCount);
	}
	
	public void setName(String name) {
		String spellString = InventoryUtils.getMeta(item, "magic_spells");
		String wandString = InventoryUtils.getMeta(item, "magic_wand");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		
		// Reset Enchantment glow
		InventoryUtils.addGlow(item);

		// The all-important last step of restoring the spell list, something
		// the Anvil will blow away.
		InventoryUtils.setMeta(item, "magic_spells", spellString);
		InventoryUtils.setMeta(item, "magic_wand", wandString);
	}

	protected void updateLore(int spellCount, int materialCount) {
		String spellString = InventoryUtils.getMeta(item, "magic_spells");
		String wandString = InventoryUtils.getMeta(item, "magic_wand");
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add("Knows " + spellCount +" Spells");
		if (materialCount > 0) {
			lore.add("Has " + materialCount +" Materials");
		}
		lore.add("Left-click to cast active spell");
		lore.add("Right-click to cycle spells");
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		InventoryUtils.addGlow(item);

		// Reset spell list and wand config
		InventoryUtils.setMeta(item, "magic_spells", spellString);
		InventoryUtils.setMeta(item, "magic_wand", wandString);
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
		return item != null && item.getType() == Material.STICK && InventoryUtils.getMeta(item, "magic_wand") != null;
	}

	public static boolean isSpell(ItemStack item) {
		return item != null && item.getType() != Material.STICK && InventoryUtils.getMeta(item, "magic_spell") != null;
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
		String spellString = InventoryUtils.getMeta(item, "magic_spells");
		String[] spells = StringUtils.split(spellString, "|");

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
		String materialString = InventoryUtils.getMeta(item, "magic_materials");
		String[] materials = StringUtils.split(materialString, "|");

		unpositioned.clear();
		for (int i = 0; i < materials.length; i++) {
			String[] parts = StringUtils.split(materials[i], "@");
			String[] nameParts = StringUtils.split(parts[0], ":");
			int typeId = Integer.parseInt(nameParts[0]);
			int dataId = nameParts.length > 1 ? Integer.parseInt(nameParts[1]) : 0;
			
			ItemStack itemStack = new ItemStack(typeId, 1, (short)0, (byte)dataId);		
			itemStack = InventoryUtils.getCopy(itemStack);
			ItemMeta meta = itemStack.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add("Magic building material");
			meta.setLore(lore);
			itemStack.setItemMeta(meta);
			
			int slot = parts.length > 1 ? Integer.parseInt(parts[1]) : itemSlot;
			if (parts.length > 1 && slot != itemSlot) {
				inventory.setItem(slot, itemStack);
			} else {
				unpositioned.add(itemStack);
			}
		}
		
		for (ItemStack stack : unpositioned) {
			addMaterialToInventory(inventory, stack);
		}

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
			if (!isSpell(items[i])) continue;

			Material material = items[i].getType();
			Spell spell = playerSpells.getSpell(material);
			if (spell == null) {
				List<Material> buildingMaterials = playerSpells.getMaster().getBuildingMaterials();
				if (material != Material.AIR && buildingMaterials.contains(material)) {
					materialNames.add(material.getId() + ":" + material.getData() + "@" + i);
				}
			} else {
				spellNames.add(spell.getKey() + "@" + i);
			}
		}
		setSpells(spellNames);
		setMaterials(materialNames);
	}

	public static boolean isActive(Player player) {
		ItemStack activeItem = player.getInventory().getItemInHand();
		return isWand(activeItem);
	}
	
	public static Wand createWand(String templateName) {
		Wand wand = new Wand();
		List<String> defaultSpells = new ArrayList<String>();
		String defaultName = "Wand";

		// Hacky until we have a config file!
		if (templateName.equals("demo")) {
			defaultSpells.add("torch");
			defaultSpells.add("fling");
			defaultSpells.add("blink");
			defaultName = "Demo Wand";
		} else if (templateName.equals("engineer")) {
			defaultSpells.add("fill");
			defaultSpells.add("pillar");
			defaultSpells.add("bridge");
			defaultSpells.add("absorb");
			defaultName = "Engineering Wand";
		}
		
		wand.setName(defaultName);
		wand.addSpells(defaultSpells);
		
		return wand;
	}
}
