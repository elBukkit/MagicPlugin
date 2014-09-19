package com.elmakers.mine.bukkit.wand;

import java.util.ArrayList;
import java.util.Collection;
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
    protected final static int favoriteCountThreshold = 8;
    protected final static int favoritePageSize = 16;

	private int currentInventoryIndex = 0;
	private int currentInventoryCount = 0;
	
	public WandOrganizer(Wand wand, Mage mage) {
		this.wand = wand;
		this.mage = mage;
	}

    public WandOrganizer(Wand wand) {
        this.wand = wand;
        this.mage = null;
    }

    protected void removeHotbar(Map<String, Integer> spells, Map<String, Integer> brushes) {
        Inventory hotbar = wand.getHotbar();
        for (int i = 0; i < Wand.HOTBAR_SIZE; i++) {
            ItemStack hotbarItem = hotbar.getItem(i);
            if (hotbarItem == null || hotbarItem.getType() == Material.AIR) continue;

            String spellName = Wand.getSpell(hotbarItem);
            if (spellName != null) {
                spells.remove(spellName);
            } else {
                String materialKey = Wand.getBrush(hotbarItem);
                if (materialKey != null) {
                    brushes.remove(materialKey);
                }
            }
        }
    }
	
	public void organize() {
        Map<String, Integer> spells = wand.getSpellInventory();
        Map<String, Integer> brushes = wand.getBrushInventory();

        removeHotbar(spells, brushes);

        // Collect favorite spells
		MagicController master = wand.getMaster();
		TreeMap<Long, List<String>> favoriteSpells = new TreeMap<Long, List<String>>();
		Map<String, Collection<String>> groupedSpells = new TreeMap<String, Collection<String>>();
		for (String spellName : spells.keySet()) {
			Spell mageSpell = mage == null ? null : mage.getSpell(spellName);
			SpellTemplate spell = mageSpell == null ? master.getSpellTemplate(spellName) : mageSpell;
			if (spell != null) {
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
				}
                String category = spell.getCategory().getKey();
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

		Map<String, String> materials = new TreeMap<String, String>();
		for (String materialKey : brushes.keySet()) {
			if (MaterialBrush.isSpecialMaterialKey(materialKey)) {
				materials.put(" " + materialKey, materialKey);
			} else {
				materials.put(materialKey, materialKey);
			}
		}

		currentInventoryIndex = 0;
		currentInventoryCount = 0;

		// Organize favorites
        Set<String> addedFavorites = new HashSet<String>();
        List<String> favoriteList = new ArrayList<String>();
		for (List<String> favorites : favoriteSpells.descendingMap().values()) {
            if (addedFavorites.size() >= favoritePageSize) break;
			for (String spellName : favorites) {
                addedFavorites.add(spellName);
                favoriteList.add(spellName);
                if (addedFavorites.size() >= favoritePageSize) break;
			}
		}

        if (addedFavorites.size() > favoriteCountThreshold) {
            for (String favorite : favoriteList) {
                int slot = getNextSlot();
                spells.put(favorite, slot);
            }
            nextPage();
        } else {
            addedFavorites.clear();
        }

		// Add unused spells by category
		for (Collection<String> spellGroup : groupedSpells.values()) {

			// Start a new inventory for a new group if the previous inventory is over 2/3 full
			if (currentInventoryCount > inventoryOrganizeNewGroupSize) {
                nextPage();
			}

			for (String spellName : spellGroup) {
                if (!addedFavorites.contains(spellName)) {
                    int slot = getNextSlot();
                    spells.put(spellName, slot);
                }
			}
		}

		if (materials.size() > 0) {
			nextPage();

			for (String materialName : materials.values()) {
				brushes.put(materialName, getNextSlot());
			}
		}

        wand.updateSpellInventory(spells);
        wand.updateBrushInventory(brushes);
	}

    public void alphabetize() {
        Map<String, Integer> spells = wand.getSpellInventory();
        Map<String, Integer> brushes = wand.getBrushInventory();

        removeHotbar(spells, brushes);

        Map<String, String> materials = new TreeMap<String, String>();
        for (String materialKey : brushes.keySet()) {
            if (MaterialBrush.isSpecialMaterialKey(materialKey)) {
                materials.put(" " + materialKey, materialKey);
            } else {
                materials.put(materialKey, materialKey);
            }
        }

        Map<String, String> alphabetized = new TreeMap<String, String>();
        for (String spellKey : spells.keySet()) {
            String name = spellKey;
            SpellTemplate spell = wand.getController().getSpellTemplate(spellKey);
            if (spell != null) {
                name = spell.getName();
            }
            alphabetized.put(name, spellKey);
        }

        currentInventoryIndex = 0;
        currentInventoryCount = 0;

        for (String spellName : alphabetized.values()) {
            spells.put(spellName, getNextSlot(Wand.INVENTORY_SIZE));
        }

        if (materials.size() > 0) {
            nextPage();

            for (String materialName : materials.values()) {
                brushes.put(materialName, getNextSlot(Wand.INVENTORY_SIZE));
            }
        }

        wand.updateSpellInventory(spells);
        wand.updateBrushInventory(brushes);
    }

    protected int getNextSlot() {
        return getNextSlot(inventoryOrganizeSize);
    }

    protected int getNextSlot(int nextPageSize) {
        int slot = Wand.HOTBAR_SIZE + currentInventoryCount + (currentInventoryIndex * Wand.INVENTORY_SIZE);
        currentInventoryCount++;
        if (currentInventoryCount > nextPageSize) {
            nextPage();
        }
        return slot;
    }

    protected void nextPage() {
        currentInventoryCount = 0;
        currentInventoryIndex++;
    }
}
