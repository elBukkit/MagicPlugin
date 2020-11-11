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
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;

public class WandOrganizer {
    private final Wand wand;
    private final Mage mage;

    protected static final int inventoryOrganizeNewGroupBuffer = 8;
    protected static final int favoriteCastCountThreshold = 20;
    protected static final int favoriteCountBuffer = 9;
    protected static final int favoritePageBuffer = 4;

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
        if (wand.getHotbarCount() == 0) return;
        List<WandInventory> hotbars = wand.getHotbars();
        for (WandInventory hotbar : hotbars) {
            int hotbarSize = hotbar.getSize();
            for (int i = 0; i < hotbarSize; i++) {
                ItemStack hotbarItem = hotbar.getItem(i);
                if (hotbarItem == null || hotbarItem.getType() == Material.AIR) continue;

                String spellName = Wand.getSpell(hotbarItem);
                if (spellName != null) {
                    SpellKey spellKey = new SpellKey(spellName);
                    spellName = spellKey.getBaseKey();
                }
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
    }

    public void organize() {
        Map<String, Integer> spells = wand.getSpellInventory();
        Map<String, Integer> brushes = wand.getBrushInventory();

        removeHotbar(spells, brushes);

        // Collect favorite spells
        MageController controller = wand.getController();
        TreeMap<Long, List<String>> favoriteSpells = new TreeMap<>();
        Map<String, Collection<String>> groupedSpells = new TreeMap<>();
        for (String spellName : spells.keySet()) {
            Spell mageSpell = mage == null ? null : mage.getSpell(spellName);
            SpellTemplate spell = mageSpell == null ? controller.getSpellTemplate(spellName) : mageSpell;
            if (spell != null) {
                // Sum up all levels of this spell:
                long castCount = 0;
                int spellLevel = 1;
                while (mageSpell != null) {
                    castCount += mageSpell.getCastCount();
                    spellLevel++;
                    SpellKey spellKey = new SpellKey(spellName, spellLevel);
                    String key = spellKey.getKey();
                    mageSpell = mage.hasSpell(key) ? mage.getSpell(key) : null;
                }
                spellName = spell.getSpellKey().getBaseKey();
                if (castCount > favoriteCastCountThreshold) {
                    List<String> favorites = null;
                    if (!favoriteSpells.containsKey(castCount)) {
                        favorites = new ArrayList<>();
                        favoriteSpells.put(castCount, favorites);
                    } else {
                        favorites = favoriteSpells.get(castCount);
                    }
                    favorites.add(spellName);
                }
                SpellCategory spellCategory = spell.getCategory();
                String category = spellCategory == null ? null : spellCategory.getKey();
                if (category == null || category.length() == 0) {
                    category = "default";
                }
                Collection<String> spellList = groupedSpells.get(category);
                if (spellList == null) {
                    spellList = new TreeSet<>();
                    groupedSpells.put(category, spellList);
                }
                spellList.add(spellName);
            }
        }

        Map<String, String> materials = new TreeMap<>();
        if (wand.getBrushMode() == WandMode.INVENTORY)
        {
            for (String materialKey : brushes.keySet()) {
                if (MaterialBrush.isSpecialMaterialKey(materialKey)) {
                    materials.put(" " + materialKey, materialKey);
                } else {
                    materials.put(materialKey, materialKey);
                }
            }
        }

        currentInventoryIndex = 0;
        currentInventoryCount = 0;

        // Organize favorites
        WandMode mode = wand.getMode();
        Set<String> addedFavorites = new HashSet<>();
        List<String> favoriteList = new ArrayList<>();
        int favoritePageSize = wand.getInventorySize() - favoritePageBuffer;
        for (List<String> favorites : favoriteSpells.descendingMap().values()) {
            if (addedFavorites.size() >= favoritePageSize) break;
            for (String spellName : favorites) {
                addedFavorites.add(spellName);
                favoriteList.add(spellName);
                if (addedFavorites.size() >= favoritePageSize) break;
            }
        }

        if (addedFavorites.size() > 0) {
            for (String favorite : favoriteList) {
                int slot = getNextSlot();
                spells.put(favorite, slot);
            }
            if (mode != WandMode.CHEST && addedFavorites.size() > wand.getInventorySize() - favoriteCountBuffer) {
                nextPage();
            }
        } else {
            addedFavorites.clear();
        }

        // Add unused spells by category
        int inventoryOrganizeNewGroupSize = wand.getInventorySize() - inventoryOrganizeNewGroupBuffer;
        for (Collection<String> spellGroup : groupedSpells.values()) {

            // Start a new inventory for a new group if the previous inventory is over 2/3 full
            if (mode != WandMode.CHEST && currentInventoryCount > inventoryOrganizeNewGroupSize) {
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
        if (materials.size() > 0) {
            wand.updateBrushInventory(brushes);
        }
    }

    public void alphabetize() {
        Map<String, Integer> spells = wand.getSpellInventory();
        Map<String, Integer> brushes = wand.getBrushInventory();

        removeHotbar(spells, brushes);

        Map<String, String> materials = new TreeMap<>();
        if (wand.getBrushMode() == WandMode.INVENTORY) {
            for (String materialKey : brushes.keySet()) {
                if (MaterialBrush.isSpecialMaterialKey(materialKey)) {
                    materials.put(" " + materialKey, materialKey);
                } else {
                    materials.put(materialKey, materialKey);
                }
            }
        }

        Map<String, String> alphabetized = new TreeMap<>();
        for (String spellKey : spells.keySet()) {
            SpellTemplate spell = wand.getController().getSpellTemplate(spellKey);
            if (spell != null) {
                alphabetized.put(spell.getName(), spell.getSpellKey().getBaseKey());
            }
        }

        currentInventoryIndex = 0;
        currentInventoryCount = 0;

        for (String spellName : alphabetized.values()) {
            spells.put(spellName, getNextSlot(wand.getInventorySize()));
        }

        if (materials.size() > 0) {
            nextPage();

            for (String materialName : materials.values()) {
                brushes.put(materialName, getNextSlot(wand.getInventorySize()));
            }
        }

        wand.updateSpellInventory(spells);
        if (materials.size() > 0) {
            wand.updateBrushInventory(brushes);
        }
    }

    protected int getNextSlot() {
        return getNextSlot(wand.getInventorySize() - wand.getOrganizeBuffer());
    }

    protected int getNextSlot(int nextPageSize) {
        int slot = wand.getHotbarSize() + currentInventoryCount + (currentInventoryIndex * wand.getInventorySize());
        currentInventoryCount++;
        if (currentInventoryCount >= nextPageSize) {
            nextPage();
        }
        return slot;
    }

    protected void nextPage() {
        currentInventoryCount = 0;
        currentInventoryIndex++;
    }
}
