package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.block.CurrencyItem;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseShopAction extends BaseSpellAction implements GUIAction
{
    protected boolean requireWand = false;
    private String permissionNode = null;
    private String requiredPath = null;
    private String requiresCompletedPath = null;
    private String exactPath = null;
    protected boolean autoUpgrade = false;
    protected boolean useXP = false;
    protected boolean sell = false;
    protected boolean useItems = false;
    protected boolean showConfirmation = true;
    protected MaterialAndData confirmFillMaterial;
    protected CastContext context;
    private Map<Integer, ShopItem> showingItems;

    protected class ShopItem {
        private final ItemStack item;
        private final double worth;

        public ShopItem(ItemStack item, double worth) {
            this.item = item;
            this.worth = worth;
        }

        public double getWorth() {
            return worth;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    @Override
    public void deactivated() {
        // Check for shop items glitched into the player's inventory
        if (context != null) {
            context.getMage().removeItemsWithTag("shop");
        }
    }

    @Override
    public void dragged(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public SpellResult checkContext(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = mage.getController();
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (permissionNode != null && !player.hasPermission(permissionNode)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!requireWand) {
            return SpellResult.CAST;
        }
        Wand wand = mage.getActiveWand();
        if (wand == null) {
            context.sendMessageKey("no_wand");
            return SpellResult.FAIL;
        }

        WandUpgradePath path = wand.getPath();

        // Check path requirements
        if (requiredPath != null || exactPath != null) {
            if (path == null) {
                context.sendMessage(context.getMessage("no_path").replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }

            if ((requiredPath != null && !path.hasPath(requiredPath)) || (exactPath != null && !exactPath.equals(path.getKey()))) {
                WandUpgradePath requiresPath = controller.getPath(requiredPath);
                if (requiresPath != null) {
                    context.sendMessage(context.getMessage("no_required_path").replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in AddSpell action: " + requiredPath);
                }
                return SpellResult.FAIL;
            }
            if (requiresCompletedPath != null) {
                WandUpgradePath pathUpgrade = path.getUpgrade();
                if (pathUpgrade == null) {
                    context.sendMessage(context.getMessage("no_upgrade").replace("$wand", wand.getName()));
                    return SpellResult.FAIL;
                }
                if (path.canEnchant(wand)) {
                    context.sendMessage(context.getMessage("no_path_end").replace("$path", pathUpgrade.getName()));
                    return SpellResult.FAIL;
                }
            }
        }

        return SpellResult.CAST;
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (context == null || item == null || !InventoryUtils.hasMeta(item, "shop")) {
            return;
        }

        int slotIndex = Integer.parseInt(InventoryUtils.getMeta(item, "shop"));
        MageController controller = context.getController();
        Messages messages = controller.getMessages();
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
        boolean isItems = (useItems || !VaultController.hasEconomy()) && controller.getWorthItem() != null;
        boolean isXP = (useXP || (!VaultController.hasEconomy() && controller.getWorthItem() == null));
        ShopItem shopItem = showingItems.get(slotIndex);
        if (shopItem == null) {
            return;
        }
        double worth = shopItem.getWorth();
        boolean hasCosts = true;
        if (worth > 0) {
            if (isXP) {
                worth = worth * controller.getWorthXP();
                hasCosts = mage.getExperience() >= (int)(double)worth;
            } else if (isItems) {
                worth = worth * controller.getWorthBase() / controller.getWorthItemAmount();
                int hasAmount = getItemAmount(controller, mage);
                hasCosts = hasAmount >= worth;
            } else {
                worth = worth * controller.getWorthBase() * controller.getWorthVirtualCurrency();
                hasCosts = VaultController.getInstance().has(mage.getPlayer(), worth);
            }
        }
        if (sell) {
            hasCosts = getItemAmount(controller, item, mage) > 0;
        }

        if (!hasCosts) {
            String costString = context.getMessage("insufficient_resources");
            if (sell) {
                String amountString = formatItemAmount(controller, item, 1);
                costString = costString.replace("$cost", amountString);
            }
            else if (isXP) {
                String xpAmount = Integer.toString((int)(double)worth);
                xpAmount = messages.get("costs.xp_amount").replace("$amount", xpAmount);
                costString = costString.replace("$cost", xpAmount);
            }
            else if (isItems)
            {
                String amountString = formatItemAmount(controller, worth);
                costString = costString.replace("$cost", amountString);
            }
            else
            {
                costString = costString.replace("$cost", VaultController.getInstance().format(worth));
            }
            context.sendMessage(costString);
        } else {
            String itemName = controller.describeItem(item);
            if (InventoryUtils.hasMeta(item, "confirm")) {
                String inventoryTitle = context.getMessage("confirm_title", "Buy $item").replace("$item", itemName);
                Inventory confirmInventory = CompatibilityUtils.createInventory(null, 9, inventoryTitle);
                InventoryUtils.removeMeta(item, "confirm");
                for (int i = 0; i < 9; i++)
                {
                    if (i != 4) {
                        ItemStack filler = confirmFillMaterial.getItemStack(1);
                        ItemMeta meta = filler.getItemMeta();
                        if (meta != null)
                        {
                            meta.setDisplayName(ChatColor.DARK_GRAY + (i < 4 ? "-->" : "<--"));
                            filler.setItemMeta(meta);
                        }
                        confirmInventory.setItem(i, filler);
                    } else {
                        confirmInventory.setItem(i, item);
                    }
                }
                mage.deactivateGUI();
                mage.activateGUI(this, confirmInventory);
                return;
            }

            String costString = context.getMessage("deducted");
            if (sell) {
                String amountString = formatItemAmount(controller, worth);
                costString = costString.replace("$cost", amountString);
            }
            else if (isXP) {
                String xpAmount = Integer.toString((int)(double)worth);
                xpAmount = messages.get("costs.xp_amount").replace("$amount", xpAmount);
                costString = costString.replace("$cost", xpAmount);
            }
            else if (isItems)
            {
                String amountString = formatItemAmount(controller, worth);
                costString = costString.replace("$cost", amountString);
            } else {
                costString = costString.replace("$cost", VaultController.getInstance().format(worth));
            }

            costString = costString.replace("$item", itemName);
            context.sendMessage(costString);
            if (sell) {
                removeItems(controller, mage, item, 1);
                if (isXP) {
                    mage.giveExperience((int) (double) worth);
                }
                else if (isItems)
                {
                    int amount = (int)Math.ceil(worth);
                    ItemStack worthItem = controller.getWorthItem();
                    while (amount > 0) {
                        worthItem = InventoryUtils.getCopy(worthItem);
                        worthItem.setAmount(Math.min(amount, 64));
                        amount -= worthItem.getAmount();
                        mage.giveItem(worthItem);
                    }
                }
                else
                {
                    VaultController.getInstance().depositPlayer(mage.getPlayer(), worth);
                }
            }
            else
            {
                item = shopItem.getItem();
                if (requireWand) {
                    if (wand == null) {
                        context.sendMessageKey("no_wand");
                        mage.deactivateGUI();
                        return;
                    }
                    if (!wand.addItem(item)) {
                        String inapplicable = context.getMessage("not_applicable", "You can't buy $item").replace("$item", itemName);
                        context.sendMessage(inapplicable);
                        mage.deactivateGUI();
                        return;
                    }
                }
                if (isXP) {
                    mage.removeExperience((int)(double)worth);
                }
                else if (isItems)
                {
                    removeItems(controller, mage, (int)Math.ceil(worth));
                }
                else
                {
                    VaultController.getInstance().withdrawPlayer(mage.getPlayer(), worth);
                }

                if (!requireWand) {
                    context.getController().giveItemToPlayer(mage.getPlayer(), item);
                }

                if (wand != null && autoUpgrade) {
                    com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
                    WandUpgradePath nextPath = path != null ? path.getUpgrade(): null;
                    if (nextPath != null && path.checkUpgradeRequirements(wand, null) && !path.canEnchant(wand)) {
                        path.upgrade(wand, mage);
                    }
                }
            }
            onPurchase(context);
        }
        mage.deactivateGUI();
    }

    protected void onPurchase(CastContext context) {

    }

    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        permissionNode = parameters.getString("permission", null);
        useXP = parameters.getBoolean("use_xp", false);
        sell = parameters.getBoolean("sell", false);
        useItems = parameters.getBoolean("use_items", false);
        showConfirmation = parameters.getBoolean("confirm", true);
        confirmFillMaterial = ConfigurationUtils.getMaterialAndData(parameters, "confirm_filler", new MaterialAndData(Material.AIR));
        requiredPath = parameters.getString("path", null);
        exactPath = parameters.getString("path_exact", null);
        requiresCompletedPath = parameters.getString("path_end", null);
        autoUpgrade = parameters.getBoolean("auto_upgrade", false);
        requireWand = parameters.getBoolean("require_wand", false);
        if (requiresCompletedPath != null) {
            requiredPath = requiresCompletedPath;
        }
        if (requiredPath != null || exactPath != null) {
            requireWand = true;
        }
    }

    public SpellResult showItems(CastContext context, List<ShopItem> items) {
        Mage mage = context.getMage();
        this.context = context;
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        this.showingItems = new HashMap<Integer, ShopItem>();

        MageController controller = context.getController();
        Messages messages = controller.getMessages();

        // Load items
        List<ItemStack> itemStacks = new ArrayList<ItemStack>();
        boolean isItems = (useItems || !VaultController.hasEconomy()) && controller.getWorthItem() != null;
        boolean isXP = (useXP || (!VaultController.hasEconomy() && controller.getWorthItem() == null));
        String costString = context.getMessage("cost_lore");
        for (ShopItem shopItem : items) {
            double worth = shopItem.getWorth();
            ItemStack item = InventoryUtils.getCopy(shopItem.getItem());
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                itemStacks.add(item);
                continue;
            }
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<String>();
            }
            String costs;
            if (isXP) {
                worth = worth * controller.getWorthXP();
                String xpAmount = Integer.toString((int)(double)worth);
                xpAmount = messages.get("costs.xp_amount").replace("$amount", xpAmount);
                costs = costString.replace("$cost", xpAmount);
            } else if (isItems) {
                worth = worth * controller.getWorthBase() / controller.getWorthItemAmount();
                String itemWorth = formatItemAmount(controller, worth);
                costs = costString.replace("$cost", itemWorth);
            } else {
                worth = worth * controller.getWorthBase() * controller.getWorthVirtualCurrency();
                costs = costString.replace("$cost", VaultController.getInstance().format(worth));
            }
            lore.add(ChatColor.GOLD + costs);
            meta.setLore(lore);
            item.setItemMeta(meta);
            item = InventoryUtils.makeReal(item);
            int currentSlot = itemStacks.size();
            InventoryUtils.setMeta(item, "shop", Integer.toString(currentSlot));
            if (showConfirmation) {
                InventoryUtils.setMeta(item, "confirm", "true");
            }
            this.showingItems.put(currentSlot, shopItem);
            itemStacks.add(item);
        }

        if (itemStacks.size() == 0) {
            Wand wand = mage.getActiveWand();
            if (wand != null && autoUpgrade) {
                com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
                WandUpgradePath nextPath = path != null ? path.getUpgrade(): null;
                if (nextPath != null && path.checkUpgradeRequirements(wand, null) && !path.canEnchant(wand)) {
                    path.upgrade(wand, mage);
                    return SpellResult.CAST;
                }
            }
            context.sendMessageKey("no_items");
            return SpellResult.FAIL;
        }

        String inventoryTitle = context.getMessage("title", "Shop ($balance)");
        if (isXP) {
            String xpAmount = Integer.toString(mage.getExperience());
            xpAmount = messages.get("costs.xp_amount").replace("$amount", xpAmount);
            inventoryTitle = inventoryTitle.replace("$balance", xpAmount);
        } else if (isItems) {
            int itemAmount = getItemAmount(controller, mage);
            inventoryTitle = inventoryTitle.replace("$balance", formatItemAmount(controller, itemAmount));
        } else {
            double balance = VaultController.getInstance().getBalance(player);
            inventoryTitle = inventoryTitle.replace("$balance", VaultController.getInstance().format(balance));
        }

        int invSize = (int)Math.ceil((float)itemStacks.size() / 9.0f) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (ItemStack item : itemStacks)
        {
            displayInventory.addItem(item);
        }
        mage.activateGUI(this, displayInventory);

        return SpellResult.CAST;
	}

    protected String formatItemAmount(MageController controller, double amount) {
        CurrencyItem currency = controller.getCurrency();
        if (currency != null) {
            int evenAmount = (int)Math.ceil(amount);
            String currencyName = currency.getName();
            if (currencyName != null && !currencyName.isEmpty()) {
                if (evenAmount == 1) {
                    return Integer.toString(evenAmount) + " " + currencyName;
                }

                String pluralName = currency.getPluralName();
                if (pluralName == null || pluralName.isEmpty()) {
                    pluralName = currencyName;
                }
                return Integer.toString(evenAmount) + " " + pluralName;
            }
        }
        return formatItemAmount(controller, controller.getWorthItem(), amount);
    }

    protected String formatItemAmount(MageController controller, ItemStack item, double amount) {
        return Integer.toString((int)Math.ceil(amount)) + " " + controller.describeItem(item);
    }

    protected int getItemAmount(MageController controller, Mage mage) {
        return getItemAmount(controller, controller.getWorthItem(), mage);
    }

    protected int getItemAmount(MageController controller, ItemStack worthItem, Mage mage) {
        int itemAmount = 0;
        ItemStack[] contents = mage.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && controller.itemsAreEqual(contents[i], worthItem))
            {
                itemAmount += contents[i].getAmount();
            }
        }

        return itemAmount;
    }

    protected void removeItems(MageController controller, Mage mage, int amount) {
        removeItems(controller, mage, controller.getWorthItem(), amount);
    }

    protected void removeItems(MageController controller, Mage mage, ItemStack worthItem, int amount) {
        int remainingAmount = amount;
        Inventory inventory = mage.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++)
        {
            if (contents[i] != null && controller.itemsAreEqual(contents[i], worthItem))
            {
                if (contents[i].getAmount() <= remainingAmount) {
                    remainingAmount -= contents[i].getAmount();
                    contents[i] = null;
                } else {
                    contents[i].setAmount(contents[i].getAmount() - remainingAmount);
                    remainingAmount = 0;
                }
            }
            if (remainingAmount <= 0) {
                break;
            }
        }
        inventory.setContents(contents);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("confirm");
        parameters.add("path");
        parameters.add("path_end");
        parameters.add("path_exact");
        parameters.add("auto_upgrade");
        parameters.add("require_wand");
        parameters.add("permission");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        MageController controller = spell.getController();
        if (parameterKey.equals("path") || parameterKey.equals("path_exact") || parameterKey.equals("path_end")) {
            examples.addAll(controller.getWandPathKeys());
        } else if (parameterKey.equals("require_wand") || parameterKey.equals("confirm") || parameterKey.equals("auto_upgrade")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
