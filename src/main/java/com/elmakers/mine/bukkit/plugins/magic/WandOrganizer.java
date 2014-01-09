package com.elmakers.mine.bukkit.plugins.magic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class WandOrganizer {
	private final Wand wand;

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
		if (currentInventoryCount > currentInventory.getSize() - 3) {
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
		PlayerSpells activePlayer = wand.getActivePlayer();
		
		Player player = activePlayer.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		int hotbarSize = Wand.hotbarSize;
		for (int i = 0; i < hotbarSize; i++) {
			ItemStack playerItem = playerInventory.getItem(i);
			if (playerItem == null || playerItem.getType() == Material.AIR) continue;
			
			String spellName = Wand.getSpell(playerItem);
			if (spellName != null) {
				hotbarSpellNames.add(spellName);
			} else {
				String materialKey = Wand.getMaterialKey(playerItem);
				if (materialKey != null) {
					hotbarMaterialNames.add(materialKey);
				}
			}
		}
		
		Map<String, Collection<String>> groupedSpells = new HashMap<String, Collection<String>>();
		Set<String> spells = wand.getSpells();
		for (String spellName : spells) {
			Spell spell = activePlayer.getSpell(spellName);
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
		
		// Clear player's top inventory
		for (int i = hotbarSize; i < playerInventory.getSize(); i++) {
			playerInventory.setItem(i, null);
		}
		
		Set<String> materials = wand.getMaterialNames();
		for (String hotbar : hotbarMaterialNames) {
			materials.remove(hotbar);
		}
		
		wand.clearInventories();
		currentInventoryIndex = 0;
		currentInventoryCount = 0;
		currentInventory = wand.getInventoryByIndex(currentInventoryIndex);
		
		for (Collection<String> spellGroup : groupedSpells.values()) {
		
			// Start a new inventory for a new group if the previous inventory is over 2/3 full
			if (currentInventoryCount > currentInventory.getSize() * 2 / 3) {
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
