package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
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

public class ItemShopAction extends BaseSpellAction implements GUIAction
{
    private boolean useXP = false;
    private boolean showConfirmation = true;
    private MaterialAndData confirmFillMaterial;
    private CastContext context;
    private Map<String, Double> items = new HashMap<String, Double>();

    @Override
    public void initialize(ConfigurationSection parameters)
    {
        super.initialize(parameters);
        items.clear();
        if (parameters.contains("items"))
        {
            ConfigurationSection itemSection = parameters.getConfigurationSection("items");
            Collection<String> itemKeys = itemSection.getKeys(false);
            for (String itemKey : itemKeys) {
                items.put(itemKey, itemSection.getDouble(itemKey));
            }
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
        if (context != null && InventoryUtils.hasMeta(item, "shop"))
        {
            Mage mage = context.getMage();
            String itemKey = InventoryUtils.getMeta(item, "shop");
            boolean isXP = useXP || !VaultController.hasEconomy();
            double worth = items.get(itemKey);
            boolean hasCosts = true;
            if (worth > 0) {
                if (isXP) {
                    hasCosts = mage.getExperience() > (int)(double)worth;
                } else {
                    hasCosts = VaultController.getInstance().has(mage.getPlayer(), worth);
                }
            }

            if (!hasCosts) {
                String costString = context.getMessage("insufficient_resources");
                if (isXP) {
                    String xpAmount = Integer.toString((int)(double)worth);
                    xpAmount = context.getMessage("costs.xp_amount").replace("$amount", xpAmount);
                    costString = costString.replace("$cost", xpAmount);
                } else {
                    costString = costString.replace("$cost", VaultController.getInstance().format(worth));
                }
                context.sendMessage(costString);
            } else {
                MagicAPI api = MagicPlugin.getAPI();
                String itemName = api.describeItem(item);
                if (InventoryUtils.hasMeta(item, "confirm")) {
                    String inventoryTitle = context.getMessage("confirm_title", "Buy $item").replace("$item", itemName);
                    Inventory confirmInventory = CompatibilityUtils.createInventory(null, 9, inventoryTitle);
                    InventoryUtils.removeMeta(item, "confirm");
                    for (int i = 0; i < 9; i++)
                    {
                        if (i != 4) {
                            ItemStack filler = confirmFillMaterial.getItemStack(1);
                            ItemMeta meta = filler.getItemMeta();
                            meta.setDisplayName(ChatColor.DARK_GRAY + (i < 4 ? "-->" : "<--"));
                            filler.setItemMeta(meta);
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

                ItemStack giveItem = api.createItem(itemKey);
                if (item == null) {
                    context.sendMessage("Invalid item: " + itemKey);
                    return;
                }
                String costString = context.getMessage("deducted");
                if (isXP) {
                    String xpAmount = Integer.toString((int)(double)worth);
                    xpAmount = context.getMessage("costs.xp_amount").replace("$amount", xpAmount);
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

                context.getController().giveItemToPlayer(mage.getPlayer(), giveItem);
            }
            mage.deactivateGUI();
        }
    }

    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useXP = parameters.getBoolean("use_xp", false);
        showConfirmation = parameters.getBoolean("confirm", true);
        confirmFillMaterial = ConfigurationUtils.getMaterialAndData(parameters, "confirm_filler", new MaterialAndData(Material.AIR));
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        this.context = context;
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        // Load items
        List<ItemStack> itemStacks = new ArrayList<ItemStack>();
        MagicAPI api = MagicPlugin.getAPI();
        boolean isXP = useXP || !VaultController.hasEconomy();
        String costString = context.getMessage("cost_lore");
        for (Map.Entry<String, Double> itemValue : items.entrySet()) {
            String itemKey = itemValue.getKey();
            double worth = items.get(itemKey);

            ItemStack item = api.createItem(itemKey);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<String>();
            }
            String costs;
            if (isXP) {
                String xpAmount = Integer.toString((int)(double)worth);
                xpAmount = context.getMessage("costs.xp_amount").replace("$amount", xpAmount);
                costs = costString.replace("$cost", xpAmount);
            } else {
                costs = costString.replace("$cost", VaultController.getInstance().format(worth));
            }
            lore.add(ChatColor.GOLD + costs);
            meta.setLore(lore);
            item.setItemMeta(meta);
            item = InventoryUtils.makeReal(item);
            InventoryUtils.setMeta(item, "shop", itemKey);
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
            xpAmount = context.getMessage("costs.xp_amount").replace("$amount", xpAmount);
            inventoryTitle = inventoryTitle.replace("$balance", xpAmount);
        } else {
            double balance = VaultController.getInstance().getBalance(player);
            inventoryTitle = inventoryTitle.replace("$balance", VaultController.getInstance().format(balance));
        }

        int invSize = ((itemStacks.size() + 9) / 9) * 9;
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
