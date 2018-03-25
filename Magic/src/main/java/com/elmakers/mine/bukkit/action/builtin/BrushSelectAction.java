package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

public class BrushSelectAction extends BaseSpellAction implements GUIAction
{
    private static int INVENTORY_ROWS = 6;
    private CastContext context;
    private List<ItemStack> schematics = new ArrayList<>();
    private Map<Material, List<ItemStack>> variants = new HashMap<>();
    private int page = 1;

    @Override
    public void deactivated() {

    }

    @Override
    public void dragged(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        InventoryAction action = event.getAction();
        if (context != null)
        {
            Mage mage = context.getMage();
            if (action == InventoryAction.NOTHING)
            {
                int direction = event.getClick() == ClickType.LEFT ? 1 : -1;
                page = page + direction;
                mage.deactivateGUI();
                perform(context);
                event.setCancelled(true);
                return;
            }

            ItemStack item = event.getCurrentItem();
            String set = InventoryUtils.getMetaString(item, "brush_set", null);
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
                    mage.activateGUI(this, displayInventory);
                    return;
                } else if (set.equals("variants")) {
                    MaterialAndData baseMaterial = new MaterialAndData(item);
                    String baseName = baseMaterial.getBaseName();
                    String inventoryTitle = context.getMessage("variants_title", "$variant Types").replace("$variant", baseName);
                    String brushKey = com.elmakers.mine.bukkit.wand.Wand.getBrush(item);
                    MaterialAndData brushMaterial = new MaterialAndData(brushKey);
                    List<ItemStack> variantList = variants.get(brushMaterial.getMaterial());
                    int invSize = ((variantList.size() + 9) / 9) * 9;
                    Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
                    for (ItemStack variantItem : variantList)
                    {
                        displayInventory.addItem(variantItem);
                    }
                    mage.deactivateGUI();
                    mage.activateGUI(this, displayInventory);
                    return;
                }
            }

            mage.deactivateGUI();
            Wand wand = context.getWand();
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
        Wand wand = context.getWand();
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
        List<String> brushKeys = new ArrayList<>(wand.getBrushes());
        Collections.sort(brushKeys);
        List<ItemStack> brushes = new ArrayList<>();
        List<ItemStack> specials = new ArrayList<>();
        MaterialAndData previous = null;
        for (String brushKey : brushKeys) {
            ItemStack brushItem = com.elmakers.mine.bukkit.wand.Wand.createBrushItem(brushKey, controller, null, false);
            if (MaterialBrush.isSchematic(brushKey)) {
                schematics.add(brushItem);
                continue;
            }
            if (MaterialBrush.isSpecialMaterialKey(brushKey)) {
                specials.add(brushItem);
                continue;
            }
            if (brushItem != null) {
                MaterialAndData material = new MaterialAndData(brushKey);
                if (previous != null && material.getMaterial() == previous.getMaterial())
                {
                    List<ItemStack> variantList = variants.get(material.getMaterial());
                    ItemStack lastAdded = brushes.get(brushes.size() - 1);
                    if (variantList == null)
                    {
                        String baseName = material.getBaseName();
                        variantList = new ArrayList<>();
                        variantList.add(lastAdded);
                        brushes.remove(brushes.size() - 1);
                        ItemStack category = InventoryUtils.getCopy(lastAdded);
                        ItemMeta meta = category.getItemMeta();
                        String name = context.getMessage("variant_name", "" + ChatColor.AQUA + "$variant");
                        meta.setDisplayName(name.replace("$variant", baseName));
                        List<String> lore = new ArrayList<>();
                        String description = context.getMessage("variant_description", "Choose a type of $variant");
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
            meta.setDisplayName(context.getMessage("schematics_name", "" + ChatColor.AQUA + "Schematics"));
            List<String> lore = new ArrayList<>();
            lore.add(context.getMessage("schematics_description", "Choose a schematic"));
            meta.setLore(lore);
            schematicItem.setItemMeta(meta);
            InventoryUtils.setMeta(schematicItem, "brush_set", "schematics");
        }
        if (schematicItem != null) {
            brushes.add(schematicItem);
        }
        brushes.addAll(specials);

        if (brushes.size() == 0)
        {
            return SpellResult.NO_TARGET;
        }

        int inventorySize = 9 * INVENTORY_ROWS;
        int numPages = (int)Math.ceil((float)brushes.size() / inventorySize);
        if (page < 1) page = numPages;
        else if (page > numPages) page = 1;
        int pageIndex = page - 1;
        int startIndex = pageIndex * inventorySize;
        int maxIndex = (pageIndex + 1) * inventorySize - 1;
        List<ItemStack> showBrushes = new ArrayList<>();
        for (int i = startIndex; i <= maxIndex && i < brushes.size(); i++)
        {
            showBrushes.add(brushes.get(i));
        }

        String inventoryTitle = context.getMessage("title", "Brushes");
        if (numPages > 1) {
            inventoryTitle += " (" + page + "/" + numPages + ")";
        }
        int invSize = (int)Math.ceil(showBrushes.size() / 9.0f) * 9;

        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (ItemStack brush : showBrushes)
        {
            displayInventory.addItem(brush);
        }
        mage.activateGUI(this, displayInventory);

        return SpellResult.CAST;
    }
}
