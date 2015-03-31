package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
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
    private boolean useXP = false;
    private boolean showConfirmation = true;
    private MaterialAndData confirmFillMaterial;
    private CastContext context;
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
        boolean isXP = useXP || !VaultController.hasEconomy();
        ShopItem shopItem = showingItems.get(slotIndex);
        if (shopItem == null) {
            return;
        }
        double worth = shopItem.getWorth();
        boolean hasCosts = true;
        if (worth > 0) {
            if (isXP) {
                worth = worth * controller.getWorthXP();
                hasCosts = mage.getExperience() > (int)(double)worth;
            } else {
                worth = worth * controller.getWorthBase();
                hasCosts = VaultController.getInstance().has(mage.getPlayer(), worth);
            }
        }

        if (!hasCosts) {
            String costString = context.getMessage("insufficient_resources");
            if (isXP) {
                String xpAmount = Integer.toString((int)(double)worth);
                xpAmount = messages.get("costs.xp_amount").replace("$amount", xpAmount);
                costString = costString.replace("$cost", xpAmount);
            } else {
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
                mage.activateGUI(this);
                mage.getPlayer().openInventory(confirmInventory);
                return;
            }

            String costString = context.getMessage("deducted");
            if (isXP) {
                String xpAmount = Integer.toString((int)(double)worth);
                xpAmount = messages.get("costs.xp_amount").replace("$amount", xpAmount);
                costString = costString.replace("$cost", xpAmount);
            } else {
                costString = costString.replace("$cost", VaultController.getInstance().format(worth));
            }

            costString = costString.replace("$item", itemName);
            context.sendMessage(costString);
            if (isXP) {
                mage.removeExperience((int)(double)worth);
            } else {
                VaultController.getInstance().withdrawPlayer(mage.getPlayer(), worth);
            }

            context.getController().giveItemToPlayer(mage.getPlayer(), item);
        }
        mage.deactivateGUI();
    }

    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useXP = parameters.getBoolean("use_xp", false);
        showConfirmation = parameters.getBoolean("confirm", true);
        confirmFillMaterial = ConfigurationUtils.getMaterialAndData(parameters, "confirm_filler", new MaterialAndData(Material.AIR));
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
        boolean isXP = useXP || !VaultController.hasEconomy();
        String costString = context.getMessage("cost_lore");
        for (ShopItem shopItem : items) {
            double worth = shopItem.getWorth();
            ItemStack item = shopItem.getItem();
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
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
            } else {
                worth = worth * controller.getWorthBase();
                costs = costString.replace("$cost", VaultController.getInstance().format(worth));
            }
            lore.add(ChatColor.GOLD + costs);
            meta.setLore(lore);
            item.setItemMeta(meta);
            item = InventoryUtils.makeReal(item);
            int currentSlot = itemStacks.size();
            InventoryUtils.setMeta(item, "shop", Integer.toString(currentSlot));
            this.showingItems.put(currentSlot, shopItem);
            if (showConfirmation) {
                InventoryUtils.setMeta(item, "confirm", "true");
            }
            itemStacks.add(item);
        }

        if (itemStacks.size() == 0) {
            context.sendMessage("no_items");
            return SpellResult.FAIL;
        }

        String inventoryTitle = context.getMessage("title", "Shop ($balance)");
        if (isXP) {
            String xpAmount = Integer.toString(mage.getExperience());
            xpAmount = messages.get("costs.xp_amount").replace("$amount", xpAmount);
            inventoryTitle = inventoryTitle.replace("$balance", xpAmount);
        } else {
            double balance = VaultController.getInstance().getBalance(player);
            inventoryTitle = inventoryTitle.replace("$balance", VaultController.getInstance().format(balance));
        }

        int invSize = (itemStacks.size() / 10 + 1) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (ItemStack item : itemStacks)
        {
            displayInventory.addItem(item);
        }
        mage.activateGUI(this);
        mage.getPlayer().openInventory(displayInventory);

        return SpellResult.CAST;
	}

    @Override
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
        parameters.add("confirm");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        if (parameterKey.equals("confirm")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
