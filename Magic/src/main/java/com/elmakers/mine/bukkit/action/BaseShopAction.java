package com.elmakers.mine.bukkit.action;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;

@Deprecated
public abstract class BaseShopAction extends BaseSpellAction implements GUIAction
{
    protected boolean requireWand = false;
    private String permissionNode = null;
    private String requiredPath = null;
    private String requiredTemplate = null;
    private String requiresCompletedPath = null;
    private String exactPath = null;
    protected boolean filterBound = false;
    protected int upgradeLevels = 0;
    protected double costScale = 1;
    protected boolean autoClose = true;
    protected boolean autoUpgrade = false;
    protected boolean castsSpells = false;
    protected boolean applyToWand = false;
    protected boolean applyToCaster = false;
    protected boolean isXP = false;
    protected boolean isSkillPoints = false;
    protected boolean sell = false;
    protected boolean isItems = false;
    protected boolean showConfirmation = true;
    protected boolean showActiveIcons = false;
    protected boolean putInHand = true;
    protected ItemStack worthItem = null;
    protected MaterialAndData confirmFillMaterial;
    protected CastContext context;
    private Map<Integer, ShopItem> showingItems;
    private List<ItemStack> itemStacks;

    // State
    private boolean isActive = false;
    private SpellResult finalResult = null;

    protected static class ShopItem implements Comparable<ShopItem> {
        private final @Nonnull ItemStack item;
        private final double worth;
        private final @Nullable String permission;

        /**
         * The configuration data attached to this item.
         *
         * <p>Only available when the item was created from a configuration section.
         */
        private final @Nullable ConfigurationSection configuration;

        public ShopItem(ItemStack item, double worth) {
            this.item = checkNotNull(item);
            this.worth = worth;
            this.permission = null;
            this.configuration = null;
        }

        public ShopItem(MageController controller, ItemStack item, ConfigurationSection configuration) {
            double worth = configuration.getDouble("cost", -1);
            if (worth < 0) {
                Double defaultWorth = controller.getWorth(item);
                worth = defaultWorth == null ? 0 : defaultWorth;
            }

            this.item = checkNotNull(item);
            this.worth = worth;
            this.permission = configuration.getString("permission");
            this.configuration = configuration;
        }

        public ShopItem(MageController controller, ItemStack item, double worth) {
            if (worth < 0) {
                Double defaultWorth = controller.getWorth(item);
                worth = defaultWorth == null ? 0 : defaultWorth;
            }

            this.item = item;
            this.worth = worth;
            this.permission = null;
            this.configuration = null;
        }

        public double getWorth() {
            return worth;
        }

        public ItemStack getItem() {
            return item;
        }

        public @Nullable String getPermission() {
            return permission;
        }

        public @Nullable ConfigurationSection getConfiguration() {
            return configuration;
        }

        @Override
        public int compareTo(ShopItem o) {
            return (int)(this.worth - o.worth);
        }
    }

    @Override
    public void deactivated() {
        // Check for shop items glitched into the player's inventory
        if (context != null) {
            context.getMage().removeItemsWithTag("shop");
        }
        isActive = false;
    }

