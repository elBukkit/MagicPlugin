package com.elmakers.mine.bukkit.wand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.magic.MagicController;

public class WandOrganizer {
	private final Wand wand;
	private final Mage mage;

	protected final static int inventoryOrganizeSize = 22;
	protected final static int inventoryOrganizeNewGroupSize = 16;
	protected final static int favoriteCastCountThreshold = 20;

	private int currentInventoryIndex = 0;
	private int currentInventoryCount = 0;
	private Inventory currentInventory = null;
	private boolean addToHotbar = true;
	
	public WandOrganizer(Wand wand, Mage mage) {
		this.wand = wand;
		this.mage = mage;
		addToHotbar = true;
	}

	protected void nextInventory() {
		currentInventoryIndex++;
		currentInventoryCount = 0;
		currentInventory = wand.getInventoryByIndex(currentInventoryIndex);
	}
	
	protected void addToInventory(ItemStack itemStack) {
		// First add to hotbar until almost full- leave one space for the wand.
		if (addToHotbar) {
			addToHotbar = false;
			Inventory hotbar = wand.getHotbar();
			for (int i = 0; i < Wand.HOTBAR_SIZE; i++) {
				ItemStack hotbarItem = hotbar.getItem(i);
				if (hotbarItem == null || hotbarItem.getType() == Material.AIR) {
					addToHotbar = true;
					hotbar.setItem(i, itemStack);
					return;
				}
			}
		}
		
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
		int hotbarSize = Wand.HOTBAR_SIZE;
		for (int i = 0; i < hotbarSize; i++) {
			ItemStack hotbarItem = hotbar.getItem(i);
			if (hotbarItem == null || hotbarItem.getType() == Material.AIR) continue;
			
			String spellName = Wand.getSpell(hotbarItem);
			if (spellName != null) {
				hotbarSpellNames.add(spellName);
			} else {
				String materialKey = Wand.getBrush(hotbarItem);
				if (materialKey != null) {
					hotbarMaterialNames.add(materialKey);
				}
			}
		}
		
		MagicController master = wand.getMaster();
		TreeMap<Long, List<String>> favoriteSpells = new TreeMap<Long, List<String>>();
		Map<String, Collection<String>> groupedSpells = new TreeMap<String, Collection<String>>();
		Set<String> spells = wand.getSpells();
		for (String spellName : spells) {
			Spell mageSpell = mage == null ? null : mage.getSpell(spellName);
			SpellTemplate spell = mageSpell == null ? master.getSpellTemplate(spellName) : mageSpell;
			if (spell != null && !hotbarSpellNames.contains(spellName)) {
				long castCount = mageSpell == null ? 0 : mageSpell.getCastCount();
				if (castCount > favoriteCastCountThreshold) {
					List<String> favorites = null;
					if (!favoriteSpells.containsKey(castCount)) {
						favorites = new ArrayList<String>();
						favoriteSpells.put(castCount, favorites);
					} else {
						favorites = favoriteSpells.get(castCount);
					}
					favorites.add(spellName);
					spell = null;
				}
				if (spell != null) {
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
		}
		
		Set<String> wandMaterials = wand.getBrushes();
		
		for (String hotbarItemName : hotbarMaterialNames) {
			wandMaterials.remove(hotbarItemName);
		}
		Map<String, String> materials = new TreeMap<String, String>();
		for (String materialKey : wandMaterials) {
			if (MaterialBrush.isSpecialMaterialKey(materialKey)) {
				materials.put(" " + materialKey, materialKey);
			} else {
				materials.put(materialKey, materialKey);
			}
		}
		
		wand.clearInventories();
		currentInventoryIndex = 0;
		currentInventoryCount = 0;
		currentInventory = wand.getInventoryByIndex(currentInventoryIndex);
		
		// Restore hotbar items first
		for (String hotbarSpellName : hotbarSpellNames) {
			addToInventory(wand.createSpellIcon(hotbarSpellName));
		}
		for (String hotbarMaterialName : hotbarMaterialNames) {
			addToInventory(wand.createBrushIcon(hotbarMaterialName));
		}
		
		// Put favorites
		for (List<String> favorites : favoriteSpells.descendingMap().values()) {
			for (String spellName : favorites) {
				addToInventory(wand.createSpellIcon(spellName));
			}
		}
		
		// Add unused spells by category
		for (Collection<String> spellGroup : groupedSpells.values()) {
		
			// Start a new inventory for a new group if the previous inventory is over 2/3 full
			if (currentInventoryCount > inventoryOrganizeNewGroupSize) {
				nextInventory();
			}
			
			for (String spellName : spellGroup) {
				addToInventory(wand.createSpellIcon(spellName));
			}
		}
		
		if (materials.size() > 0) {
			nextInventory();
			
			for (String materialName : materials.values()) {
				addToInventory(wand.createBrushIcon(materialName));
			}
		}
	}
}
