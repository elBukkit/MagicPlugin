package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrushSelectAction extends BaseSpellAction implements GUIAction
{
    private CastContext context;
    private List<ItemStack> schematics = new ArrayList<ItemStack>();
    private Map<Material, List<ItemStack>> variants = new HashMap<Material, List<ItemStack>>();

    @Override
    public void deactivated() {

    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        if (context != null)
        {
            Mage mage = context.getMage();
            ItemStack item = event.getCurrentItem();
            String set = InventoryUtils.getMeta(item, "brush_set", null);
            if (set != null) {
                if (set.equals("schematics")) {
                    String inventoryTitle = context.getMessage("schematics_title", "Schematics");
                    int invSize = ((schematics.size() + 9) / 9) * 9;
                    Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
                    for (ItemStack schematicItem : schematics)
                    {
                        displayInventory.addItem(schematicItem);
                    }
                    mage.deactivateGUI();
                    mage.activateGUI(this);
                    mage.getPlayer().openInventory(displayInventory);
                    return;
                } else if (set.equals("variants")) {
                    MaterialAndData baseMaterial = new MaterialAndData(item);
                    String baseName = baseMaterial.getBaseName();
                    String inventoryTitle = context.getMessage("variants_title", "$variant Variants").replace("$variant", baseName);
                    List<ItemStack> variantList = variants.get(baseMaterial.getMaterial());
                    int invSize = ((variantList.size() + 9) / 9) * 9;
                    Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
                    for (ItemStack variantItem : variantList)
                    {
                        displayInventory.addItem(variantItem);
                    }
                    mage.deactivateGUI();
                    mage.activateGUI(this);
                    mage.getPlayer().openInventory(displayInventory);
                    return;
                }
            }

            mage.deactivateGUI();
            Wand wand = mage.getActiveWand();
            if (wand != null && com.elmakers.mine.bukkit.wand.Wand.isBrush(item))
            {
                String brushKey = com.elmakers.mine.bukkit.wand.Wand.getBrush(item);
                wand.setActiveBrush(brushKey);
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        Wand wand = mage.getActiveWand();
        schematics.clear();
        variants.clear();
        this.context = context;
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (wand == null) {
            return SpellResult.FAIL;
        }
        List<String> brushKeys = new ArrayList(wand.getBrushes());
        Collections.sort(brushKeys);
        List<ItemStack> brushes = new ArrayList<ItemStack>();
        MaterialAndData previous = null;
        for (String brushKey : brushKeys) {
            if (MaterialBrush.isSchematic(brushKey)) {
                ItemStack brushItem = com.elmakers.mine.bukkit.wand.Wand.createBrushItem(brushKey, controller, null, false);
                schematics.add(brushItem);
                continue;
            }
            if (MaterialBrush.isSpecialMaterialKey(brushKey)) continue;
            ItemStack brushItem = com.elmakers.mine.bukkit.wand.Wand.createBrushItem(brushKey, controller, null, false);
            if (brushItem != null) {
                MaterialAndData material = new MaterialAndData(brushItem);
                if (previous != null && material.getMaterial() == previous.getMaterial())
                {
                    List<ItemStack> variantList = variants.get(material.getMaterial());
                    ItemStack lastAdded = brushes.get(brushes.size() - 1);
                    if (variantList == null)
                    {
                        String baseName = material.getBaseName();
                        variantList = new ArrayList<ItemStack>();
                        variantList.add(lastAdded);
                        brushes.remove(brushes.size() - 1);
                        ItemStack category = InventoryUtils.getCopy(lastAdded);
                        ItemMeta meta = category.getItemMeta();
                        String name = context.getMessage("variant_name", "[$variant]");
                        meta.setDisplayName(name.replace("$variant", baseName));
                        List<String> lore = new ArrayList<String>();
                        String description = context.getMessage("variant_description", "Click to choose a variant of $variant");
                        lore.add(description.replace("$variant", baseName));
                        meta.setLore(lore);
                        category.setItemMeta(meta);
                        InventoryUtils.setMeta(category, "brush_set", "variants");
                        variants.put(material.getMaterial(), variantList);
                        brushes.add(category);
                    }
                    variantList.add(brushItem);
                }
                else
                {
                    brushes.add(brushItem);
                }
                previous = material;
            }
        }

        ItemStack schematicItem = null;
        if (schematics.size() == 1) {
            schematicItem = schematics.get(0);
        } else if (schematics.size() > 0) {
            schematicItem = InventoryUtils.getCopy(schematics.get(0));
            ItemMeta meta = schematicItem.getItemMeta();
            meta.setDisplayName(context.getMessage("schematics_name", "[Schematics]"));
            List<String> lore = new ArrayList<String>();
            lore.add(context.getMessage("schematics_description", "Click to choose schematics"));
            meta.setLore(lore);
            schematicItem.setItemMeta(meta);
            InventoryUtils.setMeta(schematicItem, "brush_set", "schematics");
        }
        if (schematicItem != null) {
            brushes.add(schematicItem);
        }

        String inventoryTitle = context.getMessage("title", "Brushes");
        int invSize = ((brushes.size() + 9) / 9) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (ItemStack item : brushes)
        {
            displayInventory.addItem(item);
        }
        mage.activateGUI(this);
        mage.getPlayer().openInventory(displayInventory);

        return SpellResult.CAST;
	}
}
