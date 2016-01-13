package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseShopAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemShopAction extends BaseShopAction
{
    private class ShopItemConfiguration
    {
        ShopItemConfiguration(String key, double cost)
        {
            this.cost = cost;
            this.itemKey = key;
        }

        public String itemKey;
        public double cost;
        public String name;
        public List<String> lore;
    }
    private List<ShopItemConfiguration> items = new ArrayList<ShopItemConfiguration>();

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        items.clear();
        if (parameters.contains("items"))
        {
            if (parameters.isConfigurationSection("items")) {
                ConfigurationSection itemSection = ConfigurationUtils.getConfigurationSection(parameters, "items");
                Collection<String> itemKeys = itemSection.getKeys(false);
                for (String itemKey : itemKeys) {
                    items.add(new ShopItemConfiguration(itemKey, itemSection.getDouble(itemKey)));
                }
            } else {
                Collection<ConfigurationSection> itemList = ConfigurationUtils.getNodeList(parameters, "items");
                for (ConfigurationSection itemConfig : itemList)
                {
                    if (itemConfig != null) {
                        ShopItemConfiguration shopItem = new ShopItemConfiguration(itemConfig.getString("item"), itemConfig.getDouble("cost"));
                        shopItem.name = itemConfig.getString("name");
                        shopItem.lore = ConfigurationUtils.getStringList(itemConfig, "lore");
                        items.add(shopItem);
                    } else {
                        items.add(null);
                    }
                }
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        SpellResult contextResult = checkContext(context);
        if (!contextResult.isSuccess()) {
            return contextResult;
        }
        List<ShopItem> shopItems = new ArrayList<ShopItem>();
        MageController controller = context.getController();
        for (ShopItemConfiguration itemConfig : items) {
            if (itemConfig == null)
            {
                shopItems.add(null);
                continue;
            }
            String itemKey = itemConfig.itemKey;
            if (itemKey == null || itemKey.isEmpty() || itemKey.equalsIgnoreCase("none"))
            {
                shopItems.add(null);
                continue;
            }
            double worth = itemConfig.cost;

            // This is kinda ugly.. :|
            // This is here to undo the scaling of whatever type of currency is selected
            // So, for an SP shop- SP is converted to virtual economy units
            // and then converted back into SP by BaseShopAction.
            if (costScale > 0) {
                worth /= costScale;
            }

            String[] pieces = StringUtils.split(itemKey, '@');
            int amount = 1;
            if (pieces.length > 1) {
                itemKey = pieces[0];
                try {
                    amount = Integer.parseInt(pieces[1]);
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid item amount in shop: " + pieces[1] + " for item " + itemKey + " shop " + context.getSpell().getKey());
                }
            }
            ItemStack item = controller.createItem(itemKey);
            if (item == null) continue;

            if (itemConfig.name != null || itemConfig.lore != null) {
                ItemMeta meta = item.getItemMeta();
                if (itemConfig.name != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemConfig.name));
                }
                if (itemConfig.lore != null) {
                    List<String> lore = new ArrayList<String>();
                    for (String line : itemConfig.lore) {
                        if (line != null) {
                            lore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                    }
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
            }
            item.setAmount(amount);
            shopItems.add(new ShopItem(item, worth));
        }
        return showItems(context, shopItems);
	}
}
