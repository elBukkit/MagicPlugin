package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
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
    private boolean allowAbsorbing = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        allowAbsorbing = parameters.getBoolean("allow_absorbing", true);
    }

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
                    String baseName = getBaseName(baseMaterial);
                    String inventoryTitle = context.getMessage("variants_title", "$variant Types").replace("$variant", baseName);
                    List<ItemStack> variantList = variants.get(baseMaterial.getMaterial());
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
            Currency blockCurrency = context.getController().getBlockExchangeCurrency();
            if (event.isRightClick() && blockCurrency != null && InventoryUtils.getMetaBoolean(item, "absorb", false) && !mage.isDead()) {
                Messages messages = context.getController().getMessages();
                if (mage.isAtMaxCurrency(blockCurrency.getKey())) {
                    String limitMessage = messages.get("currency." + blockCurrency.getKey() + ".limit", messages.get("currency.default.limit"));
                    limitMessage = limitMessage.replace("$amount", Integer.toString((int)blockCurrency.getMaxValue()));
                    limitMessage = limitMessage.replace("$type", blockCurrency.getName(messages));
                    mage.sendMessage(limitMessage);
                    return;
                }
                Inventory inventory = mage.getInventory();
                ItemStack[] contents = inventory.getStorageContents();
                int count = 0;
                for (int i = 0; i < contents.length; i++) {
                    if (blockCurrency == null || mage.isAtMaxCurrency(blockCurrency.getKey())) break;
                    ItemStack itemStack = contents[i];
                    if (CompatibilityUtils.isEmpty(itemStack)) continue;
                    if (itemStack.hasItemMeta()) continue;
                    if (itemStack.getType() != item.getType()) continue;
                    Double worth = context.getController().getWorth(itemStack, blockCurrency.getKey());
                    if (worth == null || worth <= 0) continue;
                    mage.addCurrency(blockCurrency.getKey(), worth);
                    contents[i] = null;
                    count += itemStack.getAmount();
                }
                inventory.setStorageContents(contents);
                String message;
                if (count == 0) {
                    message = messages.get("brush.no_absorbed");
                    message = message.replace("$type", context.getController().describeItem(item));
                } else {
                    message = messages.get("brush.absorbed");
                    message = message.replace("$amount", Integer.toString(count));
                    message = message.replace("$type", context.getController().describeItem(item));
                }
                mage.sendMessage(message);
                return;
            }
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
            ItemStack brushItem = controller.createBrushItem(brushKey, context.getWand(), false);
            if (MaterialBrush.isSchematic(brushKey)) {
                schematics.add(brushItem);
                continue;
            }
            if (MaterialBrush.isSpecialMaterialKey(brushKey)) {
                specials.add(brushItem);
                continue;
            }
            if (brushItem != null) {
                if (mage.isRestricted(brushItem.getType())) continue;
                MaterialAndData material = new MaterialAndData(brushKey);
                Material baseVariant = DefaultMaterials.getBaseColor(material.getMaterial());
                if (baseVariant == null) {
                    baseVariant = DefaultMaterials.getBaseVariant(material.getMaterial());
                }
                if (!context.isConsumeFree()) {
                    addAbsorbInfo(brushItem);
                }

                if (previous != null && material.getMaterial() == previous.getMaterial())
                {
                    List<ItemStack> variantList = variants.get(material.getMaterial());
                    if (variantList == null)
                    {
                        ItemStack lastAdded = brushes.get(brushes.size() - 1);
                        variantList = new ArrayList<>();
                        variantList.add(lastAdded);
                        brushes.remove(brushes.size() - 1);
                        variants.put(material.getMaterial(), variantList);
                    }
                    variantList.add(brushItem);
                } else if (baseVariant != null) {
                    List<ItemStack> variantList = variants.get(baseVariant);
                    if (variantList == null)
                    {
                        variantList = new ArrayList<>();
                        variants.put(baseVariant, variantList);
                    }
                    variantList.add(brushItem);
                    // Don't set "previous" here or we'll end up removing the wrong item type
                    // This is because this is a special edge-case of having a variant of something
                    // without having the base thing added first
                } else {
                    brushes.add(brushItem);
                    previous = material;
                }
            }
        }

        for (Map.Entry<Material, List<ItemStack>> entry : variants.entrySet()) {
            MaterialAndData material = new MaterialAndData(entry.getKey());
            List<ItemStack> items = entry.getValue();
            if (items.size() == 1) {
                brushes.add(items.get(0));
                continue;
            }

            String materialName = getBaseName(material);
            ItemStack category = new ItemStack(material.getMaterial());
            category = CompatibilityUtils.makeReal(category);
            if (category == null) continue;
            ItemMeta meta = category.getItemMeta();
            String name = context.getMessage("variant_name", "" + ChatColor.AQUA + "$variant");
            meta.setDisplayName(name.replace("$variant", materialName));
            List<String> lore = new ArrayList<>();
            String description = context.getMessage("variant_description", "Choose a type of $variant");
            lore.add(description.replace("$variant", materialName));
            meta.setLore(lore);
            category.setItemMeta(meta);
            InventoryUtils.setMeta(category, "brush_set", "variants");
            brushes.add(category);
        }

        Collections.sort(brushes, new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack o1, ItemStack o2) {
                String name1 = ChatColor.stripColor(o1.getItemMeta().getDisplayName());
                String name2 = ChatColor.stripColor(o2.getItemMeta().getDisplayName());
                return name1.compareToIgnoreCase(name2);
            }
        });

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

    private void addAbsorbInfo(ItemStack itemStack) {
        if (!allowAbsorbing) return;
        MageController controller = context.getController();
        Currency blockCurrency = controller.getBlockExchangeCurrency();
        if (blockCurrency == null) return;
        String message = context.getController().getMessages().get("brush.absorb");
        if (message == null || message.isEmpty()) return;
        ItemStack plain = new ItemStack(itemStack.getType());
        Double worth = controller.getWorth(plain, blockCurrency.getKey());
        if (worth == null || worth == 0) return;
        message = message.replace("$type", controller.describeItem(plain));
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        InventoryUtils.wrapText(message, lore);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        InventoryUtils.setMetaBoolean(itemStack, "absorb", true);
    }

    private String getBaseName(MaterialAndData material) {
        // Bit of a hack to get a base material name
        String materialName = material.getName(context == null ? null : context.getController().getMessages());
        materialName = materialName.replace("White ", "");
        materialName = materialName.replace("oak ", "");
        materialName = materialName.replace("cobblestone ", "");
        return materialName;
    }
}