    @Override
    public void dragged(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    protected String getDefaultMessage(CastContext context, String key) {
        return context.getController().getMessages().get("shops." + key);
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
            context.showMessage("no_wand", getDefaultMessage(context,"no_wand"));
            return SpellResult.FAIL;
        }

        WandUpgradePath path = wand.getPath();

        if (requiredTemplate != null) {
            String template = wand.getTemplateKey();
            if (template == null || !template.equals(requiredTemplate)) {
                context.showMessage(context.getMessage("no_template", getDefaultMessage(context, "no_template")).replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }
        }

        // Check path requirements
        if (requiredPath != null || exactPath != null) {
            if (path == null) {
                context.showMessage(context.getMessage("no_path", getDefaultMessage(context, "no_path")).replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }

            if (requiredPath != null && !path.hasPath(requiredPath)) {
                WandUpgradePath requiresPath = controller.getPath(requiredPath);
                if (requiresPath != null) {
                    context.showMessage(context.getMessage("no_required_path", getDefaultMessage(context, "no_required_path")).replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in Shop action: " + requiredPath);
                }
                return SpellResult.FAIL;
            }
            if (exactPath != null && !exactPath.equals(path.getKey())) {
                WandUpgradePath requiresPath = controller.getPath(exactPath);
                if (requiresPath != null) {
                    context.showMessage(context.getMessage("no_path_exact", getDefaultMessage(context, "no_path_exact")).replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in Shop action: " + exactPath);
                }
                return SpellResult.FAIL;
            }
            if (requiresCompletedPath != null) {
                if (path.canEnchant(wand)) {
                    context.showMessage(context.getMessage("no_path_end", getDefaultMessage(context, "no_path_end")).replace("$path", path.getName()));
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
                worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthXP());
                hasCosts = mage.getExperience() >= (int)worth;
            } else if (isItems) {
                worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthItemAmount());
                int hasAmount = getItemAmount(controller, mage);
                hasCosts = hasAmount >= worth;
            } else if (isSkillPoints) {
                worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthSkillPoints());
                hasCosts = mage.getSkillPoints() >= Math.ceil(worth);
            } else {
                worth = Math.ceil(costScale * worth * controller.getWorthBase());
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
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthXP());
            amountString = Integer.toString((int)worth);
            amountString = messages.get("currency.xp.amount").replace("$amount", amountString);
        }
        else if (isItems)
        {
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthItemAmount());
            amountString = formatItemAmount(controller, worth);
        }
        else if (isSkillPoints) {
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthSkillPoints());
            amountString = Integer.toString((int)Math.ceil(worth));
            amountString = messages.get("currency.sp.amount").replace("$amount", amountString);
        }
        else
        {
            worth = Math.ceil(costScale * worth * controller.getWorthBase());
            amountString = VaultController.getInstance().format(worth);
        }

        return amountString;
    }

