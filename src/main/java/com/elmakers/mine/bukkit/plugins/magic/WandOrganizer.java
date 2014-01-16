package com.elmakers.mine.bukkit.plugins.magic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WandOrganizer {
	private final Wand wand;

	protected final static int inventoryOrganizeSize = 23;
	protected final static int inventoryOrganizeNewGroupSize = 16;

	private int currentInventoryIndex = 0;
	private int currentInventoryCount = 0;
	private Inventory currentInventory = null;
	
	public WandOrganizer(Wand wand) {
		this.wand = wand;
	}

	protected void nextInventory() {
		currentInventoryIndex++;
		currentInventoryCount = 0;
		currentInventory = wand.getInventoryByIndex(currentInventoryIndex);
	}
	
	protected void addToInventory(ItemStack itemStack) {
		// Advance when almost full
		if (currentInventoryCount > inventoryOrganizeSize) {
			nextInventory();
		}
		
		// Start a new inventory if this one fills up, or rejects an item
		HashMap<Integer, ItemStack> result = currentInventory.addItem(itemStack);
		
		if (result.size() > 0) {
			nextInventory();
			for (ItemStack skipped : result.values()) {
				currentInventory.addItem(skipped);
				currentInventoryCount++;
			}
		} else {
			currentInventoryCount++;
		}
	}
	
	public void organize() {
		// First collect spells in hotbar
		Set<String> hotbarSpellNames = new HashSet<String>();
		Set<String> hotbarMaterialNames = new HashSet<String>();

		Inventory hotbar = wand.getHotbar();
		int hotbarSize = Wand.hotbarSize;
		for (int i = 0; i < hotbarSize; i++) {
			ItemStack hotbarItem = hotbar.getItem(i);
			if (hotbarItem == null || hotbarItem.getType() == Material.AIR) continue;
			
			String spellName = Wand.getSpell(hotbarItem);
			if (spellName != null) {
				hotbarSpellNames.add(spellName);
			} else {
				String materialKey = Wand.getMaterialKey(hotbarItem);
				if (materialKey != null) {
					hotbarMaterialNames.add(materialKey);
				}
			}
		}
		
		MagicController master = wand.getMaster();
		Map<String, Collection<String>> groupedSpells = new TreeMap<String, Collection<String>>();
		Set<String> spells = wand.getSpells();
		for (String spellName : spells) {
			Spell spell = master.getSpell(spellName);
			if (spell != null && !hotbarSpellNames.contains(spellName)) {
				String category = spell.getCategory();
				if (category == null || category.length() == 0) {
					category = "default";
				}
				Collection<String> spellList = groupedSpells.get(category);
				if (spellList == null) {
					spellList = new TreeSet<String>();
					groupedSpells.put(category, spellList);
				}
				spellList.add(spellName);
			}
		}
		
		Set<String> wandMaterials = wand.getMaterialNames();
		for (String hotbarItemName : hotbarMaterialNames) {
			wandMaterials.remove(hotbarItemName);
		}
		Set<String> materials = new TreeSet<String>();
		materials.addAll(wandMaterials);
		
		wand.clearInventories();
		currentInventoryIndex = 0;
		currentInventoryCount = 0;
		currentInventory = wand.getInventoryByIndex(currentInventoryIndex);
		
		for (Collection<String> spellGroup : groupedSpells.values()) {
		
			// Start a new inventory for a new group if the previous inventory is over 2/3 full
			if (currentInventoryCount > inventoryOrganizeNewGroupSize) {
				nextInventory();
			}
			
			for (String spellName : spellGroup) {
				addToInventory(wand.createSpellItem(spellName));
			}
		}
		
		if (materials.size() > 0) {
			nextInventory();
			
			for (String materialName : materials) {
				addToInventory(wand.createMaterialItem(materialName));
			}
		}
	}
}
