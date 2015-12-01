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
    private String requiredTemplate = null;
    private String requiresCompletedPath = null;
    private String exactPath = null;
    protected int upgradeLevels = 0;
    protected boolean autoClose = true;
    protected boolean autoUpgrade = false;
    protected boolean applyToWand = false;
    protected boolean isXP = false;
    protected boolean isSkillPoints = false;
    protected boolean sell = false;
    protected boolean isItems = false;
    protected double costScale = 1;
    protected boolean showConfirmation = true;
    protected MaterialAndData confirmFillMaterial;
    protected CastContext context;
    private Map<Integer, ShopItem> showingItems;
    private List<ItemStack> itemStacks;

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
            context.showMessage("no_wand", "You must be holding a wand!");
            return SpellResult.FAIL;
        }

        WandUpgradePath path = wand.getPath();

        if (requiredTemplate != null) {
            String template = wand.getTemplateKey();
            if (template == null || !template.equals(requiredTemplate)) {
                context.showMessage(context.getMessage("no_template", "You may not learn with that $wand.").replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }
        }

        // Check path requirements
        if (requiredPath != null || exactPath != null) {
            if (path == null) {
                context.showMessage(context.getMessage("no_path", "You may not learn with that $wand.").replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }

            if ((requiredPath != null && !path.hasPath(requiredPath)) || (exactPath != null && !exactPath.equals(path.getKey()))) {
                WandUpgradePath requiresPath = controller.getPath(requiredPath);
                if (requiresPath != null) {
                    context.showMessage(context.getMessage("no_required_path", "You must be at least $path!").replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in AddSpell action: " + requiredPath);
                }
                return SpellResult.FAIL;
            }
            if (requiresCompletedPath != null) {
                WandUpgradePath pathUpgrade = path.getUpgrade();
                if (pathUpgrade == null) {
                    context.showMessage(context.getMessage("no_upgrade", "There is nothing more for you here.").replace("$wand", wand.getName()));
                    return SpellResult.FAIL;
                }
                if (path.canEnchant(wand)) {
                    context.showMessage(context.getMessage("no_path_end", "You must be ready to advance to $path!").replace("$path", pathUpgrade.getName()));
                    return SpellResult.FAIL;
                }
            }
        }

        return SpellResult.CAST;
    }

    protected boolean hasItemCosts(CastContext context, ShopItem shopItem) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
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
            } else if (isSkillPoints) {
                worth = worth * controller.getWorthBase() / controller.getWorthSkillPoints();
                hasCosts = mage.getSkillPoints() >= Math.ceil(worth);
            } else {
                worth = worth * controller.getWorthBase();
                hasCosts = VaultController.getInstance().has(mage.getPlayer(), worth);
            }
        }

        return hasCosts;
    }

    protected String getItemCost(CastContext context, ShopItem shopItem) {
        String amountString = "?";
        MageController controller = context.getController();
        Messages messages = controller.getMessages();
        double worth = shopItem.getWorth();

        if (isXP) {
            worth = worth * controller.getWorthXP();
            amountString = Integer.toString((int)(double)worth);
            amountString = messages.get("costs.xp_amount").replace("$amount", amountString);
        }
        else if (isItems)
        {
            worth = worth * controller.getWorthBase() / controller.getWorthItemAmount();
            amountString = formatItemAmount(controller, worth);
        }
        else if (isSkillPoints) {
            worth = worth * controller.getWorthBase() / controller.getWorthSkillPoints();
            amountString = Integer.toString((int)Math.ceil(worth));
            amountString = messages.get("costs.sp_amount").replace("$amount", amountString);
        }
        else
        {
            worth = worth * controller.getWorthBase();
            amountString = VaultController.getInstance().format(worth);
        }

        return amountString;
    }

    protected void giveCosts(CastContext context, ShopItem shopItem) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        double worth = shopItem.getWorth();
        if (isXP) {
            worth = worth * controller.getWorthXP();
            mage.giveExperience((int) (double) worth);
        }
        else if (isItems)
        {
            worth = worth * controller.getWorthBase() / controller.getWorthItemAmount();
            int amount = (int)Math.ceil(worth);
            ItemStack worthItem = controller.getWorthItem();
            while (amount > 0) {
                worthItem = InventoryUtils.getCopy(worthItem);
                worthItem.setAmount(Math.min(amount, 64));
                amount -= worthItem.getAmount();
                mage.giveItem(worthItem);
            }
        }
        else if (isSkillPoints)
        {
            worth = worth * controller.getWorthBase() / controller.getWorthSkillPoints();
            int amount = (int)Math.ceil(worth);
            mage.addSkillPoints(amount);
        }
        else
        {
            worth = worth * controller.getWorthBase();
            VaultController.getInstance().depositPlayer(mage.getPlayer(), worth);
        }
    }

    protected void takeCosts(CastContext context, ShopItem shopItem) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        double worth = shopItem.getWorth();

        if (isXP) {
            worth = worth * controller.getWorthXP();
            mage.removeExperience((int)(double)worth);
        }
        else if (isItems)
        {
            worth = worth * controller.getWorthBase() / controller.getWorthItemAmount();
            removeItems(controller, mage, (int)Math.ceil(worth));
        }
        else if (isSkillPoints)
        {
            worth = worth * controller.getWorthBase() / controller.getWorthSkillPoints();
            mage.addSkillPoints(-(int)Math.ceil(worth));
        }
        else
        {
            worth = worth * controller.getWorthBase();
            VaultController.getInstance().withdrawPlayer(mage.getPlayer(), worth);
        }
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        Mage mage = context.getMage();
        if (context == null || item == null || !InventoryUtils.hasMeta(item, "shop")) {
            if (!autoClose) {
                mage.deactivateGUI();
            }
            return;
        }

        int slotIndex = Integer.parseInt(InventoryUtils.getMeta(item, "shop"));
        MageController controller = context.getController();
        Wand wand = mage.getActiveWand();

        ShopItem shopItem = showingItems.get(slotIndex);
        if (shopItem == null) {
            return;
        }
        boolean hasCosts = sell ? getItemAmount(controller, shopItem.getItem(), mage) > 0
                : hasItemCosts(context, shopItem);

        if (!hasCosts) {
            String costString = context.getMessage("insufficient", ChatColor.RED + "Costs $cost");
            if (sell) {
                costString = costString.replace("$cost", formatItemAmount(controller, item, 1));
            } else {
                costString = costString.replace("$cost", getItemCost(context, shopItem));
            }
            context.showMessage(costString);
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

            double worth = shopItem.getWorth();
            String costString = context.getMessage("deducted", "&d&oYou bought &r&6$item &r&d&ofor &r&a$cost");
            if (sell) {
                String amountString = formatItemAmount(controller, worth);
                costString = costString.replace("$cost", amountString);
                removeItems(controller, mage, item, 1);
                giveCosts(context, shopItem);
            } else {
                costString = costString.replace("$cost", getItemCost(context, shopItem));
                item = shopItem.getItem();
                if (requireWand) {
                    if (wand == null) {
                        context.showMessage("no_wand", "You must be holding a wand!");
                        mage.deactivateGUI();
                        return;
                    }
                    if (applyToWand && !wand.addItem(item)) {
                        String inapplicable = context.getMessage("not_applicable", "You can't buy $item").replace("$item", itemName);
                        context.showMessage(inapplicable);
                        mage.deactivateGUI();
                        return;
                    }
                }
                takeCosts(context, shopItem);
                if (!requireWand) {
                    context.getController().giveItemToPlayer(mage.getPlayer(), item);
                }

                if (wand != null && autoUpgrade) {
                    if (upgradeLevels <= 0) {
                        com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
                        WandUpgradePath nextPath = path != null ? path.getUpgrade(): null;
                        if (nextPath != null && path.checkUpgradeRequirements(wand, null) && !path.canEnchant(wand)) {
                            path.upgrade(wand, mage);
                        }
                    } else {
                        wand.enchant(upgradeLevels, mage, false);
                    }
                }
            }

            costString = costString.replace("$item", itemName);
            context.showMessage(costString);
            onPurchase(context);
        }
        if (autoClose) {
            mage.deactivateGUI();
        } else {
            // update title
            mage.continueGUI(this, getInventory(context));
        }
    }

    protected void onPurchase(CastContext context) {

    }

    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        permissionNode = parameters.getString("permission", null);
        sell = parameters.getBoolean("sell", false);
        showConfirmation = parameters.getBoolean("confirm", true);
        confirmFillMaterial = ConfigurationUtils.getMaterialAndData(parameters, "confirm_filler", new MaterialAndData(Material.AIR));
        requiredPath = parameters.getString("path", null);
        exactPath = parameters.getString("path_exact", null);
        requiresCompletedPath = parameters.getString("path_end", null);
        requiredTemplate = parameters.getString("require_template", null);
        autoUpgrade = parameters.getBoolean("auto_upgrade", false);
        upgradeLevels = parameters.getInt("upgrade_levels", 0);
        requireWand = parameters.getBoolean("require_wand", false);
        autoClose = parameters.getBoolean("auto_close", true);
        if (!autoClose) {
            showConfirmation = false;
        }
        if (requiresCompletedPath != null) {
            requiredPath = requiresCompletedPath;
        }
        if (requiredPath != null || exactPath != null || requiredTemplate != null) {
            requireWand = true;
        }
        applyToWand = parameters.getBoolean("apply_to_wand", requireWand);

        MageController controller = context.getController();
        isXP = parameters.getBoolean("use_xp", false);
        isItems = parameters.getBoolean("use_items", false) && controller.getWorthItem() != null;
        isSkillPoints = parameters.getBoolean("use_sp", false) && controller.isSPEnabled();
        if (!isSkillPoints && !isXP && !isItems && !VaultController.hasEconomy())
        {
            if (controller.getWorthItem() != null)
            {
                isItems = true;
            }
            else
            {
                isSkillPoints = true;
            }
        }

        if (isSkillPoints) {
            costScale = controller.getWorthBase() / controller.getWorthSkillPoints();
        } else {
            costScale = 1;
        }
    }

    protected String getBalanceDescription(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        Messages messages = controller.getMessages();
        String description = "";
        if (isXP) {
            String xpAmount = Integer.toString(mage.getExperience());
            description = messages.get("costs.xp_amount").replace("$amount", xpAmount);
        } else if (isItems) {
            int itemAmount = getItemAmount(controller, mage);
            description = formatItemAmount(controller, itemAmount);
        } else if (isSkillPoints) {
            String spAmount = Integer.toString(mage.getSkillPoints());
            description = messages.get("costs.sp_amount").replace("$amount", spAmount);
        } else {
            double balance = VaultController.getInstance().getBalance(mage.getPlayer());
            description = VaultController.getInstance().format(balance);
        }

        return description;
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

        // Load items
        itemStacks = new ArrayList<ItemStack>();
        String costString = context.getMessage("cost_lore", "Costs: $cost");
        for (ShopItem shopItem : items) {
            int currentSlot = itemStacks.size();
            if (shopItem == null) {
                this.showingItems.put(currentSlot, null);
                itemStacks.add(new ItemStack(Material.AIR));
                continue;
            }
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
            String costs = costString.replace("$cost", getItemCost(context, shopItem));
            lore.add(ChatColor.GOLD + costs);
            meta.setLore(lore);
            item.setItemMeta(meta);
            item = InventoryUtils.makeReal(item);
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
            context.showMessage("no_items", "There is nothing for you to buy here");
            return SpellResult.FAIL;
        }
        Inventory displayInventory = getInventory(context);
        mage.activateGUI(this, displayInventory);

        return SpellResult.CAST;
	}

    protected Inventory getInventory(CastContext context)
    {
        String inventoryTitle = context.getMessage("title", "Shop ($balance)");
        String balanceDescription = getBalanceDescription(context);
        inventoryTitle = inventoryTitle.replace("$balance", balanceDescription);

        int invSize = itemStacks == null ? 0 : itemStacks.size();
        invSize = (int)Math.ceil((float)invSize / 9.0f) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        if (itemStacks != null)
        {
            int slot = 0;
            for (ItemStack item : itemStacks)
            {
                displayInventory.setItem(slot++, item);
            }
        }

        return displayInventory;
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