    protected void giveCosts(CastContext context, ShopItem shopItem) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        double worth = shopItem.getWorth();
        if (isXP) {
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthXP());
            mage.giveExperience((int) worth);
        }
        else if (isItems)
        {
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthItemAmount());
            int amount = (int)Math.ceil(worth);
            ItemStack worthItem = getWorthItem(controller);
            while (amount > 0) {
                worthItem = ItemUtils.getCopy(worthItem);
                worthItem.setAmount(Math.min(amount, 64));
                amount -= worthItem.getAmount();
                mage.giveItem(worthItem);
            }
        }
        else if (isSkillPoints)
        {
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthSkillPoints());
            int amount = (int)Math.ceil(worth);
            mage.addSkillPoints(amount);
        }
        else
        {
            worth = Math.ceil(costScale * worth * controller.getWorthBase());
            VaultController.getInstance().depositPlayer(mage.getPlayer(), worth);
        }
    }

    protected boolean takeCosts(CastContext context, ShopItem shopItem) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        double worth = shopItem.getWorth();

        if (isXP) {
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthXP());
            mage.removeExperience((int)worth);
        }
        else if (isItems)
        {
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthItemAmount());
            removeItems(controller, mage, (int)Math.ceil(worth));
        }
        else if (isSkillPoints)
        {
            worth = Math.ceil(costScale * worth * controller.getWorthBase() / controller.getWorthSkillPoints());
            mage.addSkillPoints(-(int)Math.ceil(worth));
        }
        else
        {
            worth = Math.ceil(costScale * worth * controller.getWorthBase());
            VaultController.getInstance().withdrawPlayer(mage.getPlayer(), worth);
        }

        return true;
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        Mage mage = context.getMage();
        if (item == null || !NBTUtils.hasMeta(item, "shop")) {
            if (!autoClose) {
                mage.deactivateGUI();
            }
            return;
        }

        int slotIndex = Integer.parseInt(NBTUtils.getMetaString(item, "shop"));
        MageController controller = context.getController();
        Wand wand = mage.getActiveWand();

        ShopItem shopItem = showingItems.get(slotIndex);
        if (shopItem == null) {
            return;
        }
        String unpurchasableMessage = NBTUtils.getMetaString(shopItem.getItem(), "unpurchasable");
        if (unpurchasableMessage != null && !unpurchasableMessage.isEmpty()) {
            context.showMessage(unpurchasableMessage);
            mage.deactivateGUI();
            return;
        }
        boolean hasCosts = sell ? hasItem(controller, mage, shopItem.getItem()) : hasItemCosts(context, shopItem);
        if (!hasCosts) {
            String costString = context.getMessage("insufficient", getDefaultMessage(context, "insufficient"));
            if (sell) {
                costString = costString.replace("$cost", formatItemAmount(controller, item, shopItem.getItem().getAmount()));
            } else {
                costString = costString.replace("$cost", getItemCost(context, shopItem));
            }
            context.showMessage(costString);
        } else {
            String itemName = formatItemAmount(controller, item, item.getAmount());
            if (NBTUtils.hasMeta(item, "confirm")) {
                String inventoryTitle = context.getMessage("confirm_title", getDefaultMessage(context, "confirm_title")).replace("$item", itemName);
                Inventory confirmInventory = CompatibilityLib.getCompatibilityUtils().createInventory(null, 9, inventoryTitle);
                NBTUtils.removeMeta(item, "confirm");
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
                isActive = true;
                mage.activateGUI(this, confirmInventory);
                return;
            }

            String costString = context.getMessage("deducted", getDefaultMessage(context, "deducted"));
            if (sell) {
                costString = costString.replace("$cost", getItemCost(context, shopItem));
                removeItems(controller, mage, item, shopItem.getItem().getAmount());
                giveCosts(context, shopItem);
            } else {
                costString = costString.replace("$cost", getItemCost(context, shopItem));
                item = shopItem.getItem();
                if (requireWand) {
                    if (wand == null) {
                        context.showMessage("no_wand", getDefaultMessage(context, "no_wand"));
                        mage.deactivateGUI();
                        return;
                    }
                    if (applyToWand && !wand.addItem(item)) {
                        String inapplicable = context.getMessage("not_applicable", getDefaultMessage(context, "not_applicable")).replace("$item", itemName);
                        context.showMessage(inapplicable);
                        mage.deactivateGUI();
                        return;
                    }
                }
                CasterProperties caster = getCaster(context);
                if (applyToCaster && !caster.addItem(item)) {
                    String inapplicable = context.getMessage("not_applicable", getDefaultMessage(context, "not_applicable")).replace("$item", itemName);
                    context.showMessage(inapplicable);
                    mage.deactivateGUI();
                    return;
                }

                if (castsSpells) {
                    Spell spell = null;
                    String spellKey = controller.getSpell(item);
                    String spellArgs = controller.getSpellArgs(item);
                    spell = mage.getSpell(spellKey);
                    if (spell != null && (spellArgs != null ? !spell.cast(StringUtils.split(spellArgs, ' ')) : !spell.cast())) {
                        context.showMessage("cast_fail", getDefaultMessage(context, "cast_fail"));
                        mage.deactivateGUI();
                        return;
                    }
                }
                if (!takeCosts(context, shopItem)) {
                    costString = context.getMessage("insufficient", getDefaultMessage(context, "insufficient"));
                    costString = costString.replace("$cost", getItemCost(context, shopItem));
                    context.showMessage(costString);
                    return;
                }
                if (!castsSpells && !applyToWand && !applyToCaster) {
                    ItemStack copy = ItemUtils.getCopy(item);
                    if (filterBound && com.elmakers.mine.bukkit.wand.Wand.isBound(copy)) {
                        Wand bindWand = controller.getWand(copy);
                        mage.tryToOwn(bindWand);
                    }
                    if (showActiveIcons && controller.getAPI().isWand(copy))
                    {
                        Wand newWand = controller.getWand(copy);
                        com.elmakers.mine.bukkit.api.block.MaterialAndData inactiveIcon = newWand.getInactiveIcon();
                        if (inactiveIcon != null) {
                            inactiveIcon.applyToItem(copy);
                        }
                    }
                    mage.giveItem(copy, putInHand);
                }
            }

            costString = costString.replace("$item", itemName);
            context.showMessage(costString);

            if (!sell && wand != null && autoUpgrade) {
                if (upgradeLevels <= 0) {
                    com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
                    WandUpgradePath nextPath = path != null ? path.getUpgrade() : null;
                    if (nextPath != null && path.checkUpgradeRequirements(wand, null) && !path.canEnchant(wand)) {
                        path.upgrade(wand, mage);
                    }
                } else {
                    wand.enchant(upgradeLevels, mage, false);
                }
            }

            finalResult = SpellResult.CAST;
            onPurchase(context, item);
        }
        if (autoClose) {
            mage.deactivateGUI();
        } else {
            // update title
            mage.continueGUI(this, getInventory(context));
        }
    }

    protected void onPurchase(CastContext context, ItemStack itemPurchased) {

    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);
        castsSpells = parameters.getBoolean("cast_spells", false);
        showActiveIcons = parameters.getBoolean("show_active_icons", false);
    }

    @Override
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
        costScale = parameters.getDouble("scale", 1);
        filterBound = parameters.getBoolean("filter_bound", false);
        putInHand = parameters.getBoolean("put_in_hand", true);
        String worthItemKey = parameters.getString("worth_item", "");
        if (!worthItemKey.isEmpty()) {
            worthItem = context.getController().createItem(worthItemKey);
        } else {
            worthItem = null;
        }
        if (!autoClose) {
            showConfirmation = false;
        }
        if (requiresCompletedPath != null) {
            requiredPath = requiresCompletedPath;
            exactPath = requiresCompletedPath;
        }
        if (requiredPath != null || exactPath != null || requiredTemplate != null) {
            requireWand = true;
        }
        applyToCaster = parameters.getBoolean("apply_to_caster", false);
        applyToWand = parameters.getBoolean("apply_to_wand", requireWand && !applyToCaster);
        if (applyToWand) {
            applyToCaster = false;
            requireWand = true;
        }

        MageController controller = context.getController();
        isXP = parameters.getBoolean("use_xp", false);
        isItems = parameters.getBoolean("use_items", false) && getWorthItem(controller) != null;
        isSkillPoints = parameters.getBoolean("use_sp", false) && controller.isSPEnabled();
        if (!isSkillPoints && !isXP && !isItems && !VaultController.hasEconomy())
        {
            if (getWorthItem(controller) != null)
            {
                isItems = true;
            }
            else
            {
                isSkillPoints = true;
            }
        }

        finalResult = null;
        isActive = false;
    }

    protected String getBalanceDescription(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        Messages messages = controller.getMessages();
        String description = "";
        if (isXP) {
            String xpAmount = Integer.toString(mage.getExperience());
            description = messages.get("currency.xp.amount").replace("$amount", xpAmount);
        } else if (isItems) {
            int itemAmount = getItemAmount(controller, mage);
            description = formatItemAmount(controller, itemAmount);
        } else if (isSkillPoints) {
            String spAmount = Integer.toString(mage.getSkillPoints());
            description = messages.get("currency.sp.amount").replace("$amount", spAmount);
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

        this.showingItems = new HashMap<>();

        // Load items
        itemStacks = new ArrayList<>();
        String costString = context.getMessage("cost_lore", getDefaultMessage(context,"cost_lore"));
        String costHeading = context.getMessage("cost_heading", getDefaultMessage(context, "cost_heading"));
        for (ShopItem shopItem : items) {
            int currentSlot = itemStacks.size();
            if (filterBound && shopItem != null) {
                String template = com.elmakers.mine.bukkit.wand.Wand.getWandTemplate(shopItem.getItem());
                if (template != null && mage.getBoundWand(template) != null) {
                    shopItem = null;
                }
            }
            if (shopItem == null) {
                this.showingItems.put(currentSlot, null);
                itemStacks.add(new ItemStack(Material.AIR));
                continue;
            }
            ItemStack item = ItemUtils.getCopy(shopItem.getItem());
            if (item == null) continue;

            String permission = shopItem.getPermission();
            if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                itemStacks.add(item);
                continue;
            }
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }

            if (!costHeading.isEmpty()) {
                InventoryUtils.wrapText(costHeading, lore);
            }
            String costs = costString.replace("$cost", getItemCost(context, shopItem));
            if (!costs.isEmpty()) {
                lore.add(costs);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            item = ItemUtils.makeReal(item);
            NBTUtils.setMeta(item, "shop", Integer.toString(currentSlot));
            if (showConfirmation) {
                NBTUtils.setMeta(item, "confirm", "true");
            }
            this.showingItems.put(currentSlot, shopItem);
            itemStacks.add(item);
        }

        if (itemStacks.size() == 0) {
            context.showMessage("no_items", getDefaultMessage(context, "no_items"));
            return SpellResult.FAIL;
        }
        isActive = true;
        finalResult = SpellResult.NO_ACTION;
        Inventory displayInventory = getInventory(context);
        mage.activateGUI(this, displayInventory);

        return SpellResult.PENDING;
    }

    protected String getInventoryTitle(CastContext context)
    {
        Wand wand = context.getWand();
        WandUpgradePath path = (wand == null ? null : wand.getPath());
        String pathName = (path == null ? null : path.getName());
        if (pathName == null) {
            pathName = "";
        }
        String title = context.getMessage("title", getDefaultMessage(context, "title"));
        title = title.replace("$path", pathName);
        return title;
    }

    protected Inventory getInventory(CastContext context)
    {
        String inventoryTitle = getInventoryTitle(context);
        String balanceDescription = getBalanceDescription(context);
        inventoryTitle = inventoryTitle.replace("$balance", balanceDescription);

        int invSize = itemStacks == null ? 0 : itemStacks.size();
        invSize = (int)Math.ceil(invSize / 9.0f) * 9;
        Inventory displayInventory = CompatibilityLib.getCompatibilityUtils().createInventory(null, invSize, inventoryTitle);
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

    @Nullable
    protected ItemStack getWorthItem(MageController controller) {
        return worthItem == null ? controller.getWorthItem() : worthItem;
    }

    protected String formatItemAmount(MageController controller, double amount) {
        Currency currency = controller.getCurrency("item");
        if (worthItem == null && currency != null) {
            return currency.formatAmount(amount, controller.getMessages());
        }
        return formatItemAmount(controller, getWorthItem(controller), amount);
    }

    protected String formatItemAmount(MageController controller, ItemStack item, double amount) {
        String spellKey = controller.getSpell(item);
        if (spellKey != null) {
            SpellKey key = new SpellKey(spellKey);
            if (key.getLevel() > 1) {
                return controller.describeItem(item) + " " + controller.getMessages().get("spell.level_description").replace("$level", Integer.toString(key.getLevel()));
            }
            return controller.describeItem(item);
        }
        return Integer.toString((int)Math.ceil(amount)) + " " + controller.describeItem(item);
    }

    protected boolean hasItem(MageController controller, Mage mage, ItemStack item) {
        if (com.elmakers.mine.bukkit.wand.Wand.isSP(item)) {
            return mage.hasItem(item);
        }
        return getItemAmount(controller, item, mage) >= item.getAmount();
    }

    protected int getItemAmount(MageController controller, Mage mage) {
        return getItemAmount(controller, getWorthItem(controller), mage);
    }

    protected int getItemAmount(MageController controller, ItemStack worthItem, Mage mage) {
        // TODO: This should really all route through Mage.hasItem
        if (com.elmakers.mine.bukkit.wand.Wand.isSP(worthItem)) {
            return mage.getSkillPoints();
        }
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
        removeItems(controller, mage, getWorthItem(controller), amount);
    }

    protected void removeItems(MageController controller, Mage mage, ItemStack worthItem, int amount) {
        // TODO: This should really all route through Mage.hasItem
        if (com.elmakers.mine.bukkit.wand.Wand.isSP(worthItem)) {
            mage.removeItem(worthItem);
            return;
        }
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
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public void finish(CastContext context) {
        isActive = false;
        finalResult = null;
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (isActive) {
            return SpellResult.PENDING;
        }
        if (finalResult != null) {
            return finalResult;
        }
        SpellResult contextResult = checkContext(context);
        if (!contextResult.isSuccess()) {
            return contextResult;
        }

        List<ShopItem> items = getItems(context);
        if (items == null) {
            return SpellResult.NO_ACTION;
        }
        return showItems(context, items);
    }

    @Nullable
    protected abstract List<ShopItem> getItems(CastContext context);

    protected @Nonnull CasterProperties getCaster(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
        CasterProperties caster = wand;
        if (caster == null) {
            caster = mage.getActiveClass();
        }
        if (caster == null) {
            caster = mage.getProperties();
        }
        return caster;
    }
}
